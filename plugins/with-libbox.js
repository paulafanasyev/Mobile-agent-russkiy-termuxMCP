const { withProjectBuildGradle, withAppBuildGradle } = require('@expo/config-plugins');
const fs = require('fs');
const path = require('path');

/**
 * Подключает воспроизводимо собранный build/native/libbox.aar к Android-проекту.
 * Сам бинарник не хранится в Git. Его создаёт scripts/build-libbox-android.sh.
 */
module.exports = function withLibbox(config) {
  config = withProjectBuildGradle(config, (cfg) => {
    const marker = "// MOBILE_AGENT_LIBBOX_REPOSITORY";
    if (!cfg.modResults.contents.includes(marker)) {
      cfg.modResults.contents += `\n${marker}\n` +
        `allprojects { repositories { flatDir { dirs(\"$rootDir/../build/native\") } } }\n`;
    }
    return cfg;
  });

  config = withAppBuildGradle(config, (cfg) => {
    const marker = "// MOBILE_AGENT_LIBBOX_DEPENDENCY";
    if (!cfg.modResults.contents.includes(marker)) {
      cfg.modResults.contents += `\n${marker}\n` +
        `dependencies { implementation(name: \"libbox\", ext: \"aar\") }\n`;
    }
    return cfg;
  });

  return config;
};
