import type { DeviceProfile } from '../local-ai/types';
import { chooseSvetlanaForDevice, canOfferFullMode } from './auto-mode';
import { getSvetlanaCapabilities } from './public-capabilities';

export interface SvetlanaLaunchPlan {
  mode: 'lite' | 'full';
  firstRunMessage: string;
  offlineFirst: true;
  capabilities: ReturnType<typeof getSvetlanaCapabilities>;
  allowManualFullSelection: boolean;
}

/** Single public launch contract for the main Svetlana screen. */
export function createSvetlanaLaunchPlan(profile: DeviceProfile): SvetlanaLaunchPlan {
  const selection = chooseSvetlanaForDevice(profile);
  return {
    mode: selection.mode,
    firstRunMessage: selection.firstRunMessage,
    offlineFirst: true,
    capabilities: getSvetlanaCapabilities(selection.mode),
    allowManualFullSelection: canOfferFullMode(profile),
  };
}
