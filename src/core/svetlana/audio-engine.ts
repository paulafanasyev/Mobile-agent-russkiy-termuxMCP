export type AudioBackend = 'sherpa-onnx' | 'whisper.cpp';

export interface SvetlanaAudioEngineConfig {
  backend: AudioBackend;
  language: 'ru-RU';
  offline: true;
  vad: true;
  tts: true;
}

export const DEFAULT_AUDIO_CONFIG: SvetlanaAudioEngineConfig = {
  backend: 'sherpa-onnx',
  language: 'ru-RU',
  offline: true,
  vad: true,
  tts: true,
};

/** Public audio contract. Native implementation stays hidden from the UI. */
export function createAudioConfig(backend: AudioBackend = 'sherpa-onnx'): SvetlanaAudioEngineConfig {
  return { ...DEFAULT_AUDIO_CONFIG, backend };
}
