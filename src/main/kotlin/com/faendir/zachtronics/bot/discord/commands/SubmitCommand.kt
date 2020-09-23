package com.faendir.zachtronics.bot.discord.commands

import com.faendir.zachtronics.bot.discord.commands.arg.Argument
import com.faendir.zachtronics.bot.discord.commands.arg.LinkArgument
import com.faendir.zachtronics.bot.discord.commands.arg.PuzzleArgument
import com.faendir.zachtronics.bot.discord.commands.arg.ScoreArgument
import com.faendir.zachtronics.bot.leaderboards.UpdateResult
import com.faendir.zachtronics.bot.model.*
import com.faendir.zachtronics.bot.utils.findInstance
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import org.springframework.stereotype.Component

@Component
class SubmitCommand(private val possibleArgs: List<Argument<*, *>>) : Command {
    override val name: String = "submit"
    override val helpText: String = "!submit <puzzle>:<score1/score2/score3>(e.g. 3.5w/320c/400g) <link>(or attach file to message)"
    override val requiresRoles: List<String> = listOf("trusted-leaderboard-poster")

    override fun handleMessage(game: Game<*, *, *, *>, message: Message): String {
        val methodResults = invokeAnnotatedMethods<Submit, UpdateResult>(game, possibleArgs, message)
        methodResults.filterIsInstance<MethodResult.Success<Record>>().takeIf { it.isNotEmpty() }?.let { s ->
            val results = s.map { it.result }
            val puzzle by lazy { s.first().args.find<PuzzleArgument, Puzzle>()?.displayName ?: "" }
            val score by lazy { s.first().args.find<ScoreArgument, Score>()?.toDisplayString() ?: "" }
            val link by lazy { s.first().args.find<LinkArgument, Link>() ?: "" }
            results.filterIsInstance<UpdateResult.Success>().takeIf { it.isNotEmpty() }?.let { successes ->
                return "thanks, the site will be updated shortly with $puzzle ${
                    successes.flatMap { it.oldScores.keys }.map { it.displayName }
                } $score (previously ${
                    successes.flatMap { it.oldScores.entries }.joinToString { "`${it.key.displayName} ${it.value?.toDisplayString() ?: "none"}`" }
                })."
            }
            results.findInstance<UpdateResult.ParetoUpdate>()?.let { return "thanks, your submission for $puzzle ($score) was included in the pareto frontier." }
            results.findInstance<UpdateResult.BrokenLink>()?.let { return "sorry, I could not load the file at $link." }
            results.filterIsInstance<UpdateResult.BetterExists>().takeIf { it.isNotEmpty() }?.let { betterExists ->
                return "sorry, your submission did not beat any of the existing scores for $puzzle ${
                    betterExists.flatMap { it.scores.entries }.joinToString { "`${it.key.displayName} ${it.value.toDisplayString()}`" }
                }"
            }
            return "sorry, something went wrong."
        }
        methodResults.findInstance<MethodResult.ArgsNotParsed<Record>>()?.let { return it.message }
        methodResults.findInstance<MethodResult.NotSupported<Record>>()?.let { return "sorry, this game does not support the command $name" }
        return "sorry, no leaderboards for this game were found"
    }

}