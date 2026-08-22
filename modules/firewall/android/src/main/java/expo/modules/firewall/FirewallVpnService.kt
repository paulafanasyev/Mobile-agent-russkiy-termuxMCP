package expo.modules.firewall

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor

class FirewallVpnService : VpnService() {
  private var vpnInterface: ParcelFileDescriptor? = null

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    val packages = intent?.getStringArrayListExtra(EXTRA_PACKAGES) ?: arrayListOf()
    establish(packages)
    return START_STICKY
  }

  private fun establish(packages: ArrayList<String>) {
    vpnInterface?.close()
    val builder = Builder()
      .setSession("Mobile Agent — Защита сети")
      .addAddress("10.77.0.2", 32)
      .addRoute("0.0.0.0", 0)
      .addRoute("::", 0)
      .setBlocking(false)

    // MVP security mode: only explicitly allowed applications are routed
    // into the local VPN. The service intentionally does not forward packets,
    // so traffic is blocked until a real packet-processing backend is added.
    if (packages.isNotEmpty()) {
      packages.forEach { packageName ->
        runCatching { builder.addAllowedApplication(packageName) }
      }
    }

    vpnInterface = builder.establish()
  }

  override fun onDestroy() {
    vpnInterface?.close()
    vpnInterface = null
    super.onDestroy()
  }

  companion object {
    const val EXTRA_PACKAGES = "allowed_packages"
  }
}
