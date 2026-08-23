export const APP_INFO = {
  productName: 'Светлана',
  developer: 'Афанасьев Павел',
  contactEmail: 'xongphavietnam@gmail.com',
  contacts: ['+84843012046', '+7 914 828-99-64'],
  sections: {
    about: 'О приложении',
    cooperation: 'Сотрудничество',
    support: 'Поддержка',
    license: 'Лицензия и условия использования',
    privacy: 'Политика конфиденциальности',
  },
} as const;

export function getAppInfo() {
  return APP_INFO;
}
