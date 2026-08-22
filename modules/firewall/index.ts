import { Platform } from 'react-native'
import { requireNativeModule } from 'expo'

/**
 * Первый безопасный этап фаервола: строгий список разрешённых приложений.
 * Режим blocklist будет добавлен после появления полноценного packet-processing
 * backend, чтобы не создавать ложного ощущения блокировки.
 */
export type FirewallMode = 'off' | 'allowlist'

export type FirewallStatus = {
  running: boolean
  mode: FirewallMode
  rules: string[]
}

type NativeFirewall = {
  prepare(): Promise<boolean>
  start(mode: FirewallMode, packages: string[]): Promise<void>
  stop(): Promise<void>
  status(): Promise<FirewallStatus>
  setRules(mode: FirewallMode, packages: string[]): Promise<void>
}

const native = Platform.OS === 'android'
  ? requireNativeModule<NativeFirewall>('Firewall')
  : null

/** Открывает системный диалог разрешения VPN. */
export async function prepareFirewall(): Promise<boolean> {
  if (native) return native.prepare()
  return true
}

export async function startFirewall(
  mode: FirewallMode = 'allowlist',
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
