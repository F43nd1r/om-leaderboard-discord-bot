package com.faendir.zachtronics.bot.generic.discord

import discord4j.core.event.domain.interaction.InteractionCreateEvent
import discord4j.discordjson.json.ApplicationCommandOptionData
import discord4j.discordjson.json.WebhookExecuteRequest
import discord4j.rest.util.MultipartRequest
import reactor.core.publisher.Mono

interface Command {

    /**
     * this name must match the name used in [buildData]
     */
    val name: String

    val isReadOnly: Boolean

    fun handle(event: InteractionCreateEvent): Mono<MultipartRequest<WebhookExecuteRequest>>

    fun buildData(): ApplicationCommandOptionData
}