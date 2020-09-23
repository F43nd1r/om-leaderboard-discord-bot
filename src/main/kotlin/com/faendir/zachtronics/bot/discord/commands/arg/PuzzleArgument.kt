package com.faendir.zachtronics.bot.discord.commands.arg

import com.faendir.zachtronics.bot.discord.commands.ArgumentIntermediateMap
import com.faendir.zachtronics.bot.model.Category
import com.faendir.zachtronics.bot.model.Game
import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.model.Score
import com.faendir.zachtronics.bot.utils.ResultOrMessage
import com.marcinmoskala.math.powerset
import net.dv8tion.jda.api.entities.Message
import org.springframework.stereotype.Component

@Component
class PuzzleArgument : Argument<List<Puzzle>, Puzzle> {

    override val argumentType: Class<*> = Puzzle::class.java
    override val argumentName: String = "puzzle"
    override val displayName: String = "puzzle"

    override fun <C : Category<C, S, P>, S : Score, P : Puzzle> getAllPossibleMatches(game: Game<*, C, S, P>, message: Message, input: List<IndexedValue<String>>): List<Match<List<Puzzle>>> {
        return input.powerset().filter { it.isNotEmpty() }.mapNotNull { strings ->
                game.findPuzzleByName(strings.joinToString(" ") { it.value })
                    .takeIf { it.isNotEmpty() }
                    ?.let { puzzles -> Match(strings.map { it.index }, puzzles.filterIsInstance<Puzzle>()) }
            }.sortedByDescending { it.consumedInputIndices.size }
    }

    override fun getActualResult(args: ArgumentIntermediateMap): ResultOrMessage<Puzzle> {
        val puzzles = args[this]!!
        return when (val size = puzzles.size) {
            0 -> ResultOrMessage.Failure("sorry, I did not recognize the puzzle.")
            1 -> ResultOrMessage.Success(puzzles.first())
            in 2..5 -> ResultOrMessage.Failure("sorry, your puzzle request was not precise enough. Use one of:\n${puzzles.joinToString("\n") { it.displayName }}")
            else -> ResultOrMessage.Failure("sorry, your puzzle request was not precise enough. $size matches.")
        }
    }
}