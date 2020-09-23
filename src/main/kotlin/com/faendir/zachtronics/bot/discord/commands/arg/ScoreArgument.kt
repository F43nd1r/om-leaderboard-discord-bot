package com.faendir.zachtronics.bot.discord.commands.arg

import com.faendir.zachtronics.bot.discord.commands.ArgumentIntermediateMap
import com.faendir.zachtronics.bot.model.Category
import com.faendir.zachtronics.bot.model.Game
import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.model.Score
import com.faendir.zachtronics.bot.utils.ResultOrMessage
import net.dv8tion.jda.api.entities.Message
import org.springframework.stereotype.Component

@Component
class ScoreArgument(private val transformers: List<ScoreTransformer<*>>) : SimpleArgument<Score> {

    override val argumentType: Class<*> = Score::class.java
    override val argumentName: String = "score"
    override val displayName: String = "score"

    override fun <C : Category<C, S, P>, S : Score, P : Puzzle> getAllPossibleMatches(game: Game<*, C, S, P>, message: Message, input: List<IndexedValue<String>>): List<Match<Score>> =
        input.mapNotNull { string -> game.parseScore(string.value)?.let { Match(string.index, it) } }

    override fun getActualResult(args: ArgumentIntermediateMap): ResultOrMessage<Score> {
        return ResultOrMessage.Success(transformers.fold(args[this]!!){ acc, transformer -> transformer.transformIfMatching(args, acc) })
    }

    private fun <T : Score> ScoreTransformer<T>.transformIfMatching(args: ArgumentIntermediateMap, score: Score) : Score{
        @Suppress("UNCHECKED_CAST") return if(type.isInstance(score)) transform(args, score as T) else score
    }
}

interface ScoreTransformer<T : Score> {
    val type: Class<T>

    fun transform(args: ArgumentIntermediateMap, t: T) : T
}