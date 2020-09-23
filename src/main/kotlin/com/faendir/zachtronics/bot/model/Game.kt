package com.faendir.zachtronics.bot.model

import com.faendir.zachtronics.bot.leaderboards.Leaderboard

interface Game<L: Leaderboard, C : Category<C, S, P>, S : Score, P : Puzzle> {
    val discordChannel: String

    val leaderboards: List<L>

    fun findCategoryByName(name: String) : List<C>

    fun findPuzzleByName(name: String): List<P>

    fun parseScore(string: String): S?
}