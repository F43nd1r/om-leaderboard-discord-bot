package com.faendir.zachtronics.bot.generic.discord

import com.faendir.zachtronics.bot.model.Category
import com.faendir.zachtronics.bot.model.Leaderboard
import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.model.Record
import com.faendir.zachtronics.bot.utils.throwIfEmpty
import discord4j.core.event.domain.interaction.InteractionCreateEvent
import discord4j.discordjson.json.WebhookExecuteRequest
import discord4j.rest.util.MultipartRequest
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import reactor.util.function.Tuple2

abstract class AbstractShowCommand<C : Category, P : Puzzle, R : Record> : Command {
    abstract val leaderboards: List<Leaderboard<C, P, R>>
    override val name: String = "show"
    override val isReadOnly: Boolean = true

    override fun handle(event: InteractionCreateEvent): Mono<MultipartRequest<WebhookExecuteRequest>> {
        return findPuzzleAndCategory(event).flatMap { (puzzle, category) ->
            leaderboards.toFlux()
                .flatMap { it.get(puzzle, category) }
                .next()
                .throwIfEmpty { "sorry, there is no score for ${puzzle.displayName} ${category.displayName}." }
                .map {
                    MultipartRequest.ofRequestAndFiles(
                        WebhookExecuteRequest.builder().content("*${puzzle.displayName}* **${category.displayName}**\n${it.toDisplayString()}").build(),
                        it.attachments()
                    )
                }
        }
    }

    abstract fun findPuzzleAndCategory(interaction: InteractionCreateEvent): Mono<Tuple2<P, C>>
}