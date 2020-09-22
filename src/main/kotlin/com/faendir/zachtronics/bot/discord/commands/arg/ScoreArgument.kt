package com.faendir.zachtronics.bot.discord.commands.arg

import com.faendir.zachtronics.bot.model.Game
import com.faendir.zachtronics.bot.model.Score

class ScoreArgument<S : Score>(private val game: Game<*, S, *>) : Argument<S> {

    override fun getAllPossibleMatches(input: List<IndexedValue<String>>): List<Match<S>> =
        input.mapNotNull { string -> game.parseScore(string.value)?.let { Match(string.index, it) } }
}