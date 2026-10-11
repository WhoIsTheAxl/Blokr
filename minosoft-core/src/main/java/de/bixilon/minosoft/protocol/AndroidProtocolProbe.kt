package de.bixilon.minosoft.protocol

import de.bixilon.minosoft.protocol.ProtocolUtil.encodeNetwork
import de.bixilon.minosoft.protocol.status.StatusPing
import de.bixilon.minosoft.protocol.status.StatusPong

/**
 * Android smoke test exercising Minosoft's original protocol utilities and
 * status timing types. This does not open a Minecraft network connection.
 */
object AndroidProtocolProbe {
    @JvmStatic
    fun check(): String {
        val bytes = with(ProtocolUtil) { "Blokr".encodeNetwork() }
        val ping = StatusPing()
        val pong = StatusPong(ping.send.elapsedNow())
        return "Minosoft protocol UTF-8: ${bytes.size} bytes; status timer: ${pong.latency.inWholeMilliseconds} ms"
    }
}
