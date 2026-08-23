import { getMemoryPolicy } from './memory-policy';
import { rankModelsForDevice } from './model-catalog';
import type { DeviceProfile, LocalModelSpec } from './types';

export interface ModelSelection {
  model: LocalModelSpec | null;
  reason: string;
}

/** Select the largest safe model that fits the current runtime memory budget. */
export function selectLocalModel(profile: DeviceProfile): ModelSelection {
  const policy = getMemoryPolicy(profile);
  const candidates = rankModelsForDevice(profile)
    .filter((model) => model.estimatedRuntimeRamMb <= policy.maxModelRamMb);

  if (candidates.length === 0) {
    return {
      model: null,
      reason: 'Недостаточно доступной RAM для безопасной локальной модели.',
    };
  }

  return {
    model: candidates[0],
    reason: `Автовыбор: ${candidates[0].name}; лимит RAM ${policy.maxModelRamMb} MB, контекст до ${policy.maxContextTokens} токенов.`,
  };
}
