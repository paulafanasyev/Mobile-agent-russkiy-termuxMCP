import type { LocalAiBackend } from './types';

export interface BackendCapabilities {
  backend: LocalAiBackend;
  native: boolean;
  preferredForLowRam: boolean;
  supportsGpu: boolean;
}

/** Runtime preference order. Native integrations are enabled only when present. */
export const BACKEND_PREFERENCE: BackendCapabilities[] = [
  { backend: 'litert', native: true, preferredForLowRam: true, supportsGpu: true },
  { backend: 'llama.cpp', native: true, preferredForLowRam: true, supportsGpu: true },
  { backend: 'mnn', native: true, preferredForLowRam: true, supportsGpu: true },
  { backend: 'executorch', native: true, preferredForLowRam: false, supportsGpu: true },
];

export function preferredBackends(lowRam: boolean): LocalAiBackend[] {
  return BACKEND_PREFERENCE
    .filter((item) => !lowRam || item.preferredForLowRam)
    .map((item) => item.backend);
}
