package com.faendir.zachtronics.bot.discord.commands

import com.faendir.zachtronics.bot.discord.commands.arg.Argument
import com.faendir.zachtronics.bot.discord.commands.arg.Match
import com.faendir.zachtronics.bot.model.Game
import com.faendir.zachtronics.bot.model.Puzzle

fun <P : Puzzle> findPuzzle(game: Game<*, *, P>, puzzleName: String, processPuzzle: (P) -> String): String {
    val puzzles = game.findPuzzleByName(puzzleName)
    return when (val size = puzzles.size) {
        0 -> "sorry, I did not recognize the puzzle \"$puzzleName\"."
        1 -> processPuzzle(puzzles.first())
        in 2..5 -> "sorry, your request for \"$puzzleName\" was not precise enough. Use one of:\n${puzzles.joinToString("\n") { it.displayName }}"
        else -> "sorry, your request for \"$puzzleName\" was not precise enough. $size matches."
    }
}

fun parseArguments(expected: List<Argument<*>>, input: String): ArgumentMap? {
    val parts = input.split(Regex("\\s+")).filter { it.isNotBlank() }.withIndex().toList()
    return findCollisionFreeFullCover(expected.map { it to it.getAllPossibleMatches(parts) }, parts.map { it.index })?.let { ArgumentMap(it.toMutableMap()) }
}

private fun findCollisionFreeFullCover(options : List<Pair<Argument<*>, List<Match<out Any>>>>, freeIndices : List<Int>) : Map<Argument<*>, Any>? {
    if(options.isEmpty()) return emptyMap()
    if(freeIndices.isEmpty()) return null
    val option = options.first()
    val other = options.drop(1)
    for(match in option.second) {
        if(freeIndices.containsAll(match.consumedInputIndices)) {
            findCollisionFreeFullCover(other, freeIndices - match.consumedInputIndices)?.let { return it + (option.first to match.result) }
        }
    }
    return null
}

class ArgumentMap(map: MutableMap<Argument<*>, Any>) : MutableMap<Argument<*>, Any> by map {
    @JvmName("tGet")
    operator fun <T : Any> get(key: Argument<T>): T? {
        @Suppress("UNCHECKED_CAST") return get(key) as? T
    }
}
