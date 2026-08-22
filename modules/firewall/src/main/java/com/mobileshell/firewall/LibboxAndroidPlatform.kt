package com.mobileshell.firewall

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInterface
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.os.Process
import io.nekohasekai.libbox.BridgeOptions
import io.nekohasekai.libbox.BridgeSession
import io.nekohasekai.libbox.ConnectionOwner
import io.nekohasekai.libbox.InterfaceUpdateListener
import io.nekohasekai.libbox.Libbox
import io.nekohasekai.libbox.LocalDNSTransport
import io.nekohasekai.libbox.NeighborEntryIterator
import io.nekohasekai.libbox.NeighborUpdateListener
import io.nekohasekai.libbox.NetworkInterface as LibboxNetworkInterface
import io.nekohasekai.libbox.NetworkInterfaceIterator
import io.nekohasekai.libbox.PlatformInterface
import io.nekohasekai.libbox.PlatformUser
import io.nekohasekai.libbox.ShellSession
import io.nekohasekai.libbox.StringIterator
import io.nekohasekai.libbox.TunOptions
import io.nekohasekai.libbox.WIFIState
import java.net.InetSocketAddress

/**
 * Реализация PlatformInterface для Android VpnService.
 *
 * Важное отличие от старого каркаса: TUN создаётся Android VpnService.Builder,
 * а полученный дескриптор передаётся реальному libbox через PlatformInterface.
 */
internal class LibboxAndroidPlatform(
    private val service: VpnService,
    private val packages: List<String>,
) : PlatformInterface {
    private val connectivity =
        service.getSystemService(ConnectivityManager::class.java)

    override fun usePlatformAutoDetectInterfaceControl(): Boolean = true

    override fun autoDetectInterfaceControl(fd: Int) {
        check(service.protect(fd)) { "Не удалось защитить сокет libbox от VPN-петли" }
    }

    override fun openTun(options: TunOptions): Int {
        check(VpnService.prepare(service) == null) {
            "Не предоставлено разрешение Android VPN"
        }

        val builder = service.Builder()
            .setSession("Mobile Agent — Защита сети")
            .setMtu(options.mtu.coerceIn(576, 65535))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder.setMetered(false)
        }

        addAddresses(builder, options)
        addRoutes(builder, options)
        addPackages(builder, options)

        val descriptor = builder.establish()
            ?: error("Android не смог создать TUN-интерфейс")

        return descriptor.detachFd()
    }

    private fun addAddresses(builder: VpnService.Builder, options: TunOptions) {
        val ipv4 = options.inet4Address
        while (ipv4.hasNext()) {
            val prefix = ipv4.next()
            builder.addAddress(prefix.address(), prefix.prefix())
        }
        val ipv6 = options.inet6Address
        while (ipv6.hasNext()) {
            val prefix = ipv6.next()
            builder.addAddress(prefix.address(), prefix.prefix())
        }
    }

    private fun addRoutes(builder: VpnService.Builder, options: TunOptions) {
        if (!options.autoRoute) return

        val dns = options.dnsServerAddress
        while (dns.hasNext()) {
            runCatching { builder.addDnsServer(dns.next()) }
        }

        val ipv4 = options.inet4RouteRange
        while (ipv4.hasNext()) {
            val prefix = ipv4.next()
            builder.addRoute(prefix.address(), prefix.prefix())
        }
        val ipv6 = options.inet6RouteRange
        while (ipv6.hasNext()) {
            val prefix = ipv6.next()
            builder.addRoute(prefix.address(), prefix.prefix())
        }

        if (!ipv4HadRoute(options) && !ipv6HadRoute(options)) {
            if (options.inet4Address.hasNext()) builder.addRoute("0.0.0.0", 0)
            if (options.inet6Address.hasNext()) builder.addRoute("::", 0)
        }
    }

    private fun ipv4HadRoute(options: TunOptions): Boolean =
        options.inet4RouteAddress.hasNext() || options.inet4RouteRange.hasNext()

    private fun ipv6HadRoute(options: TunOptions): Boolean =
        options.inet6RouteAddress.hasNext() || options.inet6RouteRange.hasNext()

    private fun addPackages(builder: VpnService.Builder, options: TunOptions) {
        val include = mutableListOf<String>()
        val includeIterator = options.includePackage
        while (includeIterator.hasNext()) include += includeIterator.next()
        if (include.isEmpty()) include += packages
        include.distinct().filter { it.isNotBlank() }.forEach { packageName ->
            runCatching { builder.addAllowedApplication(packageName) }
        }

        val exclude = options.excludePackage
        while (exclude.hasNext()) {
            runCatching { builder.addDisallowedApplication(exclude.next()) }
        }
    }

    override fun useProcFS(): Boolean = false

    override fun findConnectionOwner(
        ipProtocol: Int,
        sourceAddress: String,
        sourcePort: Int,
        destinationAddress: String,
        destinationPort: Int,
    ): ConnectionOwner {
        check(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            "Поиск владельца соединения доступен на Android 10+"
        }
        val uid = connectivity.getConnectionOwnerUid(
            ipProtocol,
            InetSocketAddress(sourceAddress, sourcePort),
            InetSocketAddress(destinationAddress, destinationPort),
        )
        check(uid != Process.INVALID_UID) { "Владелец соединения не найден" }
        return ConnectionOwner().apply {
            userId = uid
            userName = service.packageManager.getPackagesForUid(uid)?.firstOrNull() ?: ""
            setAndroidPackageNames(
                LibboxStringIterator(service.packageManager.getPackagesForUid(uid)?.toList() ?: emptyList()),
            )
        }
    }

    override fun startDefaultInterfaceMonitor(listener: InterfaceUpdateListener) = Unit
    override fun closeDefaultInterfaceMonitor(listener: InterfaceUpdateListener) = Unit

    override fun getInterfaces(): NetworkInterfaceIterator {
        val values = NetworkInterface.getNetworkInterfaces()?.toList().orEmpty().mapNotNull { network ->
            runCatching {
                LibboxNetworkInterface().apply {
                    name = network.name
                    index = network.index
                    mtu = network.mtu
                    flags = 0
                    type = Libbox.InterfaceTypeOther
                    addresses = LibboxStringIterator(network.interfaceAddresses.map { "${it.address.hostAddress}/${it.networkPrefixLength}" })
                    dnsServer = LibboxStringIterator(emptyList())
                    metered = false
                }
            }.getOrNull()
        }
        return object : NetworkInterfaceIterator {
            private val iterator = values.iterator()
            override fun hasNext(): Boolean = iterator.hasNext()
            override fun next(): LibboxNetworkInterface = iterator.next()
        }
    }

    override fun underNetworkExtension(): Boolean = false
    override fun includeAllNetworks(): Boolean = false
    override fun clearDNSCache() = Unit
    override fun readWIFIState(): WIFIState? = null
    override fun localDNSTransport(): LocalDNSTransport? = null
    override fun startNeighborMonitor(listener: NeighborUpdateListener) = Unit
    override fun closeNeighborMonitor(listener: NeighborUpdateListener) = Unit

    override fun usePlatformShell(): Boolean = false
    override fun checkPlatformShell() = Unit
    override fun openShellSession(
        user: PlatformUser,
        command: String,
        environ: StringIterator,
        term: String,
        rows: Int,
        cols: Int,
    ): ShellSession = error("Shell через libbox отключён в сетевом модуле")

    override fun readSystemSSHHostKey(): String = ""
    override fun lookupSFTPServer(): String = ""
    override fun tailscaleHostname(): String = ""
    override fun usePlatformBridge(): Boolean = false
    override fun createBridge(options: BridgeOptions): BridgeSession = error("Bridge не поддерживается без root")
    override fun lookupUser(username: String): PlatformUser = PlatformUser().apply {
        this.username = username
        uid = Process.myUid()
        gid = Process.myUid()
        homeDir = service.filesDir.absolutePath
        shell = "/system/bin/sh"
    }
    override fun registerMyInterface(name: String) = Unit
    override fun sendNotification(notification: io.nekohasekai.libbox.Notification) = Unit
}
