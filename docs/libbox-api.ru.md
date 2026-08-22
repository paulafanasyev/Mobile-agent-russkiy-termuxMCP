# Реальный API libbox в проекте

## Подтверждённый артефакт

Workflow `Воспроизводимая сборка libbox Android` успешно собрал `libbox.aar` для ARM64 из закреплённого commit:

`670d7a7693918b765a17c44aee5afb5d47ead390`

Артефакт был разобран перед интеграцией. В `classes.jar` присутствуют `io.nekohasekai.libbox.CommandServer`, `PlatformInterface`, `TunOptions`, `OverrideOptions` и другие актуальные классы.

Текущий AAR интегрируется через:

`Android VpnService → PlatformInterface.openTun() → CommandServer.startOrReloadService() → libbox`

Старый `BoxService` API в нашем bridge не используется.

## Жизненный цикл

1. Android предоставляет разрешение VPN.
2. `FirewallVpnService` запускается как foreground service.
3. `LibboxForwardingBridge` вызывает `Libbox.setup()` и `Libbox.setLocale("ru")`.
4. Создаётся `CommandServer` с Android `PlatformInterface`.
5. `CommandServer.start()` запускает управляющий слой.
6. `startOrReloadService()` загружает конфигурацию sing-box.
7. При создании TUN libbox вызывает `PlatformInterface.openTun()`.
8. Android `VpnService.Builder.establish()` создаёт TUN-дескриптор, который передаётся обратно libbox.
9. Состояние фаервола становится `running=true` только после успешного запуска сервиса.
10. При остановке сначала закрывается сервис libbox, затем освобождаются Android-ресурсы.

## Текущий режим правил

`allowlist` сейчас означает **список приложений, которые отправляются через VPN/libbox**. Приложения вне списка не добавляются в Android VPN и продолжают использовать обычную сетевую конфигурацию устройства.

Это намеренно не называется «строгой блокировкой всего остального». Строгий fail-closed режим будет отдельным режимом с маршрутизацией `package_name → direct` и `final → block` после отдельного runtime-тестирования, потому что ошибка в правилах может отключить системные сервисы и сеть устройства.

## Безопасность

- `libbox.aar` не хранится в Git.
- Версия исходника закреплена SHA.
- SHA256 проверяется в CI.
- Не используется произвольный shell-доступ через сетевой модуль.
- Фаервол не объявляется работающим при ошибке инициализации libbox.
- Текущий режим ограничен `allowlist`; более агрессивные режимы добавляются отдельно.

## Runtime-проверка

Наличие AAR и успешная компиляция не доказывают работу VPN на конкретном телефоне. Финальная проверка требует установки APK на Android и проверки:

- выдачи VPN-разрешения;
- запуска и остановки TUN;
- реального сетевого трафика;
- DNS;
- маршрутизации;
- поведения allowlist приложений;
- восстановления после ошибки и перезагрузки устройства.
