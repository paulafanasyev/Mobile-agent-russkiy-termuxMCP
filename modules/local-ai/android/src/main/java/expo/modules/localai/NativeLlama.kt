package expo.modules.localai

/**
 * Small JNI boundary. The native library is optional so the APK can still
 * start on devices where no compatible llama.cpp ABI is bundled.
 */
internal object NativeLlama {
  private var loaded = false
  private var modelPath: String? = null

  init {
    try {
      System.loadLibrary("mobile_agent_llama")
      loaded = true
    } catch (_: UnsatisfiedLinkError) {
      loaded = false
    }
  }

  fun isLoaded(): Boolean = loaded

  fun statusMessage(): String = if (loaded) {
    "Нативный llama.cpp runtime подключён."
  } else {
    "Нативный llama.cpp runtime пока не установлен для ABI устройства."
  }

  fun load(path: String): Map<String, Any?> {
    require(path.isNotBlank()) { "Путь к модели не может быть пустым." }
    if (!loaded) {
      return mapOf("ok" to false, "reason" to "native_runtime_unavailable")
    }
    modelPath = path
    return mapOf("ok" to true, "path" to path)
  }

  fun unload() {
    modelPath = null
  }
}
