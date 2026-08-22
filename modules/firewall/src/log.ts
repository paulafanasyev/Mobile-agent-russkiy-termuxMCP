export type FirewallEvent = {
  timestamp: number
  action: 'allow' | 'block'
  packageName?: string
  domain?: string
  address?: string
  port?: number
  protocol?: string
  reason: string
}

const MAX_EVENTS = 1000
const events: FirewallEvent[] = []

export function recordFirewallEvent(event: FirewallEvent): void {
  events.push(event)
  if (events.length > MAX_EVENTS) events.splice(0, events.length - MAX_EVENTS)
}

export function getFirewallEvents(limit = 100): FirewallEvent[] {
  return events.slice(Math.max(0, events.length - limit)).reverse()
}

export function clearFirewallEvents(): void {
  events.length = 0
}
