package com.mobileshell.firewall

import android.net.VpnService

/**
 * Compatibility boundary for the generated libbox AAR.
 *
 * The previous implementation referenced a non-existent Java/Kotlin class,
 * which made the Android module impossible to compile. The real libbox
 * PlatformInterface/CommandServer adapter must be wired here before the
 * firewall is reported as running.
 */
class LibboxForwardingBridge(
    private val vpnService: VpnService,
    @Suppress("UNUSED_PARAMETER") protectedService: VpnService,
    @Suppress("UNUSED_PARAMETER") private val packages: List<String>,
) {
    data class StartResult(val isSuccess: Boolean)

    fun start(@Suppress("UNUSED_PARAMETER") config: String): StartResult {
        val libboxPresent = runCatching {
            Class.forName("libbox.Libbox")
        }.isSuccess

        if (!libboxPresent) {
            return StartResult(false)
        }

        // Do not claim that the VPN is running until the native
        // PlatformInterface/OpenTun adapter is actually connected.
        return StartResult(false)
    }

    fun stop() {
        // Native libbox is not started by this compatibility boundary.
        // Kept idempotent so service shutdown remains safe.
    }
}
