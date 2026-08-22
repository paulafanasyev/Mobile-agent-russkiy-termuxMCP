export type FirewallAction = 'allow' | 'block'

export type FirewallRule = {
  id: string
  action: FirewallAction
  packageName?: string
  domain?: string
  cidr?: string
  protocol?: 'tcp' | 'udp' | 'icmp' | 'any'
  port?: number
  enabled: boolean
}

export type FirewallProfile = {
  name: string
  defaultAction: FirewallAction
  rules: FirewallRule[]
  killSwitch: boolean
}

export function matchesDomain(domain: string, ruleDomain: string): boolean {
  const a = domain.trim().toLowerCase().replace(/^\.+/, '')
  const b = ruleDomain.trim().toLowerCase().replace(/^\.+/, '')
  return a === b || a.endsWith(`.${b}`)
}

export function decidePacket(
  input: { packageName?: string; domain?: string; protocol?: FirewallRule['protocol']; port?: number },
  profile: FirewallProfile,
): FirewallAction {
  for (const rule of profile.rules) {
    if (!rule.enabled) continue
    if (rule.packageName && rule.packageName !== input.packageName) continue
    if (rule.domain && (!input.domain || !matchesDomain(input.domain, rule.domain))) continue
    if (rule.protocol && rule.protocol !== 'any' && rule.protocol !== input.protocol) continue
    if (rule.port && rule.port !== input.port) continue
    return rule.action
  }
  return profile.defaultAction
}
