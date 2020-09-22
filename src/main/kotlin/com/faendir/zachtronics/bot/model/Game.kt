package com.faendir.zachtronics.bot.model

import com.faendir.zachtronics.bot.discord.commands.arg.Argument
import com.faendir.zachtronics.bot.leaderboards.Leaderboard

interface Game<C : Category<C, S, P>, S : Score, P : Puzzle> {
    val discordChannel: String

    val leaderboards: List<Leaderboard<C, S, P>>

    fun findPuzzleByName(name: String): List<P>

    fun parseScore(string: String): S?
}