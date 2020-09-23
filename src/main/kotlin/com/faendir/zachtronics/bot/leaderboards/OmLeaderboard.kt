package com.faendir.zachtronics.bot.leaderboards

import com.faendir.zachtronics.bot.discord.commands.Show
import com.faendir.zachtronics.bot.discord.commands.Submit
import com.faendir.zachtronics.bot.model.Author
import com.faendir.zachtronics.bot.model.Link
import com.faendir.zachtronics.bot.model.Record
import com.faendir.zachtronics.bot.model.om.OmCategory
import com.faendir.zachtronics.bot.model.om.OmPuzzle
import com.faendir.zachtronics.bot.model.om.OmScore

interface OmLeaderboard : Leaderboard {
    override val supportedCategories: Collection<OmCategory>

    @Show
    fun get(puzzle: OmPuzzle, category: OmCategory): Record?

    @Submit
    fun update(user: Author, puzzle: OmPuzzle, categories: List<OmCategory>, score: OmScore, link: Link): UpdateResult
}