export type LocalAiBackend = 'llama.cpp' | 'litert' | 'executorch' | 'none';

export type DeviceTier = 'low' | 'standard' | 'high';

export interface DeviceProfile {
  totalRamMb: number;
  availableRamMb: number;
  cpuCores: number;
  architecture: 'arm64-v8a' | 'armeabi-v7a' | 'x86_64' | 'unknown';
  hasGpuAcceleration: boolean;
  tier: DeviceTier;
}

export interface LocalModelSpec {
  id: string;
  name: string;
  backend: Exclude<LocalAiBackend, 'none'>;
  parametersB: number;
  quantization?: string;
  estimatedFileMb: number;
  estimatedRuntimeRamMb: number;
  multimodal?: boolean;
  recommendedFor: DeviceTier[];
}

export interface LocalAiRequest {
  prompt: string;
  systemPrompt?: string;
  maxTokens?: number;
  contextTokens?: number;
  temperature?: number;
}

export interface LocalAiResponse {
  text: string;
  backend: Exclude<LocalAiBackend, 'none'>;
  modelId: string;
  tokensPerSecond?: number;
}

export interface LocalAiEngine {
  readonly backend: Exclude<LocalAiBackend, 'none'>;
  isAvailable(): Promise<boolean>;
  load(model: LocalModelSpec): Promise<void>;
  unload(): Promise<void>;
  generate(request: LocalAiRequest): Promise<LocalAiResponse>;
}
