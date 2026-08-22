package expo.modules.firewall

/**
 * Разбор IP-пакетов, поступающих из TUN-интерфейса Android VpnService.
 *
 * Это только детерминированный инспектор: он не открывает сокеты и не
 * отправляет трафик в интернет. Поэтому его можно безопасно использовать
 * для анализа и журналирования до подключения полноценного forwarding backend.
 */
data class PacketInfo(
  val version: Int,
  val protocol: Int,
  val sourceAddress: String,
  val destinationAddress: String,
  val sourcePort: Int? = null,
  val destinationPort: Int? = null,
  val payloadLength: Int = 0,
)

object PacketInspector {
  fun inspect(packet: ByteArray, length: Int = packet.size): PacketInfo? {
    if (length < 20) return null
    val version = (packet[0].toInt() ushr 4) and 0x0f
    return when (version) {
      4 -> inspectIpv4(packet, length)
      6 -> inspectIpv6(packet, length)
      else -> null
    }
  }

  private fun inspectIpv4(packet: ByteArray, length: Int): PacketInfo? {
    val ihl = (packet[0].toInt() and 0x0f) * 4
    if (ihl < 20 || length < ihl) return null
    val protocol = packet[9].toInt() and 0xff
    val source = ipv4(packet, 12)
    val destination = ipv4(packet, 16)
    val totalLength = u16(packet, 2).coerceAtMost(length)
    val transport = transportPorts(packet, ihl, totalLength, protocol)
    return PacketInfo(4, protocol, source, destination, transport?.first, transport?.second, (totalLength - ihl).coerceAtLeast(0))
  }

  private fun inspectIpv6(packet: ByteArray, length: Int): PacketInfo? {
    if (length < 40) return null
    val protocol = packet[6].toInt() and 0xff
    val source = ipv6(packet, 8)
    val destination = ipv6(packet, 24)
    val payloadLength = u16(packet, 4).coerceAtMost(length - 40)
    val transport = transportPorts(packet, 40, length, protocol)
    return PacketInfo(6, protocol, source, destination, transport?.first, transport?.second, payloadLength)
  }

  private fun transportPorts(packet: ByteArray, offset: Int, length: Int, protocol: Int): Pair<Int, Int>? {
    if ((protocol != 6 && protocol != 17) || length < offset + 4) return null
    return u16(packet, offset) to u16(packet, offset + 2)
  }

  private fun u16(packet: ByteArray, offset: Int): Int =
    ((packet[offset].toInt() and 0xff) shl 8) or (packet[offset + 1].toInt() and 0xff)

  private fun ipv4(packet: ByteArray, offset: Int): String =
    (0 until 4).joinToString(".") { (packet[offset + it].toInt() and 0xff).toString() }

  private fun ipv6(packet: ByteArray, offset: Int): String =
    (0 until 8).joinToString(":") { i ->
      "%02x%02x".format(packet[offset + i * 2].toInt() and 0xff, packet[offset + i * 2 + 1].toInt() and 0xff)
    }
}
