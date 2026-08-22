import { Platform } from 'react-native'
import { requireNativeModule } from 'expo'

export type FirewallMode = 'off' | 'allowlist' | 'blocklist'

export type FirewallStatus = {
  running: boolean
  mode: FirewallMode
  rules: string[]
}

type NativeFirewall = {
  start(mode: FirewallMode, packages: string[]): Promise<void>
  stop(): Promise<void>
  status(): Promise<FirewallStatus>
  setRules(mode: FirewallMode, packages: string[]): Promise<void>
}

const native = Platform.OS === 'android'
  ? requireNativeModule<NativeFirewall>('Firewall')
  : null

export async function startFirewall(
  mode: FirewallMode = 'blocklist',
  packages: string[] = [],
): Promise<void> {
  if (native) await native.start(mode, packages)
}

export async function stopFirewall(): Promise<void> {
  if (native) await native.stop()
}

export async function setFirewallRules(
  mode: FirewallMode,
  packages: string[],
): Promise<void> {
  if (native) await native.setRules(mode, packages)
}

export async function getFirewallStatus(): Promise<FirewallStatus> {
  if (native) return native.status()
  return { running: false, mode: 'off', rules: [] }
}
