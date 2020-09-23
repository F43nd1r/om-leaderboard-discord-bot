package com.faendir.zachtronics.bot.discord.commands.arg

import com.faendir.zachtronics.bot.model.*
import net.dv8tion.jda.api.entities.Message
import org.springframework.stereotype.Component

@Component
class LinkArgument : SimpleArgument<Link> {
    companion object {
        private val regex = Regex("http.*\\.(gif|gifv|mp4|webm)")
    }

    override val argumentType: Class<*> = Link::class.java
    override val argumentName: String = "link"
    override val displayName: String = "link"

    override fun <C : Category<C, S, P>, S : Score, P : Puzzle> getAllPossibleMatches(game: Game<*, C, S, P>, message: Message, input: List<IndexedValue<String>>): List<Match<Link>> {
        val matches = input.filter { it.value.matches(regex) }.map { Match(it.index, Link(it.value)) }
        if (message.attachments.isNotEmpty()) {
            val url = message.attachments.first().url
            if (url.matches(regex)) {
                return matches + Match(emptyList(), Link(url))
            }
        }
        return matches
    }
}