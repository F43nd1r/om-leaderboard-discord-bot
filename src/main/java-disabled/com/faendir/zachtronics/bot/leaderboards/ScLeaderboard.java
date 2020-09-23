package com.faendir.zachtronics.bot.leaderboards;

import com.faendir.zachtronics.bot.model.Record;
import com.faendir.zachtronics.bot.model.sc.*;
import com.faendir.zachtronics.bot.reddit.RedditService;
import com.faendir.zachtronics.bot.reddit.Subreddit;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dean.jraw.models.WikiPage;
import net.dean.jraw.references.SubredditReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class ScLeaderboard implements Leaderboard<ScCategory, ScScore, ScPuzzle> {
    @Getter
    private final Collection<ScCategory> supportedCategories = Arrays.asList(ScCategory.values());

    private final RedditService redditService;

    @Nullable
    @Override
    public Record get(@NotNull ScPuzzle puzzle, @NotNull ScCategory category) {
        boolean needsReactors = category.name().startsWith("R") && puzzle.getType() != ScType.PRODUCTION_TRIVIAL;
        String puzzleHeader = Pattern.quote(puzzle.getDisplayName()) + (needsReactors ? " - \\d Reactors?" : "");
        Pattern puzzleRegex = Pattern.compile(puzzleHeader);

        SubredditReference sc = redditService.subreddit(Subreddit.SPACECHEM);
        WikiPage page = sc.wiki().page(puzzle.getGroup().getWikiPage());
        String[] lines = page.getContent().split("\\r?\\n");

        for (String line : lines) {
            if (!puzzleRegex.matcher(line).find())
                continue;
            String[] tableCols = line.substring(1).split("\\s*\\|\\s*");
            int column = findColumn(category, tableCols.length - 1);
            String scoreCell = tableCols[column];
            if (scoreCell.equals("←"))
                scoreCell = tableCols[column - 1];
            else if (scoreCell.equals("←←"))
                scoreCell = tableCols[column - 2];
            Pattern scoreRegex = Pattern.compile("(?:†\\s*)?\\[?(?<score>" + SpaceChem.SCORE_REGEX.pattern() +
                                                 ")\\s+(?<author>[^]]+)(?:]\\((?<link>[^)]+)\\).*?)?");
            Matcher m = scoreRegex.matcher(scoreCell);
            if (m.matches()) {
                ScScore score = SpaceChem.parseScore(m.group("score"));
                return new ScRecord(category, score, m.group("author"), m.group("link"));
            }
            break;
        }
        return null;
    }

    private static int findColumn(ScCategory category, int numCols) {
        assert (numCols == 4) || (numCols == 6);
        switch (category) {
            case CYCLES:
            case RC:
                return 1;
            case SYMBOLS:
            case RS:
                return numCols / 2 + 1;
            case CNB:
            case RCNB:
                return 2;
            case SNB:
            case RSNB:
                return numCols / 2 + 2;
            case CNP:
            case RCNP:
                return 3;
            case SNP:
            case RSNP:
                return numCols / 2 + 3;
            default:
                throw new IllegalArgumentException();
        }
    }
}
