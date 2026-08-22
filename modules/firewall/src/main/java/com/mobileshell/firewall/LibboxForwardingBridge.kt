package com.mobileshell.firewall

/**
 * Безопасный адаптер между Android VpnService и libbox.
 *
 * Этот слой намеренно не подменяет libbox заглушкой: до появления
 * собранного AAR сервис не запускает forwarding и сообщает об отсутствии
 * движка. Это предотвращает ложное состояние «фаервол работает».
 */
class LibboxForwardingBridge {
    @Volatile
    private var running = false

    fun start(configJson: String): Result<Unit> {
        if (configJson.isBlank()) {
            return Result.failure(IllegalArgumentException("Конфигурация libbox пуста"))
        }
        return runCatching {
            // Реальная JNI/libbox binding подключается после появления AAR.
            // Не выполняем фиктивный forwarding.
            check(LibboxBindingAvailability.isAvailable()) {
                "libbox AAR/JNI ещё не подключён к Android-модулю"
            }
            running = true
        }
    }

    fun stop() {
        running = false
    }

    fun isRunning(): Boolean = running
}

private object LibboxBindingAvailability {
    fun isAvailable(): Boolean = try {
        Class.forName("io.nekohasekai.libbox.BoxService")
        true
    } catch (_: ClassNotFoundException) {
        false
    }
}
