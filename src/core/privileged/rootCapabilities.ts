/**
 * Привилегированный слой возможностей Светланы.
 * Android не может выдать root обычному приложению, поэтому этот слой
 * никогда не выполняет эксплойты и не пытается скрытно повышать права.
 */

export type PrivilegedCapabilities = {
  rootAvailable: boolean
  shell: boolean
  systemSettings: boolean
  packageManagement: boolean
  firewall: boolean
  processControl: boolean
  networkControl: boolean
}

export type RootProbeResult = {
  available: boolean
  authorized: boolean
  method: 'su' | 'termux-privileged-bridge' | 'none'
  uid?: number | null
}

/** Наличие su НЕ означает наличие root. Нужен успешный uid=0. */
export function capabilitiesFromProbe(probe: RootProbeResult): PrivilegedCapabilities {
  const privileged = probe.available && probe.authorized && probe.uid === 0
  return {
    rootAvailable: privileged,
    shell: privileged,
    systemSettings: privileged,
    packageManagement: privileged,
    firewall: privileged,
    processControl: privileged,
    networkControl: privileged,
  }
}

/** Операции представлены как данные; LLM не получает произвольную shell-строку. */
export const PRIVILEGED_OPERATIONS = [
  'inspect_device',
  'inspect_processes',
  'start_process',
  'stop_process',
  'install_package',
  'remove_package',
  'inspect_ports',
  'set_firewall_rule',
  'inspect_network',
  'read_system_log',
] as const

export type PrivilegedOperation = (typeof PRIVILEGED_OPERATIONS)[number]
