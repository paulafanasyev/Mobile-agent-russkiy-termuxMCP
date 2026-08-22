#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

fail() { echo "ОШИБКА: $*" >&2; exit 1; }

command -v node >/dev/null || fail "Node.js не найден"
command -v pnpm >/dev/null || fail "pnpm не найден"

node -e 'const fs=require("fs"); const p=JSON.parse(fs.readFileSync("app.json","utf8")); if(!p.expo || p.expo.name !== "Мобильный ИИ-агент") process.exit(1); console.log("OK: русское имя приложения")'

[ -f "plugins/with-libbox.js" ] || fail "Не найден config plugin libbox"
[ -f "scripts/build-libbox-android.sh" ] || fail "Не найден скрипт сборки libbox"

if [ -f "build/native/libbox.aar" ]; then
  [ -s "build/native/libbox.aar" ] || fail "libbox.aar пустой"
  echo "OK: найден локальный libbox.aar"
else
  echo "ПРЕДУПРЕЖДЕНИЕ: libbox.aar пока не собран; нативная сборка не считается готовой."
fi

pnpm exec expo config --type public >/tmp/mobile-agent-expo-config.json
node -e 'const fs=require("fs"); const p=JSON.parse(fs.readFileSync("/tmp/mobile-agent-expo-config.json","utf8")); const plugins=p.expo?.plugins||[]; if(!plugins.some(x=>x==="./plugins/with-libbox")) process.exit(1); console.log("OK: with-libbox подключён")'

echo "Проверка конфигурации завершена."
