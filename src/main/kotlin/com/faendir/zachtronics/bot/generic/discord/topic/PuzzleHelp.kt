package com.faendir.zachtronics.bot.generic.discord.topic

import net.dv8tion.jda.api.entities.Message
import org.springframework.stereotype.Component

@Component
class PuzzleHelp : HelpTopic {
    override val id: String = "puzzle"

    override fun getHelpText(message: Message): String {
        return """
            |A Puzzle name can be given in a shortened or abbreviated manner, as long as it remains unique.
            |For example:
            | - `stab water` matches `Stabilized Water`
            | - `Face` matches `Face Powder`
            | - `PMO` matches `Precision Machine Oil`
        """.trimMargin()
    }
}