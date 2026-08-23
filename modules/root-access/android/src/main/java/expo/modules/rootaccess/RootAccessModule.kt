package expo.modules.rootaccess

import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import java.util.concurrent.TimeUnit

class RootAccessModule : Module() {
  override fun definition() = ModuleDefinition {
    Name("RootAccess")

    AsyncFunction("status") {
      checkRoot()
    }

    AsyncFunction("requestAuthorization") {
      // На рутованном устройстве su/root-менеджер сам решает, выдавать ли
      // приложению уже существующие привилегии. Мы не обходим защиту Android.
      checkRoot()
    }
  }

  private fun checkRoot(): Map<String, Any?> {
    if (android.os.Build.VERSION.SDK_INT < 24) {
      return mapOf(
        "supported" to false,
        "available" to false,
        "authorized" to false,
        "uid" to null,
        "source" to "none",
        "message" to "Эта функция требует Android 7.0 или новее."
      )
    }

    return try {
      val process = ProcessBuilder("su", "-c", "id")
        .redirectErrorStream(true)
        .start()

      val completed = process.waitFor(8, TimeUnit.SECONDS)
      if (!completed) {
        process.destroyForcibly()
        return mapOf(
          "supported" to true,
          "available" to true,
          "authorized" to false,
          "uid" to null,
          "source" to "su",
          "message" to "su найден, но подтверждение root не завершилось вовремя."
        )
      }

      val output = process.inputStream.bufferedReader().use { it.readText() }.trim()
      val exitCode = process.exitValue()
      val uid = Regex("uid=(\\d+)").find(output)?.groupValues?.get(1)?.toIntOrNull()
      val authorized = exitCode == 0 && uid == 0

      mapOf(
        "supported" to true,
        "available" to true,
        "authorized" to authorized,
        "uid" to uid,
        "source" to "su",
        "message" to if (authorized) {
          "Root уже разрешён для приложения."
        } else {
          "su доступен, но root для приложения не разрешён."
        }
      )
    } catch (_: java.io.IOException) {
      mapOf(
        "supported" to true,
        "available" to false,
        "authorized" to false,
        "uid" to null,
        "source" to "none",
        "message" to "su не найден. Устройство не предоставляет приложению root-доступ."
      )
    } catch (e: Exception) {
      mapOf(
        "supported" to true,
        "available" to false,
        "authorized" to false,
        "uid" to null,
        "source" to "none",
        "message" to "Не удалось проверить root: ${e.javaClass.simpleName}."
      )
    }
  }
}
