package com.faendir.zachtronics.bot.discord.commands.arg

data class Match<T>(val consumedInputIndices: List<Int>, val result: T) {
    constructor(consumedIndex: Int, result: T) : this(listOf(consumedIndex), result)
}