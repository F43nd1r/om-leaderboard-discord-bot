package com.faendir.zachtronics.bot.generic.discord

import com.faendir.discord4j.command.annotation.OptionConverter
import discord4j.common.util.Snowflake
import discord4j.core.`object`.command.Interaction
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.reaction.ReactionEmoji
import discord4j.core.event.domain.interaction.InteractionCreateEvent
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toMono
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import java.util.*

class LinkConverter : OptionConverter<String> {
    override fun fromString(context: InteractionCreateEvent, string: String?): Mono<Optional<String>> {
        return string?.let { s ->
            findLink(s, context.interaction.channel.flatMapMany { it.getMessagesBefore(it.lastMessageId.orElseGet { Snowflake.of(Instant.now()) }) }
                .filter { it.author.isPresent && it.author.get() == context.interaction.user }).map { Optional.of(it) }
        } ?: Mono.just(Optional.empty())
    }

    private fun findLink(input: String, messages: Flux<Message>): Mono<String> = Mono.defer {
        if (Regex("m\\d+").matches(input)) {
            val num = input.removePrefix("m").toInt()
            messages.elementAt(num - 1).flatMap { message ->
                message.attachments.firstOrNull()?.url?.let { message.addReaction(ReactionEmoji.unicode("\uD83D\uDC4D"/* 👍 */)).then(it.toMono()) }
                    ?: message.guild.map { it.id.asLong().toString() }.switchIfEmpty { "@me".toMono() }.map {
                        throw IllegalArgumentException("https://discord.com/channels/${it}/${message.channelId.asLong()}/${message.id.asLong()} had no attachments")
                    }
            }
        } else {
            input.toMono()
        }.doOnNext { checkLink(it) }
    }

    private fun checkLink(string: String) {
        val valid = try {
            val connection = URL(string).openConnection() as HttpURLConnection
            connection.requestMethod = "HEAD"
            connection.responseCode in (200 until 300)
        } catch (e: Exception) {
            false
        }
        if (!valid) {
            throw IllegalArgumentException("\"$string\" is not a valid link")
        }
    }
}