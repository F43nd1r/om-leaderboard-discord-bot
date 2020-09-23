package com.faendir.zachtronics.bot.discord

import com.faendir.zachtronics.bot.config.DiscordProperties
import com.faendir.zachtronics.bot.discord.commands.Command
import com.faendir.zachtronics.bot.model.Game
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.GatewayIntent
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Service
class DiscordService(private val discordProperties: DiscordProperties, private val commands: List<Command>, private val games: List<Game<*, *, *, *>>) {
    private val adapter = object : ListenerAdapter() {
        override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
            if (!event.author.isBot) {
                val game = games.find { it.discordChannel == event.channel.name } ?: return
                handleMessage(event, game)
            }
        }
    }
    private val jda: JDA = JDABuilder.createLight(discordProperties.token, GatewayIntent.GUILD_MESSAGES).addEventListeners(adapter).build().awaitReady()

    private fun handleMessage(event: GuildMessageReceivedEvent, game: Game<*, *, *, *>) {
        val message = event.message.contentRaw
        commands.forEach { command ->
            if (message.startsWith("!${command.name}")) {
                event.channel.sendMessage("${event.author.asMention} ${
                    if (command.requiresRoles.isEmpty() || event.member?.roles?.map { it.name }?.containsAll(command.requiresRoles) == true) {
                        command.handleMessage(game, event.message)
                    } else {
                        "sorry, you do not have all required roles for this command ${command.requiresRoles.joinToString(separator = "`, `", prefix = "(`", postfix = "`)")}."
                    }
                }").mention(event.author).queue()
            }
        }
    }

    fun sendMessage(channel: String, message: String) {
        jda.guilds.forEach { guild -> guild.channels.filterIsInstance<TextChannel>().find { it.name == channel }?.sendMessage(message)?.queue() }
    }
}


