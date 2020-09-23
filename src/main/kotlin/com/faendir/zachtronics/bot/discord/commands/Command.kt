package com.faendir.zachtronics.bot.discord.commands

import com.faendir.zachtronics.bot.model.Game
import net.dv8tion.jda.api.entities.Message

interface Command {

    val name: String

    val helpText: String

    val requiresRoles: List<String>
        get() = emptyList()

    fun handleMessage(game: Game<*, *, *, *>, message: Message): String
}