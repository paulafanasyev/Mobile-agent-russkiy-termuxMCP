package com.mobileshell.firewall

/** Состояния движка, которые можно безопасно показывать UI и Светлане. */
enum class FirewallEngineState {
    ВЫКЛЮЧЕН,
    ГОТОВ,
    ЗАПУСКАЕТСЯ,
    РАБОТАЕТ,
    ОШИБКА,
}
