package com.faendir.zachtronics.bot.om.model

import com.faendir.zachtronics.bot.model.Game
import com.faendir.zachtronics.bot.utils.Result
import com.faendir.zachtronics.bot.utils.Result.Companion.parseFailure
import com.faendir.zachtronics.bot.utils.Result.Companion.success
import com.faendir.zachtronics.bot.utils.getSingleMatchingPuzzle
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import org.springframework.stereotype.Component

@Component
class OpusMagnum : Game<OmCategory, OmScore, OmPuzzle, OmRecord> {

    override val discordChannel = "opus-magnum"

    override val displayName = "Opus Magnum"

    override fun parsePuzzle(name: String): Result<OmPuzzle> = OmPuzzle.values().getSingleMatchingPuzzle(name)

    internal fun parseScore(puzzle: OmPuzzle, string: String): Result<OmScore> {
        if (string.isBlank()) return parseFailure("I didn't find a score in your command.")
        val parts = string.split('/')
        if (parts.size < 3) return parseFailure("your score must have at least three parts.")
        if (string.contains(Regex("[a-zA-Z]"))) {
            return success(OmScore((parts.map { OmScorePart.parse(it) ?: return parseFailure("I didn't understand \"$it\".") })))
        }
        if (parts.size == 4) {
            return success(OmScore(OmScorePart.COST to parts[0].toDouble(),
                OmScorePart.CYCLES to parts[1].toDouble(),
                OmScorePart.AREA to parts[2].toDouble(),
                OmScorePart.INSTRUCTIONS to parts[3].toDouble()))
        }
        if (parts.size == 3) {
            return success(OmScore(OmScorePart.COST to parts[0].toDouble(),
                OmScorePart.CYCLES to parts[1].toDouble(),
                (if (puzzle.type == OmType.PRODUCTION) OmScorePart.INSTRUCTIONS else OmScorePart.AREA) to parts[2].toDouble()))
        }
        return parseFailure("you need to specify score part identifiers when using more than four values.")
    }

    internal fun findLink(command: MatchResult, message: Message): Result<String> {
        return (command.groups["link"]?.value ?: message.attachments.firstOrNull()?.url)?.let { success(it) }
            ?: parseFailure("I could not find a valid link or attachment in your message.")
    }

    override fun parseCategory(name: String): List<OmCategory> =
        OmCategory.values().filter { category -> category.displayNames.any { it.equals(name, ignoreCase = true) } }

    override fun hasWritePermission(member: Member?): Boolean = member?.roles?.any { it.name == "trusted-leaderboard-poster" } ?: false
}