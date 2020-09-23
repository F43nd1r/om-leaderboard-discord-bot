package com.faendir.zachtronics.bot.utils

sealed class ResultOrMessage<T> {
    class Success<T>(val result: T) : ResultOrMessage<T>()

    @Suppress("UNCHECKED_CAST")
    class Failure<T>(val message: String) : ResultOrMessage<T>() {
        fun <R> typed() = this as Failure<R>
    }
}