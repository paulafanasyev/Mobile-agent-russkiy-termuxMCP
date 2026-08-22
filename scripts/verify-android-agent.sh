#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

fail() { echo "ОШИБКА: $*" >&2; exit 1; }

command -v node >/dev/null || fail "Node.js не найден"

node - <<'NODE'
const fs = require('fs')
const app = JSON.parse(fs.readFileSync('app.json', 'utf8'))
if (!app.expo || app.expo.name !== 'Мобильный ИИ-агент') process.exit(1)
const plugins = app.expo.plugins || []
if (!plugins.some((item) => item === './plugins/with-libbox')) process.exit(2)
console.log('OK: русское имя приложения')
console.log('OK: with-libbox подключён')
NODE

[ -f "plugins/with-libbox.js" ] || fail "Не найден config plugin libbox"
[ -f "scripts/build-libbox-android.sh" ] || fail "Не найден скрипт сборки libbox"
[ -f "modules/firewall/src/main/java/com/mobileshell/firewall/LibboxForwardingBridge.kt" ] || fail "Не найден реальный libbox bridge"
[ -f "modules/firewall/src/main/java/com/mobileshell/firewall/LibboxAndroidPlatform.kt" ] || fail "Не найден Android PlatformInterface"
[ -f "modules/firewall/android/src/main/java/expo/modules/firewall/FirewallVpnService.kt" ] || fail "Не найден Android VpnService"

if [ -f "build/native/libbox.aar" ]; then
  [ -s "build/native/libbox.aar" ] || fail "libbox.aar пустой"
  echo "OK: найден локальный libbox.aar"

  if command -v jar >/dev/null; then
    if ! unzip -p build/native/libbox.aar classes.jar > /tmp/mobile-agent-libbox-classes.jar; then
      fail "Не удалось извлечь classes.jar из libbox.aar"
    fi
    jar tf /tmp/mobile-agent-libbox-classes.jar | grep -q 'io/nekohasekai/libbox/CommandServer.class' \
      || fail "AAR не содержит реальный CommandServer API"
    if jar tf /tmp/mobile-agent-libbox-classes.jar | grep -q 'io/nekohasekai/libbox/BoxService.class'; then
      echo "ПРЕДУПРЕЖДЕНИЕ: AAR содержит старый BoxService API; текущий bridge использует CommandServer."
    fi
    echo "OK: AAR содержит CommandServer API"
  fi
else
  echo "ПРЕДУПРЕЖДЕНИЕ: libbox.aar пока не собран; native runtime не считается проверенным."
fi

grep -R "BoxService" modules/firewall/src/main/java modules/firewall/android/src/main/java >/dev/null 2>&1 \
  && fail "В нашем Android-коде осталась ссылка на устаревший BoxService API"

echo "Проверка конфигурации и libbox bridge завершена."
