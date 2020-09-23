package com.faendir.zachtronics.bot.discord.commands

import com.faendir.zachtronics.bot.discord.commands.arg.Argument
import com.faendir.zachtronics.bot.discord.commands.arg.PuzzleArgument
import com.faendir.zachtronics.bot.leaderboards.reddit.RedditLeaderboard
import com.faendir.zachtronics.bot.model.Game
import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.model.Record
import com.faendir.zachtronics.bot.utils.findInstance
import net.dv8tion.jda.api.entities.Message
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class ShowCommand(private val possibleArgs: List<Argument<*, *>>) : Command {
    override val name: String = "show"
    override val helpText: String = "!show <category> <puzzle>"

    override fun handleMessage(game: Game<*, *, *, *>, message: Message): String {
        val results = invokeAnnotatedMethods<Show, Record>(game, possibleArgs, message)
        results.findInstance<MethodResult.Success<Record>>()
            ?.let { return "here you go: ${it.args.find<PuzzleArgument, Puzzle>()?.displayName ?: ""} ${it.result.toDisplayString()}" }
        results.findInstance<MethodResult.NotFound<Record>>()?.let { return "sorry, there is no score for ${message.contentRaw.removePrefix("!show ")}." }
        results.findInstance<MethodResult.ArgsNotParsed<Record>>()?.let { return it.message }
        results.findInstance<MethodResult.NotSupported<Record>>()?.let { return "sorry, this game does not support the command $name" }
        return "sorry, no leaderboards for this game were found"
    }
}