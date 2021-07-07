package com.faendir.zachtronics.bot.generic.discord

import com.faendir.zachtronics.bot.model.Category
import com.faendir.zachtronics.bot.model.Leaderboard
import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.model.Record
import discord4j.core.event.domain.interaction.InteractionCreateEvent
import discord4j.discordjson.json.EmbedData
import discord4j.discordjson.json.EmbedFieldData
import discord4j.discordjson.json.WebhookExecuteRequest
import discord4j.rest.util.MultipartRequest
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import reactor.util.function.Tuple2

abstract class AbstractListCommand<C : Category, P : Puzzle, R : Record> : Command {
    abstract val leaderboards: List<Leaderboard<C, P, R>>
    override val name: String = "list"
    override val isReadOnly: Boolean = true

    override fun handle(event: InteractionCreateEvent): Mono<MultipartRequest<WebhookExecuteRequest>> {
        return findPuzzleAndCategories(event).flatMap { (puzzle, categories) ->
            leaderboards.toFlux().flatMap { it.getAll(puzzle, categories) }.collectList()
                .map { it.reduce { acc, map -> acc + map } }
                .map { records ->
                    WebhookExecuteRequest.builder()
                        .addEmbed(EmbedData.builder()
                            .title("*${puzzle.displayName}*")
                            .addAllFields(
                                records.asIterable()
                                    .groupBy({ it.value }, { it.key })
                                    .map { entry -> entry.key to entry.value.sortedBy<C, Comparable<*>> { it as? Comparable<*> } }
                                    .sortedBy<Pair<R, List<C>>, Comparable<*>> { it.second.first() as? Comparable<*> }
                                    .map { (record, categories) ->
                                        EmbedFieldData.builder()
                                            .name(categories.joinToString("/") { it.displayName })
                                            .value(record.let { "[${it.score.toDisplayString()}](${it.link})" })
                                            .inline(true)
                                            .build()
                                    }
                            )
                            .apply {
                                val missing = categories.minus(records.map { it.key })
                                if (missing.isNotEmpty()) {
                                    addField(
                                        EmbedFieldData.builder()
                                            .name(missing.joinToString("/") { it.displayName })
                                            .value("None")
                                            .inline(false)
                                            .build()
                                    )
                                }
                            }
                            .build()
                        )
                        .build()
                }
        }.map { MultipartRequest.ofRequest(it) }
    }

    /** @return pair of Puzzle and all the categories that support it */
    abstract fun findPuzzleAndCategories(interaction: InteractionCreateEvent): Mono<Tuple2<P, List<C>>>
}