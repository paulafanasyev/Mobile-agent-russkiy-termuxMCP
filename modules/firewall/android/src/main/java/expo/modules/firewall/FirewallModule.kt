package expo.modules.firewall

import android.content.Context
import android.content.Intent
import android.net.VpnService
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition

class FirewallModule : Module() {
  private var running = false
  private var mode = "off"
  private var rules = emptyList<String>()

  override fun definition() = ModuleDefinition {
    Name("Firewall")

    AsyncFunction("prepare") {
      val context = appContext.reactContext ?: throw IllegalStateException("Контекст Android недоступен")
      val intent = VpnService.prepare(context)
      if (intent != null) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        false
      } else {
        true
      }
    }

    AsyncFunction("start") { requestedMode: String, packages: List<String> ->
      val context = appContext.reactContext ?: throw IllegalStateException("Контекст Android недоступен")
      if (VpnService.prepare(context) != null) {
        throw IllegalStateException("Требуется разрешение Android VPN. Вызовите prepare() и подтвердите системный диалог.")
      }
      if (requestedMode != "allowlist") {
        throw IllegalArgumentException("Пока поддерживается только безопасный режим списка разрешённых приложений (allowlist)")
      }
      val intent = Intent(context, FirewallVpnService::class.java).apply {
        putStringArrayListExtra(FirewallVpnService.EXTRA_PACKAGES, ArrayList(packages))
      }
      context.startService(intent)
      running = true
      mode = requestedMode
      rules = packages.toList()
    }

    AsyncFunction("stop") {
      val context = appContext.reactContext ?: return@AsyncFunction
      context.stopService(Intent(context, FirewallVpnService::class.java))
      running = false
      mode = "off"
      rules = emptyList()
    }

    AsyncFunction("setRules") { requestedMode: String, packages: List<String> ->
      if (!running) {
        throw IllegalStateException("Фаервол не запущен")
      }
      if (requestedMode != "allowlist") {
        throw IllegalArgumentException("Пока поддерживается только режим allowlist")
      }
      val context = appContext.reactContext ?: throw IllegalStateException("Контекст Android недоступен")
      context.stopService(Intent(context, FirewallVpnService::class.java))
      val intent = Intent(context, FirewallVpnService::class.java).apply {
        putStringArrayListExtra(FirewallVpnService.EXTRA_PACKAGES, ArrayList(packages))
      }
      context.startService(intent)
      mode = requestedMode
      rules = packages.toList()
    }

    AsyncFunction("status") {
      mapOf(
        "running" to running,
        "mode" to mode,
        "rules" to rules,
      )
    }
  }
}
