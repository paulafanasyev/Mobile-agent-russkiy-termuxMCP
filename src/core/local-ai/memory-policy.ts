import type { DeviceProfile, DeviceTier } from './types';

export interface MemoryPolicy {
  maxModelRamMb: number;
  maxContextTokens: number;
  maxThreads: number;
  allowGpu: boolean;
  unloadBackgroundAppsHint: boolean;
}

/** Conservative inference budgets. They are intentionally not based on total RAM alone. */
export function getMemoryPolicy(profile: DeviceProfile): MemoryPolicy {
  const available = Math.max(0, profile.availableRamMb);

  if (profile.tier === 'low') {
    return {
      maxModelRamMb: Math.min(1800, Math.floor(available * 0.55)),
      maxContextTokens: 2048,
      maxThreads: Math.min(4, Math.max(2, profile.cpuCores)),
      allowGpu: profile.hasGpuAcceleration,
      unloadBackgroundAppsHint: true,
    };
  }

  if (profile.tier === 'standard') {
    return {
      maxModelRamMb: Math.min(3200, Math.floor(available * 0.65)),
      maxContextTokens: 4096,
      maxThreads: Math.min(6, Math.max(2, profile.cpuCores)),
      allowGpu: profile.hasGpuAcceleration,
      unloadBackgroundAppsHint: false,
    };
  }

  return {
    maxModelRamMb: Math.min(6144, Math.floor(available * 0.70)),
    maxContextTokens: 8192,
    maxThreads: Math.min(8, Math.max(2, profile.cpuCores)),
    allowGpu: profile.hasGpuAcceleration,
    unloadBackgroundAppsHint: false,
  };
}

export function canLoadModel(
  profile: DeviceProfile,
  estimatedRuntimeRamMb: number,
): boolean {
  return estimatedRuntimeRamMb <= getMemoryPolicy(profile).maxModelRamMb;
}

export function tierForRam(totalRamMb: number): DeviceTier {
  if (totalRamMb < 6144) return 'low';
  if (totalRamMb < 10240) return 'standard';
  return 'high';
}
