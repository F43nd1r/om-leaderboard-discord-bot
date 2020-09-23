package com.faendir.zachtronics.bot.discord.commands.arg

import com.faendir.zachtronics.bot.model.*
import net.dv8tion.jda.api.entities.Message
import org.springframework.stereotype.Component

@Component
class AuthorArgument : SimpleArgument<Author> {
    override val argumentType: Class<*> = Author::class.java
    override val argumentName: String = "author"
    override val displayName: String = "author"

    override fun <C : Category<C, S, P>, S : Score, P : Puzzle> getAllPossibleMatches(game: Game<*, C, S, P>, message: Message, input: List<IndexedValue<String>>): List<Match<Author>> {
        return input.map { Match(it.index, Author(it.value)) } + Match(emptyList(), Author(message.author.name))
    }
}