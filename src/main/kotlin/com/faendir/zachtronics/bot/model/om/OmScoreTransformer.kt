package com.faendir.zachtronics.bot.model.om

import com.faendir.zachtronics.bot.discord.commands.ArgumentIntermediateMap
import com.faendir.zachtronics.bot.discord.commands.arg.PuzzleArgument
import com.faendir.zachtronics.bot.discord.commands.arg.ScoreTransformer
import com.faendir.zachtronics.bot.model.om.OmScorePart.*
import com.faendir.zachtronics.bot.model.om.OmType.PRODUCTION
import com.faendir.zachtronics.bot.utils.ResultOrMessage
import org.springframework.stereotype.Component

@Component
class OmScoreTransformer : ScoreTransformer<OmScore> {
    override val type: Class<OmScore> = OmScore::class.java

    override fun transform(args: ArgumentIntermediateMap, t: OmScore): OmScore {
        val puzzle = (args.findKey<PuzzleArgument>()?.getActualResult(args) as? ResultOrMessage.Success)?.result
        if (puzzle != null && t.parts.keys.contains(AREA_OR_INSTRUCTIONS)) {
            return OmScore((t.parts.filterNot { it.key == AREA_OR_INSTRUCTIONS }
                .map { it.key to it.value } + ((if (puzzle.type == PRODUCTION) INSTRUCTIONS else AREA) to t.parts[AREA_OR_INSTRUCTIONS]!!)).toMap(LinkedHashMap()))
        }
        return t;
    }

}