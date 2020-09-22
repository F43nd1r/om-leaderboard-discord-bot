package com.faendir.zachtronics.bot.discord.commands

import com.faendir.zachtronics.bot.model.Category
import com.faendir.zachtronics.bot.model.Game
import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.model.Score
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import org.springframework.stereotype.Component

@Component
class ShowCommand : Command {
    override val regex = Regex("!show\\s+(?<category>\\S+)\\s+(?<puzzle>.+)")
    override val name: String = "show"
    override val helpText: String = "!show <category> <puzzle>"

    override fun <C : Category<C, S, P>, S : Score, P : Puzzle> handleMessage(game: Game<C, S, P>, author: User, channel: TextChannel, message: Message, command: MatchResult): String {
        val arguments = parseArguments(game.showArguments, message.contentRaw) ?:
        val categoryString = command.groups["category"]!!.value
        return findPuzzle(game, command.groups["puzzle"]!!.value) { puzzle ->
            val (leaderboard, category) = game.leaderboards.flatMap { leaderboard -> leaderboard.supportedCategories.map { leaderboard to it } }
                .filter { (_, category) -> category.displayName.equals(categoryString, ignoreCase = true) }
                .ifEmpty { return@findPuzzle "sorry, I could not find the category \"$categoryString\"" }
                .find { it.second.supportsPuzzle(puzzle) }
                ?: return@findPuzzle "sorry, the category \"${categoryString.toLowerCase()}\" does not support the puzzle ${puzzle.displayName}."
            return@findPuzzle leaderboard.get(puzzle, category)?.let {
                "here you go: ${puzzle.displayName} ${it.toDisplayString()}"
            } ?: "sorry, there is no score for ${puzzle.displayName} ${category.displayName}."
        }
    }
}