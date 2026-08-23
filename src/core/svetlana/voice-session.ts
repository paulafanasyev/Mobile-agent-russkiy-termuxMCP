export type SvetlanaVoiceState = 'idle' | 'listening' | 'thinking' | 'speaking';

export interface SvetlanaVoiceSession {
  state: SvetlanaVoiceState;
  offlineCapable: true;
  transcript: string;
  response: string;
}

export function createVoiceSession(): SvetlanaVoiceSession {
  return { state: 'idle', offlineCapable: true, transcript: '', response: '' };
}

export function beginListening(session: SvetlanaVoiceSession): SvetlanaVoiceSession {
  return { ...session, state: 'listening' };
}

export function beginThinking(session: SvetlanaVoiceSession, transcript: string): SvetlanaVoiceSession {
  return { ...session, state: 'thinking', transcript };
}

export function beginSpeaking(session: SvetlanaVoiceSession, response: string): SvetlanaVoiceSession {
  return { ...session, state: 'speaking', response };
}

export function finishSpeaking(session: SvetlanaVoiceSession): SvetlanaVoiceSession {
  return { ...session, state: 'idle' };
}
