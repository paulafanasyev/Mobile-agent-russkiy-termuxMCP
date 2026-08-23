import type { SvetlanaMode } from './modes';

export interface SvetlanaConversationTurn {
  role: 'user' | 'svetlana';
  text: string;
  offline: true;
}

export interface SvetlanaConversation {
  mode: SvetlanaMode;
  turns: SvetlanaConversationTurn[];
}

export function createConversation(mode: SvetlanaMode): SvetlanaConversation {
  return { mode, turns: [] };
}

export function addUserTurn(conversation: SvetlanaConversation, text: string): SvetlanaConversation {
  return { ...conversation, turns: [...conversation.turns, { role: 'user', text, offline: true }] };
}

export function addSvetlanaTurn(conversation: SvetlanaConversation, text: string): SvetlanaConversation {
  return { ...conversation, turns: [...conversation.turns, { role: 'svetlana', text, offline: true }] };
}
