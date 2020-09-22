package com.faendir.zachtronics.bot.discord.commands.arg

class LinkArgument : Argument<String> {
    companion object {
        private val regex = Regex("http.*\\.(gif|gifv|mp4|webm)")
    }

    override fun getAllPossibleMatches(input: List<IndexedValue<String>>): List<Match<String>> = input.filter { it.value.matches(regex) }.map { Match(it.index, it.value) }
}