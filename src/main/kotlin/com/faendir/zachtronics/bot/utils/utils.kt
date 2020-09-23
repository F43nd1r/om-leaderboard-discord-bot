package com.faendir.zachtronics.bot.utils

inline fun <reified R> Iterable<*>.findInstance():R? {
    return filterIsInstance<R>().firstOrNull()
}