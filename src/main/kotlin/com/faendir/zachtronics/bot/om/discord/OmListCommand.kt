package com.faendir.zachtronics.bot.om.discord

import com.faendir.discord4j.command.annotation.ApplicationCommand
import com.faendir.discord4j.command.annotation.Converter
import com.faendir.discord4j.command.annotation.Description
import com.faendir.zachtronics.bot.generic.discord.AbstractListCommand
import com.faendir.zachtronics.bot.model.Leaderboard
import com.faendir.zachtronics.bot.om.model.*
import discord4j.core.event.domain.interaction.InteractionCreateEvent
import discord4j.discordjson.json.ApplicationCommandOptionData
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.util.function.Tuple2
import reactor.util.function.Tuples

@Component
class OmListCommand(override val leaderboards: List<Leaderboard<OmCategory, OmPuzzle, OmRecord>>) :
    AbstractListCommand<OmCategory, OmPuzzle, OmRecord>() {

    override fun buildData(): ApplicationCommandOptionData = ListCommandParser.buildData()

    override fun findPuzzleAndCategories(interaction: InteractionCreateEvent): Mono<Tuple2<OmPuzzle, List<OmCategory>>> {
        return ListCommandParser.parse(interaction).map { show -> Tuples.of(show.puzzle, OmCategory.values().filter { it.supportsPuzzle(show.puzzle) }) }
    }
}

@ApplicationCommand(name = "list", description = "List records", subCommand = true)
data class ListCommand(
    @Description("Puzzle name. Can be shortened or abbreviated. E.g. `stab water`, `PMO`")
    @Converter(PuzzleConverter::class)
    val puzzle: OmPuzzle
)

