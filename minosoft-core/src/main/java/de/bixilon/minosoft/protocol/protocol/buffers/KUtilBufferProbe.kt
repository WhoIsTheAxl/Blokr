package de.bixilon.minosoft.protocol.protocol.buffers

/**
 * Exercises the real KUtil byte-buffer implementations that Minosoft extends.
 * This is the first direct runtime check of Minosoft's upstream buffer dependency.
 */
object KUtilBufferProbe {
    @JvmStatic fun check(): String {
        val output = de.bixilon.kutil.buffer.bytes.out.OutByteBuffer()
        output.writeVarInt(300)
        output.writeVarInt(-1)
        output.writeInt(0x12345678)
        output.writeLong(0x123456789abcdefL)
        val input = de.bixilon.kutil.buffer.bytes.`in`.InByteBuffer(output.toArray())
        check(input.readVarInt() == 300)
        check(input.readVarInt() == -1)
        check(input.readInt() == 0x12345678)
        check(input.readLong() == 0x123456789abcdefL)
        return "Original KUtil buffers: PASS (VarInt/int/long)"
    }
}
