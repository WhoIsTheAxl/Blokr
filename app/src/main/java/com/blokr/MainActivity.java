package com.blokr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.os.Handler;
import android.os.Looper;
import com.blokr.app.bridge.NetworkTransportProbe;

import com.blokr.app.bridge.MinosoftBridge;
import com.blokr.app.bridge.MinosoftCoreProbe;
import com.blokr.app.diagnostic.Diagnostics;
import de.bixilon.minosoft.protocol.AndroidProtocolProbe;
import de.bixilon.minosoft.protocol.network.AndroidPacketFraming;
import com.blokr.app.view.activities.GameActivity;

/** Android launcher for the bridge and graphics bootstrap. */
public final class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Diagnostics.info("MainActivity", "Launcher created; bridge=" + MinosoftBridge.getInstance().getState());

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        int padding = (int) (24 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.blokr_logo);
        int logoSize = (int) (96 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(logoSize, logoSize);
        logoParams.gravity = Gravity.CENTER_HORIZONTAL;
        logoParams.bottomMargin = (int) (12 * getResources().getDisplayMetrics().density);
        layout.addView(logo, logoParams);

        TextView status = new TextView(this);
        String protocolStatus = AndroidProtocolProbe.check();
        Diagnostics.info("MinosoftProtocol", protocolStatus);
        String framingStatus;
        try {
            framingStatus = AndroidPacketFraming.selfTest();
            Diagnostics.info("PacketFraming", framingStatus);
        } catch (Exception failure) {
            framingStatus = "Packet framing: FAIL (" + failure.getClass().getSimpleName() + ")";
            Diagnostics.warn("PacketFraming", framingStatus);
        }
        status.setText("Blokr\nAndroid bootstrap ready\nMinosoft: "
            + MinosoftBridge.getInstance().getState()
            + "\n" + MinosoftCoreProbe.check()
            + "\n" + protocolStatus
            + "\n" + framingStatus);
        status.setTextSize(22);
        status.setGravity(Gravity.CENTER);
        layout.addView(status);

        Button graphics = new Button(this);
        graphics.setText("Test OpenGL ES 3.0");
        graphics.setOnClickListener(v ->
            startActivity(new Intent(this, GameActivity.class)));
        layout.addView(graphics);
        EditText host = new EditText(this);
        host.setSingleLine(true);
        host.setHint("Server hostname");
        layout.addView(host);

        EditText port = new EditText(this);
        port.setSingleLine(true);
        port.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        port.setText("25565");
        layout.addView(port);

        TextView networkStatus = new TextView(this);
        networkStatus.setText("TCP diagnostic only — no Minecraft handshake");
        layout.addView(networkStatus);

        Button connect = new Button(this);
        connect.setText("Test TCP Connection");
        connect.setOnClickListener(v -> {
            final String target = host.getText().toString().trim();
            final String portText = port.getText().toString().trim();
            final int targetPort;
            try {
                targetPort = Integer.parseInt(portText);
                if (targetPort < 1 || targetPort > 65535) throw new NumberFormatException();
                if (target.isEmpty()) throw new IllegalArgumentException("Enter a hostname");
            } catch (RuntimeException invalid) {
                networkStatus.setText("Enter a valid hostname and port (1–65535)");
                return;
            }
            connect.setEnabled(false);
            networkStatus.setText("Connecting...");
            Handler main = new Handler(Looper.getMainLooper());
            new Thread(() -> {
                String result;
                try {
                    result = NetworkTransportProbe.connect(target, targetPort, 5000);
                    Diagnostics.info("NetworkProbe", "TCP connection succeeded; port=" + targetPort);
                } catch (Exception failure) {
                    result = "TCP connection failed: " + failure.getClass().getSimpleName();
                    Diagnostics.warn("NetworkProbe", result);
                }
                final String display = result;
                main.post(() -> {
                    networkStatus.setText(display);
                    connect.setEnabled(true);
                });
            }, "Blokr-TCP-Probe").start();
        });
        layout.addView(connect);
        setContentView(layout);
    }
}
