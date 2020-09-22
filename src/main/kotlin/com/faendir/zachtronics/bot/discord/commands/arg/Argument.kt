package com.faendir.zachtronics.bot.discord.commands.arg

interface Argument<T : Any> {
    fun getAllPossibleMatches(input: List<IndexedValue<String>>): List<Match<T>>
}