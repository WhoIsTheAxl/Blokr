package de.bixilon.minosoft.protocol.protocol.buffers

import de.bixilon.minosoft.protocol.network.AndroidPacketFraming
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

/**
 * Android bootstrap for the primitive subset of Minosoft's packet buffers.
 * The original InByteBuffer/OutByteBuffer depend on KUtil and other game types;
 * this isolated bridge intentionally does not pretend to implement those APIs.
 */
class AndroidPrimitiveOutBuffer {
    private val bytes = ByteArrayOutputStream()
    fun writeByte(value: Int) { bytes.write(value and 255) }
    fun writeInt(value: Int) { bytes.write(ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(value).array()) }
    fun writeLong(value: Long) { bytes.write(ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN).putLong(value).array()) }
    fun writeVarInt(value: Int) { bytes.write(AndroidPacketFraming.encodeVarInt(value)) }
    fun writeString(value: String, maxCharacters: Int = 32767) {
        require(value.length <= maxCharacters)
        val encoded = value.toByteArray(StandardCharsets.UTF_8)
        require(encoded.size <= maxCharacters.toLong() * 4)
        writeVarInt(encoded.size)
        bytes.write(encoded)
    }
    fun toArray(): ByteArray = bytes.toByteArray()
}

class AndroidPrimitiveInBuffer(private val bytes: ByteArray) {
    private var cursor = 0
    val remaining: Int get() = bytes.size - cursor
    private fun take(count: Int): ByteArray {
        require(count >= 0 && count <= remaining) { "Truncated packet" }
        return bytes.copyOfRange(cursor, cursor + count).also { cursor += count }
    }
    fun readByte(): Int = take(1)[0].toInt()
    fun readInt(): Int = ByteBuffer.wrap(take(4)).order(ByteOrder.BIG_ENDIAN).int
    fun readLong(): Long = ByteBuffer.wrap(take(8)).order(ByteOrder.BIG_ENDIAN).long
    fun readVarInt(): Int {
        val (value, consumed) = AndroidPacketFraming.decodeVarInt(bytes, cursor)
            ?: throw IllegalArgumentException("Truncated VarInt")
        cursor += consumed
        return value
    }
    fun readString(maxCharacters: Int = 32767): String {
        val length = readVarInt()
        require(length >= 0 && length.toLong() <= maxCharacters.toLong() * 4 && length <= remaining)
        val value = String(take(length), StandardCharsets.UTF_8)
        require(value.length <= maxCharacters)
        return value
    }
}

object AndroidPrimitiveBufferProbe {
    @JvmStatic fun check(): String {
        val output = AndroidPrimitiveOutBuffer()
        output.writeByte(0x7f)
        output.writeInt(0x12345678)
        output.writeLong(0x123456789abcdefL)
        output.writeVarInt(300)
        output.writeVarInt(-1)
        output.writeString("Blokr 🌍")
        val input = AndroidPrimitiveInBuffer(output.toArray())
        check(input.readByte() == 127)
        check(input.readInt() == 0x12345678)
        check(input.readLong() == 0x123456789abcdefL)
        check(input.readVarInt() == 300)
        check(input.readVarInt() == -1)
        check(input.readString() == "Blokr 🌍")
        check(input.remaining == 0)
        return "Minosoft primitive bridge: PASS (byte/int/long/VarInt/UTF-8)"
    }
}
