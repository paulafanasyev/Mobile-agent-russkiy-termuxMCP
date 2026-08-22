package com.mobileshell.firewall

import io.nekohasekai.libbox.StringIterator

/** Безопасный адаптер коллекции строк к интерфейсу gomobile. */
internal class LibboxStringIterator(
    values: List<String>,
) : StringIterator {
    private val iterator = values.iterator()

    override fun len(): Int = 0
    override fun hasNext(): Boolean = iterator.hasNext()
    override fun next(): String = iterator.next()
}
