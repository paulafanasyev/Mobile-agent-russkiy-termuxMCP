const { withAppBuildGradle } = require('@expo/config-plugins');

/**
 * Подключает воспроизводимо собранный build/native/libbox.aar к Android-проекту.
 * Сам бинарник не хранится в Git. Его создаёт scripts/build-libbox-android.sh.
 *
 * Используем прямую file dependency вместо Gradle flatDir + implementation(name, ext),
 * чтобы источник AAR был определённым при разрешении compileDebugJavaWithJavac.
 */
module.exports = function withLibbox(config) {
  config = withAppBuildGradle(config, (cfg) => {
    const marker = "// MOBILE_AGENT_LIBBOX_DEPENDENCY";
    if (!cfg.modResults.contents.includes(marker)) {
      cfg.modResults.contents += `\n${marker}\n` +
        `dependencies { implementation(files("$rootDir/../build/native/libbox.aar")) }\n`;
    }
    return cfg;
  });

  return config;
};
