package com.blokr.app.graphics;

import android.opengl.GLES30;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import de.bixilon.minosoft.data.world.portable.PortableWorld;

/**
 * First voxel-world rendering milestone: draws visible faces from real
 * Minosoft-core PortableWorld block storage using OpenGL ES 3.0.
 *
 * This is a local demonstration world, not a loaded Minecraft save, and not
 * the upstream Minosoft renderer. No Minecraft assets are bundled.
 * All methods are invoked on the GLSurfaceView render thread.
 */
public final class PortableWorldRenderer {
    private static final String TAG = "PortableWorldRenderer";
    private static final String VERTEX_SHADER =
            "#version 300 es\n" +
            "layout(location=0) in vec3 position;\n" +
            "layout(location=1) in vec3 color;\n" +
            "uniform mat4 mvp;\n" +
            "out vec3 tint;\n" +
            "void main(){gl_Position=mvp*vec4(position,1.0);tint=color;}\n";
    private static final String FRAGMENT_SHADER =
            "#version 300 es\n" +
            "precision mediump float;\n" +
            "in vec3 tint;out vec4 fragment;\n" +
            "void main(){fragment=vec4(tint,1.0);}\n";

    private static final int[][] NORMALS = {
            {0, 1, 0}, {0, -1, 0}, {1, 0, 0},
            {-1, 0, 0}, {0, 0, 1}, {0, 0, -1}
    };
    // Four corners per face, wound counterclockwise from the outside.
    private static final float[][][] FACES = {
            {{0,1,0},{0,1,1},{1,1,1},{1,1,0}},
            {{0,0,1},{0,0,0},{1,0,0},{1,0,1}},
            {{1,0,0},{1,1,0},{1,1,1},{1,0,1}},
            {{0,0,1},{0,1,1},{0,1,0},{0,0,0}},
            {{1,0,1},{1,1,1},{0,1,1},{0,0,1}},
            {{0,0,0},{0,1,0},{1,1,0},{1,0,0}}
    };
    private static final int[] TRIANGLE_INDICES = {0, 1, 2, 0, 2, 3};
    private static final float[] SHADE = {1.0f, .55f, .78f, .78f, .87f, .67f};

    private final PortableWorld world = new PortableWorld();
    private final float[] projection = new float[16];
    private final float[] view = new float[16];
    private final float[] mvp = new float[16];
    private final float[] scratch = new float[16];
    private int program;
    private int vertexBuffer;
    private int vertexCount;
    private long firstFrameNanos;
    private float yaw = 0.65f;
    private float pitch = 0.38f;

    public PortableWorldRenderer() {
        generateDemoWorld();
    }

    private void generateDemoWorld() {
        // IDs 1/2/3 are demo grass/dirt/stone colors, not global Minecraft state IDs.
        for (int x = -12; x <= 12; x++) {
            for (int z = -12; z <= 12; z++) {
                int height = (int) Math.round(2.0 * Math.sin(x * .29)
                        + 1.5 * Math.cos(z * .36));
                for (int y = -4; y <= height; y++) {
                    int id = y == height ? 1 : y >= height - 2 ? 2 : 3;
                    world.setBlock(x, y, z, id);
                }
            }
        }
        // Distinctive blocks demonstrate vertical geometry and occlusion.
        for (int y = 3; y < 8; y++) world.setBlock(0, y, 0, 3);
        Log.i(TAG, "Generated demo world with " + world.loadedChunks() + " chunk columns");
    }

    private static int compile(int type, String source) {
        int shader = GLES30.glCreateShader(type);
        GLES30.glShaderSource(shader, source);
        GLES30.glCompileShader(shader);
        int[] ok = new int[1];
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, ok, 0);
        if (ok[0] == 0) {
            String error = GLES30.glGetShaderInfoLog(shader);
            GLES30.glDeleteShader(shader);
            throw new IllegalStateException("Shader compilation failed: " + error);
        }
        return shader;
    }

    public void onSurfaceCreated() {
        // Context loss requires rebuilding GPU objects; CPU world stays intact.
        int vertex = compile(GLES30.GL_VERTEX_SHADER, VERTEX_SHADER);
        int fragment = compile(GLES30.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);
        program = GLES30.glCreateProgram();
        GLES30.glAttachShader(program, vertex);
        GLES30.glAttachShader(program, fragment);
        GLES30.glLinkProgram(program);
        GLES30.glDeleteShader(vertex);
        GLES30.glDeleteShader(fragment);
        int[] linked = new int[1];
        GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linked, 0);
        if (linked[0] == 0) throw new IllegalStateException(
                "Program linking failed: " + GLES30.glGetProgramInfoLog(program));
        uploadMesh();
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        GLES30.glEnable(GLES30.GL_CULL_FACE);
        GLES30.glCullFace(GLES30.GL_BACK);
        GLES30.glClearColor(.52f, .73f, .94f, 1f);
        firstFrameNanos = System.nanoTime();
    }

    private void uploadMesh() {
        // Worst-case cap is intentionally avoided: grow the CPU buffer as needed.
        FloatBuilder data = new FloatBuilder();
        for (int x = -12; x <= 12; x++) for (int z = -12; z <= 12; z++) {
            for (int y = -4; y <= 8; y++) {
                int block = world.getBlock(x, y, z);
                if (block == 0) continue;
                for (int face = 0; face < 6; face++) {
                    int[] n = NORMALS[face];
                    if (world.getBlock(x + n[0], y + n[1], z + n[2]) != 0) continue;
                    float[] rgb = blockColor(block, face);
                    for (int index : TRIANGLE_INDICES) {
                        float[] corner = FACES[face][index];
                        data.add(x + corner[0]);
                        data.add(y + corner[1]);
                        data.add(z + corner[2]);
                        data.add(rgb[0] * SHADE[face]);
                        data.add(rgb[1] * SHADE[face]);
                        data.add(rgb[2] * SHADE[face]);
                    }
                }
            }
        }
        vertexCount = data.size / 6;
        FloatBuffer vertices = ByteBuffer.allocateDirect(data.size * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertices.put(data.values, 0, data.size).position(0);
        int[] buffers = new int[1];
        GLES30.glGenBuffers(1, buffers, 0);
        vertexBuffer = buffers[0];
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vertexBuffer);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, data.size * 4,
                vertices, GLES30.GL_STATIC_DRAW);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
        Log.i(TAG, "Uploaded " + vertexCount / 3 + " triangles from PortableWorld");
    }

    private static float[] blockColor(int block, int face) {
        if (block == 1) return face == 0
                ? new float[] {.30f, .68f, .20f}
                : new float[] {.42f, .50f, .22f};
        if (block == 2) return new float[] {.48f, .33f, .20f};
        return new float[] {.49f, .51f, .54f};
    }

    public void onSurfaceChanged(int width, int height) {
        GLES30.glViewport(0, 0, width, height);
        Matrix.perspectiveM(projection, 0, 70f, (float) width / Math.max(1, height),
                .1f, 110f);
    }

    public void renderFrame() {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
        float elapsed = (System.nanoTime() - firstFrameNanos) * 1e-9f;
        // Slow orbit is a camera demonstration, not player controls.
        float angle = yaw + elapsed * .11f;
        float radius = 30f;
        float eyeX = (float) Math.sin(angle) * radius;
        float eyeZ = (float) Math.cos(angle) * radius;
        float eyeY = 12f + pitch * 10f;
        Matrix.setLookAtM(view, 0, eyeX, eyeY, eyeZ, 0, 0, 0, 0, 1, 0);
        Matrix.multiplyMM(mvp, 0, projection, 0, view, 0);
        GLES30.glUseProgram(program);
        GLES30.glUniformMatrix4fv(GLES30.glGetUniformLocation(program, "mvp"),
                1, false, mvp, 0);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vertexBuffer);
        GLES30.glEnableVertexAttribArray(0);
        GLES30.glEnableVertexAttribArray(1);
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 24, 0);
        GLES30.glVertexAttribPointer(1, 3, GLES30.GL_FLOAT, false, 24, 12);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexCount);
        GLES30.glDisableVertexAttribArray(0);
        GLES30.glDisableVertexAttribArray(1);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
    }

    private static final class FloatBuilder {
        float[] values = new float[16384];
        int size;
        void add(float value) {
            if (size == values.length)
                values = java.util.Arrays.copyOf(values, values.length * 2);
            values[size++] = value;
        }
    }
}
