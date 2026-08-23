import type { DeviceProfile, DeviceTier, LocalModelSpec } from './types';

/**
 * Conservative defaults for phones with limited RAM.
 * Runtime memory is intentionally estimated above the weight file size because
 * KV cache, tokenizer state and native buffers also consume RAM.
 */
export const LOCAL_MODEL_CATALOG: LocalModelSpec[] = [
  {
    id: 'qwen2.5-1.5b-q4',
    name: 'Qwen 2.5 1.5B Q4',
    backend: 'llama.cpp',
    parametersB: 1.5,
    quantization: 'Q4',
    estimatedFileMb: 1100,
    estimatedRuntimeRamMb: 1800,
    recommendedFor: ['low', 'standard'],
  },
  {
    id: 'gemma-3-1b-it-q4',
    name: 'Gemma 3 1B IT Q4',
    backend: 'llama.cpp',
    parametersB: 1,
    quantization: 'Q4',
    estimatedFileMb: 800,
    estimatedRuntimeRamMb: 1500,
    recommendedFor: ['low', 'standard'],
  },
  {
    id: 'llama-3.2-3b-q4',
    name: 'Llama 3.2 3B Q4',
    backend: 'llama.cpp',
    parametersB: 3,
    quantization: 'Q4',
    estimatedFileMb: 2000,
    estimatedRuntimeRamMb: 3000,
    recommendedFor: ['standard', 'high'],
  },
  {
    id: 'gemma-lite-2b',
    name: 'Gemma Lite 2B',
    backend: 'litert',
    parametersB: 2,
    estimatedFileMb: 1600,
    estimatedRuntimeRamMb: 2500,
    multimodal: true,
    recommendedFor: ['standard', 'high'],
  },
];

export function classifyDevice(totalRamMb: number): DeviceTier {
  if (totalRamMb < 6144) return 'low';
  if (totalRamMb < 10240) return 'standard';
  return 'high';
}

export function rankModelsForDevice(profile: DeviceProfile): LocalModelSpec[] {
  const tier = profile.tier;
  return LOCAL_MODEL_CATALOG
    .filter((model) => model.recommendedFor.includes(tier))
    .filter((model) => model.estimatedRuntimeRamMb <= Math.max(1200, profile.availableRamMb * 0.65))
    .sort((a, b) => b.parametersB - a.parametersB);
}
