package de.bixilon.minosoft.protocol.network

import java.io.ByteArrayOutputStream

/**
 * Android bootstrap implementation of Minecraft's VarInt packet framing.
 * Mirrors the framing behavior of Minosoft's LengthEncoder/LengthDecoder;
 * does not replace Minosoft's full packet buffers or Netty pipeline.
 */
object AndroidPacketFraming {
    private const val MAX_PACKET = 2_097_152

    @JvmStatic
    fun encodeVarInt(value: Int): ByteArray {
        val out = ByteArrayOutputStream()
        var remaining = value
        while ((remaining and 0x7F.inv()) != 0) {
            out.write((remaining and 0x7F) or 0x80)
            remaining = remaining ushr 7
        }
        out.write(remaining)
        return out.toByteArray()
    }

    @JvmStatic
    fun decodeVarInt(bytes: ByteArray, offset: Int = 0): Pair<Int, Int>? {
        require(offset in 0..bytes.size)
        var result = 0
        for (i in 0 until 5) {
            if (offset + i >= bytes.size) return null
            val current = bytes[offset + i].toInt() and 0xFF
            if (i == 4 && (current and 0xF0) != 0) {
                throw IllegalArgumentException("Invalid VarInt: exceeds 32 bits")
            }
            result = result or ((current and 0x7F) shl (7 * i))
            if ((current and 0x80) == 0) return result to (i + 1)
        }
        throw IllegalArgumentException("VarInt exceeds five bytes")
    }

    @JvmStatic
    fun frame(payload: ByteArray): ByteArray {
        require(payload.isNotEmpty() && payload.size <= MAX_PACKET) { "Invalid packet length" }
        return encodeVarInt(payload.size) + payload
    }

    /** Returns null if more bytes are required, without consuming any data. */
    @JvmStatic
    fun readFrame(bytes: ByteArray, offset: Int = 0): Pair<ByteArray, Int>? {
        val (length, prefix) = decodeVarInt(bytes, offset) ?: return null
        require(length in 1..MAX_PACKET) { "Invalid packet length: $length" }
        val available = bytes.size.toLong() - offset - prefix
        if (available < length) return null
        return bytes.copyOfRange(offset + prefix, offset + prefix + length) to (prefix + length)
    }

    @JvmStatic
    fun selfTest(): String {
        val values = intArrayOf(0, 1, 127, 128, 255, 2097151, Int.MAX_VALUE, -1)
        for (value in values) {
            check(decodeVarInt(encodeVarInt(value))?.first == value)
        }
        val payload = byteArrayOf(0x00, 0x01, 0x7F)
        val framed = frame(payload)
        check(readFrame(framed.copyOf(framed.size - 1)) == null)
        val decoded = requireNotNull(readFrame(framed))
        check(decoded.first.contentEquals(payload) && decoded.second == framed.size)
        return "Minecraft VarInt/framing: PASS (${values.size} VarInts, partial-frame check)"
    }
}
