import type { DeviceProfile } from '../local-ai/types';
import { createSvetlanaLaunchPlan } from './launch-plan';
import { DEFAULT_SVETLANA_APPEARANCE, getSvetlanaAppearance, type SvetlanaAppearanceId } from './appearance';

export interface SvetlanaMainScreenModel {
  assistantName: 'Светлана';
  mode: 'lite' | 'full';
  modeTitle: 'Светлана Лайт' | 'Светлана Фулл';
  appearance: ReturnType<typeof getSvetlanaAppearance>;
  firstRunMessage: string;
  greeting: string;
  status: 'ready' | 'listening' | 'thinking' | 'speaking';
  offlineFirst: true;
  capabilities: ReturnType<typeof createSvetlanaLaunchPlan>['capabilities'];
  allowFullModeSelection: boolean;
}

export function createSvetlanaMainScreen(
  profile: DeviceProfile,
  appearanceId: SvetlanaAppearanceId = DEFAULT_SVETLANA_APPEARANCE,
): SvetlanaMainScreenModel {
  const launch = createSvetlanaLaunchPlan(profile);
  return {
    assistantName: 'Светлана',
    mode: launch.mode,
    modeTitle: launch.mode === 'lite' ? 'Светлана Лайт' : 'Светлана Фулл',
    appearance: getSvetlanaAppearance(appearanceId),
    firstRunMessage: launch.firstRunMessage,
    greeting: 'Здравствуйте! Я Светлана. Чем могу помочь?',
    status: 'ready',
    offlineFirst: true,
    capabilities: launch.capabilities,
    allowFullModeSelection: launch.allowManualFullSelection,
  };
}
