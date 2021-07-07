package com.faendir.zachtronics.bot.om.discord

import com.faendir.discord4j.command.annotation.ApplicationCommand
import com.faendir.discord4j.command.annotation.Converter
import com.faendir.discord4j.command.annotation.Description
import com.faendir.zachtronics.bot.generic.discord.AbstractShowCommand
import com.faendir.zachtronics.bot.model.Leaderboard
import com.faendir.zachtronics.bot.om.model.*
import discord4j.core.event.domain.interaction.InteractionCreateEvent
import discord4j.discordjson.json.ApplicationCommandOptionData
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.util.function.Tuple2
import reactor.util.function.Tuples

@Component
class OmShowCommand(override val leaderboards: List<Leaderboard<OmCategory, OmPuzzle, OmRecord>>) :
    AbstractShowCommand<OmCategory, OmPuzzle, OmRecord>() {

    override fun buildData(): ApplicationCommandOptionData = ShowParser.buildData()

    override fun findPuzzleAndCategory(interaction: InteractionCreateEvent): Mono<Tuple2<OmPuzzle, OmCategory>> {
        return ShowParser.parse(interaction).map { show ->
            val puzzle = show.puzzle
            val categories = findCategoryCandidates(show)
            if (categories.isEmpty()) throw IllegalArgumentException("${show.category} is not a tracked category.")
            val filtered = categories.filter { it.supportsPuzzle(puzzle) }
            if (filtered.isEmpty()) throw IllegalArgumentException("Category ${categories.first().displayName} does not support ${puzzle.displayName}")
            Tuples.of(puzzle, filtered.first())
        }
    }

    private fun findCategoryCandidates(show: Show): List<OmCategory> {
        return OmCategory.values().filter { it.displayName.startsWith(show.category, ignoreCase = true) }
    }

}

@ApplicationCommand(description = "Show a record", subCommand = true)
data class Show(
    @Description("Puzzle name. Can be shortened or abbreviated. E.g. `stab water`, `PMO`")
    @Converter(PuzzleConverter::class)
    val puzzle: OmPuzzle,
    @Description("Category. E.g. `GC`, `sum`")
    val category: String
)

