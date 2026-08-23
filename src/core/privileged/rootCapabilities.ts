/**
 * Privileged capability layer for Svetlana v2.
 *
 * Android cannot grant root to an ordinary application. This module therefore
 * never attempts an exploit or silently escalates privileges. It detects an
 * already-authorized root/privileged bridge and exposes capabilities to the
 * agent runtime. Without root it falls back to normal Android/Termux APIs.
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
  method: 'su' | 'termux-privileged-bridge' | 'none'
  uid?: number
}

/**
 * Pure capability model. The native/Termux bridge supplies the actual probe.
 * Keeping the policy here prevents the LLM from inventing privileges.
 */
export function capabilitiesFromProbe(probe: RootProbeResult): PrivilegedCapabilities {
  if (!probe.available) {
    return {
      rootAvailable: false,
      shell: false,
      systemSettings: false,
      packageManagement: false,
      firewall: false,
      processControl: false,
      networkControl: false,
    }
  }

  return {
    rootAvailable: true,
    shell: true,
    systemSettings: true,
    packageManagement: true,
    firewall: true,
    processControl: true,
    networkControl: true,
  }
}

/** Commands are data, not executable shell text from the LLM. */
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
