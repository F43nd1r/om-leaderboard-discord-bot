package com.faendir.zachtronics.bot.discord.commands.arg

import com.faendir.zachtronics.bot.model.Game
import com.faendir.zachtronics.bot.model.Puzzle
import com.marcinmoskala.math.powerset

class PuzzleArgument<P : Puzzle>(private val game: Game<*, *, P>) : Argument<List<P>> {
    override fun getAllPossibleMatches(input: List<IndexedValue<String>>): List<Match<List<P>>> {
        return input.powerset()
            .filter { it.isNotEmpty() }
            .mapNotNull { strings ->
                game.findPuzzleByName(strings.joinToString(" ") { it.value })
                    .takeIf { it.isNotEmpty() }
                    ?.let { puzzles -> Match(strings.map { it.index }, puzzles) }
            }.sortedByDescending { it.consumedInputIndices.size }
    }
}