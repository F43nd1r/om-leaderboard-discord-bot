package com.faendir.zachtronics.bot.model.om

import com.faendir.zachtronics.bot.leaderboards.git.GitLeaderboard
import com.faendir.zachtronics.bot.leaderboards.reddit.RedditLeaderboard
import com.faendir.zachtronics.bot.model.Game
import org.springframework.stereotype.Component

@Component
class OpusMagnum(gitLeaderboard: GitLeaderboard, redditLeaderboard: RedditLeaderboard) : Game<OmCategory, OmScore, OmPuzzle> {
    override val discordChannel = "opus-magnum"
    override fun findPuzzleByName(name: String) = OmPuzzle.values().filter { it.displayName.contains(name, ignoreCase = true) }
    override fun parseScore(string: String): OmScore? {
        if (string.isBlank()) return null
        val parts = string.split('/')
        if (parts.size < 3) return null
        if (string.contains(Regex("[a-zA-Z]"))) {
            return OmScore((parts.map { OmScorePart.parse(it) ?: return null }).toMap(LinkedHashMap()))
        }
        try {
            if (parts.size == 4) {
                return OmScore(linkedMapOf(OmScorePart.COST to parts[0].toDouble(),
                    OmScorePart.CYCLES to parts[1].toDouble(),
                    OmScorePart.AREA to parts[2].toDouble(),
                    OmScorePart.INSTRUCTIONS to parts[3].toDouble()))
            }
            if (parts.size == 3) {
                return OmScore(linkedMapOf(OmScorePart.COST to parts[0].toDouble(),
                    OmScorePart.CYCLES to parts[1].toDouble(),
                    OmScorePart.AREA_OR_INSTRUCTIONS to parts[2].toDouble()))
            }
        } catch (e: NumberFormatException) {
            return null
        }
        return null
    }

    override val leaderboards = listOf(gitLeaderboard, redditLeaderboard)
}