export type SvetlanaAppearanceId = 'classic' | 'modern' | 'business' | 'friendly';

export interface SvetlanaAppearance {
  id: SvetlanaAppearanceId;
  title: string;
  description: string;
  assetKey: string;
}

/** Appearance is a user-facing profile, independent from AI mode/backend. */
export const SVETLANA_APPEARANCES: SvetlanaAppearance[] = [
  { id: 'classic', title: 'Классическая', description: 'Основной образ Светланы.', assetKey: 'svetlana-classic' },
  { id: 'modern', title: 'Современная', description: 'Более современный визуальный образ.', assetKey: 'svetlana-modern' },
  { id: 'business', title: 'Деловая', description: 'Строгий и профессиональный образ.', assetKey: 'svetlana-business' },
  { id: 'friendly', title: 'Дружелюбная', description: 'Тёплый и неформальный образ.', assetKey: 'svetlana-friendly' },
];

export const DEFAULT_SVETLANA_APPEARANCE: SvetlanaAppearanceId = 'classic';

export function getSvetlanaAppearance(id?: SvetlanaAppearanceId): SvetlanaAppearance {
  return SVETLANA_APPEARANCES.find((item) => item.id === id) ?? SVETLANA_APPEARANCES[0];
}
