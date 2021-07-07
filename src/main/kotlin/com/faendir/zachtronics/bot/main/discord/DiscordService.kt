package com.faendir.zachtronics.bot.main.discord

import com.faendir.zachtronics.bot.main.GameContext
import com.faendir.zachtronics.bot.utils.throwIfEmpty
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.entity.User
import discord4j.core.event.domain.interaction.InteractionCreateEvent
import discord4j.core.event.domain.interaction.SlashCommandEvent
import discord4j.discordjson.json.ApplicationCommandRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2

@Service
class DiscordService(discordClient: GatewayDiscordClient, private val gameContexts: List<GameContext>) {
    companion object {
        private val logger = LoggerFactory.getLogger(DiscordService::class.java)
    }

    init {
        val requests = gameContexts.map { context ->
            val game = context.game
            val request = ApplicationCommandRequest.builder()
                .name(game.commandName)
                .description(game.displayName)
            for (command in context.commands) {
                request.addOption(command.buildData())
            }
            request.build()
        }
        val restClient = discordClient.restClient
        Flux.fromIterable(requests)
            .zipWith(restClient.applicationId.repeat())
            .flatMap { (req, appId) ->
                restClient.applicationService
                    .createGlobalApplicationCommand(appId, req)
                    .doOnError { e -> logger.warn("Unable to create global command", e) }
                    .onErrorResume { Mono.empty() }
            }
            .subscribe()
        discordClient.on(SlashCommandEvent::class.java).flatMap {
            it.acknowledge().then(handleCommand(it))
        }.subscribe()
    }

    private fun handleCommand(event: SlashCommandEvent): Mono<Void> {
        return Mono.defer {
            findGameContext(event)
                .flatMap { gameContext ->
                    val option = event.options.first()
                    val command = gameContext.commands.find { it.name == option.name }
                        ?: return@flatMap Mono.error(IllegalArgumentException("I did not recognize the command \"${option.name}\"."))

                    if (!command.isReadOnly) {
                        gameContext.game.hasWritePermission(event.interaction.member.map { it as User }.orElse(event.interaction.user)).map {
                            if (it) {
                                command
                            } else {
                                throw IllegalArgumentException("sorry, you do not have the permission to use this command.")
                            }
                        }
                    } else {
                        command.toMono()
                    }
                }
                .flatMap { command -> command.handle(event) }
                .flatMap { event.interactionResponse.createFollowupMessage(it) }
                .onErrorResume {
                    logger.info("User command failed", it)
                    event.interactionResponse.createFollowupMessage("**Failed**: ${it.message ?: "Something went wrong"}")
                }
                .then()
        }
    }

    private fun findGameContext(event: InteractionCreateEvent): Mono<GameContext> {
        val name = event.interaction.commandInteraction.orElseThrow { IllegalStateException() }.name.orElseThrow { IllegalStateException() }
        return Mono.fromCallable<GameContext> { gameContexts.find { it.game.commandName == name } }
            .throwIfEmpty { "I did not recognize the game \"$name\"." }
    }
}


