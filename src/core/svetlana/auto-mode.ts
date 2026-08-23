import { selectSvetlanaMode, type SvetlanaMode } from './modes';
import type { DeviceProfile } from '../local-ai/types';

export interface SvetlanaAutoModeResult {
  mode: SvetlanaMode;
  firstRunMessage: string;
}

/** Public product behavior: users see only Svetlana modes, never engine/model names. */
export function chooseSvetlanaForDevice(profile: DeviceProfile): SvetlanaAutoModeResult {
  const mode = selectSvetlanaMode(profile);
  return {
    mode,
    firstRunMessage:
      mode === 'lite'
        ? 'Я определила оптимальный режим для вашего телефона — Светлана Лайт.'
        : 'Я определила оптимальный режим для вашего телефона — Светлана Фулл.',
  };
}

export function canOfferFullMode(profile: DeviceProfile): boolean {
  return profile.tier !== 'low' && profile.availableRamMb >= 1800;
}
