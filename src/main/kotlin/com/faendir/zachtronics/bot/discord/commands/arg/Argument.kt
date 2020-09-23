package com.faendir.zachtronics.bot.discord.commands.arg

import com.faendir.zachtronics.bot.discord.commands.ArgumentIntermediateMap
import com.faendir.zachtronics.bot.model.Category
import com.faendir.zachtronics.bot.model.Game
import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.model.Score
import com.faendir.zachtronics.bot.utils.ResultOrMessage
import net.dv8tion.jda.api.entities.Message

interface Argument<T : Any, R : Any> {
    val argumentType: Class<*>
    val argumentName: String
    val displayName: String

    fun <C : Category<C, S, P>, S: Score, P : Puzzle> getAllPossibleMatches(game: Game<*, C,S,P>, message: Message, input: List<IndexedValue<String>>): List<Match<T>>

    fun getActualResult(args: ArgumentIntermediateMap) : ResultOrMessage<R>

}