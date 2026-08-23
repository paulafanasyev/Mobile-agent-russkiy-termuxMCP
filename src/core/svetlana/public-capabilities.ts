import type { SvetlanaMode } from './modes';

export interface SvetlanaPublicCapabilities {
  voice: boolean;
  vision: boolean;
  documents: boolean;
  offline: boolean;
  advancedTools: boolean;
}

/** UI-facing capabilities. Concrete engines/models are deliberately omitted. */
export function getSvetlanaCapabilities(mode: SvetlanaMode): SvetlanaPublicCapabilities {
  if (mode === 'lite') {
    return {
      voice: true,
      vision: false,
      documents: true,
      offline: true,
      advancedTools: false,
    };
  }

  return {
    voice: true,
    vision: true,
    documents: true,
    offline: true,
    advancedTools: true,
  };
}
