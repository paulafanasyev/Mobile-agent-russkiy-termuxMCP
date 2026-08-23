const { withProjectBuildGradle, withAppBuildGradle } = require('@expo/config-plugins');

/**
 * Подключает воспроизводимо собранный libbox как Maven-модуль.
 *
 * Важно: не используем implementation(files(...)) и flatDir.
 * Gradle 9.x может падать на LocalFileDependencyBackedArtifactSet при
 * разрешении compileDebugJavaWithJavac. Maven-координаты дают Gradle
 * нормальную module dependency и устраняют этот класс ошибок.
 */
module.exports = function withLibbox(config) {
  config = withProjectBuildGradle(config, (cfg) => {
    const marker = '// MOBILE_AGENT_LIBBOX_MAVEN_REPOSITORY';
    if (!cfg.modResults.contents.includes(marker)) {
      cfg.modResults.contents += `\n${marker}\n` +
        `allprojects { repositories { maven { url = uri("$rootDir/../build/native/maven") } } }\n`;
    }
    return cfg;
  });

  config = withAppBuildGradle(config, (cfg) => {
    const marker = '// MOBILE_AGENT_LIBBOX_MAVEN_DEPENDENCY';
    if (!cfg.modResults.contents.includes(marker)) {
      cfg.modResults.contents += `\n${marker}\n` +
        `dependencies { implementation("ru.mirsamozanyatykh:libbox:1.0.0") }\n`;
    }
    return cfg;
  });

  return config;
};
