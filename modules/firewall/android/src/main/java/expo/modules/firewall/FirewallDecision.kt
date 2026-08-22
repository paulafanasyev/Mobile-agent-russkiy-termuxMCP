package expo.modules.firewall

enum class FirewallAction { ALLOW, BLOCK, LOG }

data class FirewallRule(
  val action: FirewallAction,
  val protocol: Int? = null,
  val destinationPort: Int? = null,
  val destinationHost: String? = null,
  val destinationCidr: String? = null,
)

/** Детерминированная проверка правил без сетевых побочных эффектов. */
object FirewallDecision {
  fun decide(packet: PacketInfo, rules: List<FirewallRule>): FirewallAction {
    for (rule in rules) {
      if (rule.protocol != null && rule.protocol != packet.protocol) continue
      if (rule.destinationPort != null && rule.destinationPort != packet.destinationPort) continue
      if (rule.destinationHost != null && !hostMatches(packet.destinationAddress, rule.destinationHost)) continue
      if (rule.destinationCidr != null && !cidrMatches(packet.destinationAddress, rule.destinationCidr)) continue
      return rule.action
    }
    return FirewallAction.ALLOW
  }

  private fun hostMatches(address: String, host: String): Boolean =
    address.equals(host, ignoreCase = true)

  private fun cidrMatches(address: String, cidr: String): Boolean {
    val parts = cidr.split('/', limit = 2)
    if (parts.size != 2 || address.contains(':') || parts[0].contains(':')) return false
    val prefix = parts[1].toIntOrNull() ?: return false
    if (prefix !in 0..32) return false
    val target = ipv4ToLong(address) ?: return false
    val network = ipv4ToLong(parts[0]) ?: return false
    val mask = if (prefix == 0) 0L else (0xffffffffL shl (32 - prefix)) and 0xffffffffL
    return (target and mask) == (network and mask)
  }

  private fun ipv4ToLong(value: String): Long? {
    val octets = value.split('.')
    if (octets.size != 4) return null
    var result = 0L
    for (octet in octets) {
      val n = octet.toIntOrNull() ?: return null
      if (n !in 0..255) return null
      result = (result shl 8) or n.toLong()
    }
    return result
  }
}
