import type { DeviceProfile } from '../local-ai/types';
import type { SvetlanaMode } from './modes';
import type { AudioBackend } from './audio-engine';

export interface AudioModelPolicy {
  backend: AudioBackend;
  maxThreads: number;
  preferQuantized: true;
}

export function selectAudioPolicy(mode: SvetlanaMode, profile: DeviceProfile): AudioModelPolicy {
  const lowRam = mode === 'lite' || profile.tier === 'low' || profile.availableRamMb < 1800;
  return {
    backend: 'sherpa-onnx',
    maxThreads: lowRam ? 2 : 4,
    preferQuantized: true,
  };
}
