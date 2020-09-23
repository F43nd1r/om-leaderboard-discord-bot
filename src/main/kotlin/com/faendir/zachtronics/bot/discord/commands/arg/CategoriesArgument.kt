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
class CategoriesArgument : Argument<List<Category<*, *, *>>, List<Category<*, *, *>>> {

    override val argumentType: Class<*> = List::class.java
    override val argumentName: String = "categories"
    override val displayName: String = "category"

    override fun <C : Category<C, S, P>, S : Score, P : Puzzle> getAllPossibleMatches(game: Game<*, C, S, P>, message: Message, input: List<IndexedValue<String>>): List<Match<List<Category<*, *, *>>>> =
        input.mapNotNull { string -> game.findCategoryByName(string.value).takeIf { it.isNotEmpty() }?.let { Match(string.index, it) } }

    override fun getActualResult(args: ArgumentIntermediateMap): ResultOrMessage<List<Category<*, *, *>>> {
        val puzzleResult = args.findKey<PuzzleArgument>()?.getActualResult(args)
        if(puzzleResult is ResultOrMessage.Failure) return puzzleResult.typed()
        val puzzle = (puzzleResult as ResultOrMessage.Success?)?.result
        val scoreResult = args.findKey<ScoreArgument>()?.getActualResult(args)
        if(scoreResult is ResultOrMessage.Failure) return scoreResult.typed()
        val score = (scoreResult as ResultOrMessage.Success?)?.result
        val categories = args[this]!!
        val result = categories.filter { c ->
            @Suppress("UNCHECKED_CAST") val category = (c as Category<*, Score, Puzzle>)
            puzzle?.let { category.supportsPuzzle(it) } ?: true && score?.let { category.supportsScore(score) } ?: true
        }
        if(result.isEmpty()) return ResultOrMessage.Failure("sorry, the category \"${categories.first().displayName}\" does not support the puzzle ${puzzle?.displayName}.")
        return ResultOrMessage.Success(result)
    }
}