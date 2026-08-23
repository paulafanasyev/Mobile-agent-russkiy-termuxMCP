import { selectLocalModel } from './model-selector';
import type { DeviceProfile } from './types';

export interface SvetlanaLocalRuntimePlan {
  modelId: string | null;
  backend: string | null;
  offline: true;
  safeForDevice: boolean;
  reason: string;
}

/**
 * Svetlana remains the user-facing assistant; local AI is only her inference
 * engine. UI, memory, tools, MCP and permissions must not depend on a model.
 */
export function planSvetlanaLocalRuntime(profile: DeviceProfile): SvetlanaLocalRuntimePlan {
  const selection = selectLocalModel(profile);
  if (!selection.model) {
    return {
      modelId: null,
      backend: null,
      offline: true,
      safeForDevice: false,
      reason: selection.reason,
    };
  }

  return {
    modelId: selection.model.id,
    backend: selection.model.backend,
    offline: true,
    safeForDevice: true,
    reason: selection.reason,
  };
}
