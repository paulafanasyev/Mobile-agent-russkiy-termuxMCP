package com.mobileshell.firewall

import android.content.Context
import android.net.VpnService
import android.util.Log
import io.nekohasekai.libbox.CommandServer
import io.nekohasekai.libbox.CommandServerHandler
import io.nekohasekai.libbox.Libbox
import io.nekohasekai.libbox.OverrideOptions
import io.nekohasekai.libbox.PlatformInterface
import io.nekohasekai.libbox.SystemProxyStatus

/**
 * Реальный мост Android VpnService -> CommandServer -> libbox.
 *
 * В собранном AAR из закреплённого revision API предоставляет CommandServer,
 * а не старый BoxService. Поэтому мост использует фактический API артефакта.
 */
class LibboxForwardingBridge(
    private val context: Context,
    private val vpnService: VpnService,
    private val packages: List<String>,
) : CommandServerHandler {
    @Volatile
    private var commandServer: CommandServer? = null

    @Volatile
    private var running = false

    fun start(configJson: String): Result<Unit> {
        if (configJson.isBlank()) {
            return Result.failure(IllegalArgumentException("Конфигурация libbox пуста"))
        }
        return runCatching {
            stop()

            val setup = io.nekohasekai.libbox.SetupOptions().apply {
                basePath = context.filesDir.absolutePath
                workingPath = context.filesDir.absolutePath
                tempPath = context.cacheDir.absolutePath
                fixAndroidStack = true
                debug = false
            }
            Libbox.setup(setup)
            Libbox.setLocale("ru")
            Libbox.touch()

            val platform: PlatformInterface = LibboxAndroidPlatform(vpnService, packages)
            val server = CommandServer(this, platform)
            server.start()
            server.startOrReloadService(
                configJson,
                OverrideOptions().apply {
                    autoRedirect = false
                },
            )
            commandServer = server
            running = true
        }.onFailure {
            Log.e(TAG, "Не удалось запустить libbox", it)
            runCatching { commandServer?.close() }
            commandServer = null
            running = false
        }
    }

    fun stop() {
        val server = commandServer
        commandServer = null
        running = false
        if (server != null) {
            runCatching { server.closeService() }
            runCatching { server.close() }
        }
    }

    fun isRunning(): Boolean = running

    override fun serviceStop() {
        running = false
    }

    override fun serviceReload() {
        // Перезагрузка выполняется вызывающим слоем с новой конфигурацией.
    }

    override fun getSystemProxyStatus(): SystemProxyStatus = SystemProxyStatus().apply {
        available = false
        enabled = false
    }

    override fun setSystemProxyEnabled(enabled: Boolean) = Unit

    override fun triggerNativeCrash() {
        throw IllegalStateException("Отладочный native crash вызван явно")
    }

    override fun writeDebugMessage(message: String) {
        Log.d(TAG, message)
    }

    override fun connectSSHAgent(): Int = -1

    companion object {
        private const val TAG = "MobileAgentLibbox"
    }
}
