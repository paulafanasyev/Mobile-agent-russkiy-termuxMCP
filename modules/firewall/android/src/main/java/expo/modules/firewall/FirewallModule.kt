package expo.modules.firewall

import android.content.Context
import android.content.Intent
import android.net.VpnService
import androidx.core.content.ContextCompat
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition

class FirewallModule : Module() {
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
                throw IllegalStateException("Требуется разрешение Android VPN. Сначала вызовите prepare() и подтвердите системный диалог.")
            }
            require(requestedMode == "allowlist") {
                "Пока поддерживается только безопасный режим списка разрешённых приложений (allowlist)"
            }
            startService(context, requestedMode, packages)
        }

        AsyncFunction("stop") {
            val context = appContext.reactContext ?: return@AsyncFunction
            context.stopService(Intent(context, FirewallVpnService::class.java))
            FirewallRuntimeState.set(false, emptyList())
        }

        AsyncFunction("setRules") { requestedMode: String, packages: List<String> ->
            require(requestedMode == "allowlist") {
                "Пока поддерживается только режим allowlist"
            }
            val context = appContext.reactContext ?: throw IllegalStateException("Контекст Android недоступен")
            if (VpnService.prepare(context) != null) {
                throw IllegalStateException("Требуется разрешение Android VPN")
            }
            context.stopService(Intent(context, FirewallVpnService::class.java))
            startService(context, requestedMode, packages)
        }

        AsyncFunction("status") {
            mapOf(
                "running" to FirewallRuntimeState.isRunning(),
                "mode" to if (FirewallRuntimeState.isRunning()) "allowlist" else "off",
                "rules" to FirewallRuntimeState.rules(),
            )
        }
    }

    private fun startService(context: Context, mode: String, packages: List<String>) {
        val intent = Intent(context, FirewallVpnService::class.java).apply {
            putStringExtra(FirewallVpnService.EXTRA_MODE, mode)
            putStringArrayListExtra(FirewallVpnService.EXTRA_PACKAGES, ArrayList(packages.distinct()))
        }
        ContextCompat.startForegroundService(context, intent)
    }
}
