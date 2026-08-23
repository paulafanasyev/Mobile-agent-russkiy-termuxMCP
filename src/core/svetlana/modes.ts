import type { DeviceProfile } from '../local-ai/types';

/** Public product modes. Concrete engines/models stay internal. */
export type SvetlanaMode = 'lite' | 'full';

export interface SvetlanaModeInfo {
  id: SvetlanaMode;
  title: string;
  description: string;
}

export const SVETLANA_MODES: Record<SvetlanaMode, SvetlanaModeInfo> = {
  lite: {
    id: 'lite',
    title: 'Светлана Лайт',
    description: 'Быстрая и экономичная Светлана для слабых телефонов.',
  },
  full: {
    id: 'full',
    title: 'Светлана Фулл',
    description: 'Полная версия Светланы с расширенными возможностями.',
  },
};

export function selectSvetlanaMode(profile: DeviceProfile): SvetlanaMode {
  if (profile.tier === 'low' || profile.availableRamMb < 1800) return 'lite';
  return 'full';
}

export function getSvetlanaModeInfo(mode: SvetlanaMode): SvetlanaModeInfo {
  return SVETLANA_MODES[mode];
}
