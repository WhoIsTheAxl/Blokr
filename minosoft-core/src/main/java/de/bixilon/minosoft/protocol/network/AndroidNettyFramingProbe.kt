package de.bixilon.minosoft.protocol.network

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.embedded.EmbeddedChannel
import io.netty.handler.codec.ByteToMessageDecoder
import io.netty.handler.codec.MessageToByteEncoder

/**
 * Exercises real Netty's Android-compatible framing pipeline in memory.
 * This is a bridge toward Minosoft's LengthEncoder/LengthDecoder, not a
 * substitute for Minosoft's complete network/session implementation.
 */
object AndroidNettyFramingProbe {
    private const val MAX_LENGTH = 2_097_152

    private class FrameEncoder : MessageToByteEncoder<ByteArray>() {
        override fun encode(ctx: ChannelHandlerContext, msg: ByteArray, out: ByteBuf) {
            require(msg.isNotEmpty() && msg.size <= MAX_LENGTH)
            out.writeBytes(AndroidPacketFraming.encodeVarInt(msg.size))
            out.writeBytes(msg)
        }
    }

    private class FrameDecoder : ByteToMessageDecoder() {
        override fun decode(ctx: ChannelHandlerContext, input: ByteBuf, output: MutableList<Any>) {
            input.markReaderIndex()
            var length = 0
            var prefix = 0
            while (prefix < 5) {
                if (!input.isReadable) {
                    input.resetReaderIndex()
                    return
                }
                val b = input.readUnsignedByte().toInt()
                if (prefix == 4 && b and 0xF0 != 0) throw IllegalArgumentException("Invalid VarInt")
                length = length or ((b and 0x7F) shl (7 * prefix))
                prefix++
                if (b and 0x80 == 0) break
            }
            if (prefix == 5 && input.getUnsignedByte(input.readerIndex() - 1).toInt() and 0x80 != 0) {
                throw IllegalArgumentException("VarInt exceeds five bytes")
            }
            require(length in 1..MAX_LENGTH) { "Invalid packet length" }
            if (input.readableBytes() < length) {
                input.resetReaderIndex()
                return
            }
            val packet = ByteArray(length)
            input.readBytes(packet)
            output.add(packet)
        }
    }

    @JvmStatic
    fun check(): String {
        val encoder = EmbeddedChannel(FrameEncoder())
        val decoder = EmbeddedChannel(FrameDecoder())
        try {
            val packet = byteArrayOf(0, 1, 2, 127)
            check(encoder.writeOutbound(packet))
            val encoded = encoder.readOutbound<ByteBuf>()
            try {
                val half = encoded.readRetainedSlice(2)
                try {
                    decoder.writeInbound(half)
                    check(decoder.readInbound<ByteArray>() == null)
                } finally {
                    // EmbeddedChannel owns the inbound buffer after writeInbound.
                }
                val rest = encoded.readRetainedSlice(encoded.readableBytes())
                decoder.writeInbound(rest)
                val result = decoder.readInbound<ByteArray>()
                check(result != null && result.contentEquals(packet))
            } finally {
                encoded.release()
            }
            return "Netty packet pipeline: PASS (fragmented frame)"
        } finally {
            encoder.finishAndReleaseAll()
            decoder.finishAndReleaseAll()
        }
    }
}
