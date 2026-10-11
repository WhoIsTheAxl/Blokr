/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.network.network.client.netty.pipeline

import de.bixilon.minosoft.protocol.network.network.client.netty.packet.receiver.PacketReceiver
import de.bixilon.minosoft.protocol.network.network.client.netty.packet.receiver.QueuedS2CP
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler

class ClientPacketHandler(
    private val receiver: PacketReceiver,
) : SimpleChannelInboundHandler<QueuedS2CP<*>>() {

    override fun channelRead0(context: ChannelHandlerContext, queued: QueuedS2CP<*>) {
        receiver.onReceive(queued.type, queued.packet)
    }

    companion object {
        const val NAME = "client_packet_handler"
    }
}
