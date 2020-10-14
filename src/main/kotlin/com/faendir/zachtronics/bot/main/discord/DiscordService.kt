package com.faendir.zachtronics.bot.main.discord

import com.faendir.zachtronics.bot.main.GameContext
import com.faendir.zachtronics.bot.utils.Result
import com.faendir.zachtronics.bot.utils.changeContent
import com.faendir.zachtronics.bot.utils.fuzzyMatch
import com.faendir.zachtronics.bot.utils.match
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.MessageUpdateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.restaction.MessageAction
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicBoolean

@Service
class DiscordService(private val jda: JDA, private val gameContexts: List<GameContext>) {
    private val adapter = object : ListenerAdapter() {
        override fun onMessageReceived(event: MessageReceivedEvent) {
            if (!event.author.isBot) {
                handleMessage(event.message) { event.channel.sendMessage(it) }
            }
        }

        override fun onMessageUpdate(event: MessageUpdateEvent) {
            messageCache[event.message.idLong]?.let { response ->
                handleMessage(event.message) { response.editMessage(it) }
            }
        }
    }
    private val privateMessageRegex = Regex("(?<game>[^!:]*)[:\\s]*(?<command>!.*)")

    init {
        jda.addEventListener(adapter)
    }

    //size bounded message cache
    private val messageCache = object : LinkedHashMap<Long, Message>() {
        override fun removeEldestEntry(eldest: Map.Entry<Long, Message>) = size > 50
    }

    private fun handleMessage(message: Message, createMessageAction: (String) -> MessageAction) {
        val (gameContext, editedMessage) = findGameContext(message).onFailure {
            sendResponse(message, it, createMessageAction)
            return
        }
        val command = gameContext.commands.find { editedMessage.contentRaw.startsWith("!${it.name}") } ?: return
        GlobalScope.launch {
            val mainJob = async {
                if (command.isReadOnly || gameContext.game.hasWritePermission(editedMessage.member)) {
                    command.handleMessage(editedMessage)
                } else {
                    "sorry, you do not have the permission to use this command."
                } + if (command.isReadOnly && editedMessage.channelType != ChannelType.PRIVATE) {
                    "\n\nᴰᶦᵈ ʸᵒᵘ ᵏⁿᵒʷˀ ʸᵒᵘ ᶜᵃⁿ ᵘˢᵉ ᵗʰᶦˢ ᶜᵒᵐᵐᵃⁿᵈ ᶦⁿ ᵖʳᶦᵛᵃᵗᵉ ᵐᵉˢˢᵃᵍᵉˢ"
                } else {
                    ""
                }
            }
            val startedMessageSend = AtomicBoolean()
            val messageJob = async {
                delay(2000)
                if (startedMessageSend.compareAndSet(false, true)) {
                    createMessageAction("${message.author.asMention} processing your request, please wait...").mention(message.author).complete()
                } else {
                    null
                }
            }
            val response = mainJob.await()
            val currentCreateMessageAction: (String) -> MessageAction = if (startedMessageSend.compareAndSet(true, true)) {
                messageJob.await().let { message -> { message!!.editMessage(it) } }
            } else {
                messageJob.cancel()
                createMessageAction
            }
            sendResponse(message, response, currentCreateMessageAction)
        }
    }

    private fun sendResponse(message: Message, response: String, createMessageAction: (String) -> MessageAction) {
        if (response.isNotBlank()) {
            createMessageAction("${message.author.asMention} $response").mention(message.author).queue {
                messageCache[message.idLong] = it
            }
        }
    }

    private fun findGameContext(message: Message): Result<Pair<GameContext, Message>> {
        return if (message.channelType == ChannelType.TEXT) {
            gameContexts.find { it.game.discordChannel == message.channel.name }?.let { Result.success(it to message) } ?: Result.failure("")
        } else {
            message.match(privateMessageRegex).flatMap { input ->
                val name = input.groups["game"]!!.value.trim()
                if (name.isBlank()) return Result.parseFailure("In private messages you have to prefix your command with a game, e.g. `OM: !help`.")
                val matches = gameContexts.fuzzyMatch(name) { it.game.displayName }
                when (val size = matches.size) {
                    0 -> Result.parseFailure("I did not recognize the game \"$name\".")
                    1 -> Result.success(matches.first() to message.changeContent(input.groups["command"]!!.value))
                    in 2..5 -> Result.parseFailure("your request for \"$name\" was not precise enough. Use one of:\n${
                        matches.joinToString("\n") { it.game.displayName }
                    }")
                    else -> Result.parseFailure("your request for \"$name\" was not precise enough. $size matches.")
                }
            }
        }
    }

    fun sendMessage(channel: String, message: String) {
        jda.guilds.forEach { guild -> guild.channels.filterIsInstance<TextChannel>().find { it.name == channel }?.sendMessage(message)?.queue() }
    }
}

