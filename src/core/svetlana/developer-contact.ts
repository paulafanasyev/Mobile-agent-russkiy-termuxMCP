export const SVETLANA_DEVELOPER_CONTACT = {
  developer: 'Афанасьев Павел',
  purpose: 'Разработка, коммерческое сотрудничество и вопросы по приложению Светлана.',
  phones: [
    '+84843012046',
    '+7 914 828-99-64',
  ],
  email: 'xongphavietnam@gmail.com',
} as const;

/** Public contact card for the app's About / Cooperation section. */
export function getSvetlanaDeveloperContact() {
  return SVETLANA_DEVELOPER_CONTACT;
}
