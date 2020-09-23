package com.faendir.zachtronics.bot.leaderboards

import com.faendir.zachtronics.bot.model.Category
import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.model.Score

interface Leaderboard {
    val supportedCategories: Collection<Category<*, *, *>>
}

sealed class UpdateResult {
    class Success(val oldScores: Map<Category<*, *, *>, Score?>) : UpdateResult()

    class ParetoUpdate : UpdateResult()

    class BetterExists(val scores: Map<Category<*, *, *>, Score>) : UpdateResult()

    class BrokenLink : UpdateResult()
}

