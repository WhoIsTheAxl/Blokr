package com.blokr.app.bridge;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Diagnostic-only TCP connectivity probe. This does not implement Minecraft's
 * handshake, status protocol, authentication, or Minosoft's Netty transport.
 */
public final class NetworkTransportProbe {
    private NetworkTransportProbe() {}

    public static String connect(String host, int port, int timeoutMillis) throws IOException {
        if (host == null || host.trim().isEmpty()) throw new IllegalArgumentException("Host is required");
        if (port < 1 || port > 65535) throw new IllegalArgumentException("Invalid port");
        long start = System.nanoTime();
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host.trim(), port), timeoutMillis);
            long millis = (System.nanoTime() - start) / 1_000_000L;
            return "TCP connected in " + millis + " ms";
        }
    }
}
