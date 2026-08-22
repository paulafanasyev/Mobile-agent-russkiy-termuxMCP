# Воспроизводимая сборка libbox.aar

`libbox.aar` не хранится в Git. Он является генерируемым нативным артефактом и каждый раз собирается из закреплённого исходного commit `sing-box`.

Закреплённый revision:

`670d7a7693918b765a17c44aee5afb5d47ead390`

Проверенный commit относится к ветке `testing` sing-box. Сам sing-box использует `cmd/internal/build_libbox` и `gomobile bind` для генерации Android AAR; официальный build-скрипт также формирует `libbox.aar` и legacy-вариант. urlИсходный build_libbox sing-boxhttps://github.com/SagerNet/sing-box/blob/testing/cmd/internal/build_libbox/main.go

## Требования

- Git
- Go
- OpenJDK 17
- Android SDK
- Android NDK
- `ANDROID_HOME` или `ANDROID_SDK_ROOT`

## Одна команда в Termux/Linux

Из корня репозитория:

```bash
bash scripts/build-libbox-android.sh
```

Результат:

- `build/native/libbox.aar`
- `build/native/sing-box-revision.txt`
- `build/native/libbox.aar.sha256`

AAR намеренно не коммитится в Git. Это позволяет получать один и тот же бинарный артефакт из одного и того же исходного revision и независимо проверять его в CI.

## Проверка

```bash
cat build/native/sing-box-revision.txt
cat build/native/libbox.aar.sha256
sha256sum -c build/native/libbox.aar.sha256
```

После успешной сборки AAR можно подключать к сгенерированному Android-проекту приложения. Сам firewall не должен переходить в состояние `RUNNING`, пока реальный `libbox` не найден и не инициализирован.
