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

import de.bixilon.minosoft.protocol.network.AndroidPacketFraming
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder

class LengthEncoder(private val maxLength: Int) : MessageToByteEncoder<ByteArray>() {
    fun write(data: ByteArray, out: ByteBuf) {
        require(data.isNotEmpty() && data.size <= maxLength) { "Invalid packet size: ${data.size}" }
        out.writeBytes(AndroidPacketFraming.encodeVarInt(data.size))
        out.writeBytes(data)
    }

    override fun encode(context: ChannelHandlerContext, data: ByteArray, out: ByteBuf) {
        write(data, out)
    }

    companion object { const val NAME = "length_encoder" }
}
