package com.faendir.zachtronics.bot.discord.commands

import com.faendir.zachtronics.bot.leaderboards.Leaderboard
import com.faendir.zachtronics.bot.model.Category
import com.faendir.zachtronics.bot.model.Game
import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.model.Score
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
class HelpCommand(@Lazy private val commands: List<Command>) : Command {
    override val name: String = "help"
    override val helpText: String = "This command"

    override fun handleMessage(game: Game<*,*, *, *>, message: Message) =
        "Available commands:\n```${makeCommandTable()}```\nSupported categories:\n```${makeCategoryList(game.leaderboards)}```"

    private fun makeCommandTable(): String {
        val nameLength = commands.maxOf { it.name.length } + 4
        val textLength = commands.maxOf { it.helpText.length }
        return commands.joinToString("\n") { it.name.padEnd(nameLength) + it.helpText.padEnd(textLength) }
    }

    private fun makeCategoryList(leaderboards: List<Leaderboard>): String {
        return leaderboards.flatMap { it.supportedCategories }
            .distinctBy { it.displayName }
            .joinToString("\n") { category -> "${category.displayName} (${category.contentDescription})" }
    }
}