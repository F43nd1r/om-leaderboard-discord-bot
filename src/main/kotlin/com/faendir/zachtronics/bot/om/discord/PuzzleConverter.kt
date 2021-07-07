package com.faendir.zachtronics.bot.om.discord

import com.faendir.discord4j.command.annotation.OptionConverter
import com.faendir.zachtronics.bot.om.model.OmPuzzle
import com.faendir.zachtronics.bot.utils.fuzzyMatch
import discord4j.core.`object`.component.ActionRow
import discord4j.core.`object`.component.SelectMenu
import discord4j.core.event.domain.interaction.InteractionCreateEvent
import discord4j.core.event.domain.interaction.SelectMenuInteractEvent
import discord4j.discordjson.json.WebhookExecuteRequest
import discord4j.rest.util.MultipartRequest
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.util.*

class PuzzleConverter : OptionConverter<OmPuzzle> {
    override fun fromString(context: InteractionCreateEvent, string: String?): Mono<Optional<OmPuzzle>> =
        Mono.defer {
            if (string == null) return@defer Optional.empty<OmPuzzle>().toMono()
            val matches = OmPuzzle.values().toList().fuzzyMatch(string) { displayName }
            when (matches.size) {
                1 -> Optional.of(matches.first()).toMono()
                in 2..25 -> {
                    context.interactionResponse.createFollowupMessageEphemeral(
                        MultipartRequest.ofRequest(
                            WebhookExecuteRequest.builder()
                                .content("Multiple matching puzzles. Select what you meant:")
                                .addComponent(ActionRow.of(SelectMenu.of("puzzleSelect", matches.map { SelectMenu.Option.of(it.displayName, it.name) })).data)
                                .build()
                        )
                    )
                        .map { it.id() }
                        .flatMap { selectMenuMessageId ->
                            context.interaction.client.on(SelectMenuInteractEvent::class.java).flatMap { event ->
                                Mono.justOrEmpty(event.interaction.message)
                                    .map { it.id }
                                    .filter { selectMenuMessageId.asString() == it.asString() }
                                    .map {
                                        event.acknowledge()
                                        event.message.delete()
                                        Optional.of(OmPuzzle.valueOf(event.values.first()))
                                    }
                            }.next()
                        }
                }
                else -> throw IllegalArgumentException("your request for \"$string\" was not precise enough. ${matches.size} matches.")
            }
        }
}