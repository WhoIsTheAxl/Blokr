/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * Android adaptation: ByteArray replaces KUtil ArbitraryByteArray until the original buffers are integrated.
 */
package de.bixilon.minosoft.protocol.network.network.client.netty.pipeline.length

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder

class LengthDecoder(private val maxLength: Int) : ByteToMessageDecoder() {
    fun read(buffer: ByteBuf): ByteArray? {
        buffer.markReaderIndex()
        var length = 0
        for (i in 0 until 5) {
            if (!buffer.isReadable) {
                buffer.resetReaderIndex()
                return null
            }
            val current = buffer.readUnsignedByte().toInt()
            require(i != 4 || (current and 0xF0) == 0) { "Invalid packet length VarInt" }
            length = length or ((current and 0x7F) shl (7 * i))
            if (current and 0x80 == 0) {
                require(length in 1..maxLength) { "Invalid packet size: $length" }
                if (buffer.readableBytes() < length) {
                    buffer.resetReaderIndex()
                    return null
                }
                return ByteArray(length).also { buffer.readBytes(it) }
            }
        }
        throw IllegalArgumentException("Packet length VarInt exceeds five bytes")
    }

    override fun decode(context: ChannelHandlerContext, buffer: ByteBuf, out: MutableList<Any>) {
        out.add(read(buffer) ?: return)
    }

    companion object { const val NAME = "length_decoder" }
}
