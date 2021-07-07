package com.faendir.zachtronics.bot.om.discord

import com.faendir.discord4j.command.annotation.ApplicationCommand
import com.faendir.discord4j.command.annotation.Converter
import com.faendir.discord4j.command.annotation.Description
import com.faendir.zachtronics.bot.generic.discord.Command
import com.faendir.zachtronics.bot.generic.discord.LinkConverter
import com.faendir.zachtronics.bot.om.model.OmRecord
import discord4j.core.event.domain.interaction.InteractionCreateEvent
import discord4j.discordjson.json.ApplicationCommandOptionData
import discord4j.discordjson.json.WebhookExecuteRequest
import discord4j.rest.util.MultipartRequest
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2

@Component
class OmSubmitArchiveCommand(private val archiveCommand: OmArchiveCommand, private val submitCommand: OmSubmitCommand) : Command {
    override val name: String = "submit-archive"
    override val isReadOnly: Boolean = false

    override fun handle(event: InteractionCreateEvent): Mono<MultipartRequest<WebhookExecuteRequest>> {
        return SubmitArchiveParser.parse(event).flatMap { submitArchive ->
            archiveCommand.parseSolution(archiveCommand.findScoreIdentifier(submitArchive), submitArchive.solution)
                .flatMap { solution ->
                    Mono.zip(
                        archiveCommand.archive(solution),
                        submitCommand.submitToLeaderboards(solution.puzzle, OmRecord(solution.score, submitArchive.gif, event.interaction.user.username))
                    )
                }.map { (archiveOut, submitOut) -> WebhookExecuteRequest.builder().from(submitOut).addEmbed(archiveOut).build() }
        }.map { MultipartRequest.ofRequest(it) }
    }

    override fun buildData(): ApplicationCommandOptionData = SubmitArchiveParser.buildData()
}

@ApplicationCommand(description = "Submit and archive a solution", subCommand = true)
data class SubmitArchive(
    @Converter(LinkConverter::class)
    @Description("Link to your solution file, can be `m1` to scrape it from your last message")
    override val solution: String,
    @Converter(LinkConverter::class)
    @Description("Link to your solution gif/mp4, can be `m1` to scrape it from your last message")
    val gif: String,
    @Description("Score part for nonstandard metrics. E.g. `4h`, `3.5w`")
    override val score: String?
) : IArchive