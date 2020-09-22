package com.faendir.zachtronics.bot.discord.commands.arg

class AuthorArgument : Argument<String> {
    override fun getAllPossibleMatches(input: List<IndexedValue<String>>): List<Match<String>> = input.map { Match(it.index, it.value) }
}