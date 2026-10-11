/*
 * Blokr Minosoft-core integration contracts.
 * Copyright (C) 2026 Blokr contributors.
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * NOT upstream Minosoft implementations. These compile-time contracts only
 * describe future integration boundaries. No runtime implementations provided.
 */
package de.bixilon.minosoft.portplan

/** Requires protocol registry, packet handlers and authenticated session. */
interface WorldNetworkPort {
    fun disconnect()
}

/** Requires original world renderer, meshing and Android graphics adaptation. */
interface WorldRenderingPort {
    fun onWorldUnloaded()
}

/** Requires upstream client lifecycle and session management. */
interface ClientLifecyclePort {
    fun stop()
}
