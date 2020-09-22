package com.faendir.zachtronics.bot.discord.commands.arg

import com.faendir.zachtronics.bot.model.Category

class CategoryArgument<C : Category<C, *, *>>(private val categories: List<C>) : Argument<List<C>> {
    override fun getAllPossibleMatches(input: List<IndexedValue<String>>): List<Match<List<C>>> =
        input.mapNotNull { string -> categories.filter { it.displayName == string.value }.takeIf { it.isNotEmpty() }?.let { Match(string.index, it) } }
}