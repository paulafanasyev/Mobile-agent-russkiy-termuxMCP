import type { FirewallProfile } from './policy'

export const FIREWALL_PROFILES: FirewallProfile[] = [
  {
    name: 'Обычный',
    defaultAction: 'allow',
    killSwitch: false,
    rules: [],
  },
  {
    name: 'Строгий',
    defaultAction: 'block',
    killSwitch: true,
    rules: [],
  },
  {
    name: 'Только разрешённые приложения',
    defaultAction: 'block',
    killSwitch: false,
    rules: [],
  },
]
