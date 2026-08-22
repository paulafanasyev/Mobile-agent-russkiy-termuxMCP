package expo.modules.firewall

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat

class FirewallVpnService : VpnService() {
  private var vpnInterface: ParcelFileDescriptor? = null

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    startSecurityForeground()
    val packages = intent?.getStringArrayListExtra(EXTRA_PACKAGES) ?: arrayListOf()
    establish(packages)
    return START_STICKY
  }

  private fun startSecurityForeground() {
    val channelId = "mobile_agent_firewall"
    val manager = getSystemService(NotificationManager::class.java)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      manager.createNotificationChannel(
        NotificationChannel(
          channelId,
          "Защита сети",
          NotificationManager.IMPORTANCE_LOW,
        ),
      )
    }

    val notification = NotificationCompat.Builder(this, channelId)
      .setSmallIcon(android.R.drawable.ic_lock_lock)
      .setContentTitle("Mobile Agent — фаервол")
      .setContentText("Защита сетевого трафика включена")
      .setOngoing(true)
      .setCategory(NotificationCompat.CATEGORY_SERVICE)
      .build()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      startForeground(
        NOTIFICATION_ID,
        notification,
        android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
      )
    } else {
      startForeground(NOTIFICATION_ID, notification)
    }
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
    packages.forEach { packageName ->
      runCatching { builder.addAllowedApplication(packageName) }
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
    private const val NOTIFICATION_ID = 27001
  }
}
