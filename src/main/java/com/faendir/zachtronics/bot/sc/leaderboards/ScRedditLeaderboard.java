package com.faendir.zachtronics.bot.sc.leaderboards;

import com.faendir.zachtronics.bot.model.Leaderboard;
import com.faendir.zachtronics.bot.model.UpdateResult;
import com.faendir.zachtronics.bot.main.reddit.RedditService;
import com.faendir.zachtronics.bot.main.reddit.Subreddit;
import com.faendir.zachtronics.bot.sc.model.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.faendir.zachtronics.bot.sc.model.ScCategory.*;

@Component
@RequiredArgsConstructor
public class ScRedditLeaderboard implements Leaderboard<ScCategory, ScScore, ScPuzzle, ScRecord> {
    @Getter
    private final List<ScCategory> supportedCategories = Arrays.asList(ScCategory.values());
    private final RedditService redditService;

    @Nullable
    @Override
    public ScRecord get(@NotNull ScPuzzle puzzle, @NotNull ScCategory category) {
        String[] lines = redditService.getWikiPage(Subreddit.SPACECHEM, puzzle.getGroup().getWikiPage()).split("\\r?\\n");

        boolean needsReactors = category.name().startsWith("R") && puzzle.getType() != ScType.PRODUCTION_TRIVIAL;
        String puzzleHeader = Pattern.quote(puzzle.getDisplayName()) + (needsReactors ? " - \\d Reactors?" : "");
        Pattern puzzleRegex = Pattern.compile("^\\s*\\|\\s*" + puzzleHeader);

        for (String line : lines) {
            if (!puzzleRegex.matcher(line).find())
                continue;

            String[] pieces = line.trim().split("\\s*\\|\\s*");
            List<String> tableCols = Arrays.asList(pieces).subList(2, pieces.length);
            List<String> halfTable = category.getDisplayName().contains("C") ? tableCols
                    .subList(0, tableCols.size() / 2) : tableCols.subList(tableCols.size() / 2, tableCols.size());
            ScRecord[] records = new ScRecord[3];
            parseHalfTable(halfTable, records);

            int column = category.getDisplayName().endsWith("NP") ? 2 : category.getDisplayName()
                                                                                .endsWith("NB") ? 1 : 0;
            ScRecord record = records[column];
            if (record != null && record != ScRecord.IMPOSSIBLE_CATEGORY)
                return record;
            break;
        }
        return null;
    }

    @NotNull
    @Override
    public UpdateResult<ScCategory, ScScore> update(@NotNull ScPuzzle puzzle, @NotNull ScRecord record) {
        String[] lines = redditService.getWikiPage(Subreddit.SPACECHEM, puzzle.getGroup().getWikiPage()).split("\\r?\\n");
        Pattern puzzleRegex = Pattern.compile("^\\s*\\|\\s*" + Pattern.quote(puzzle.getDisplayName()));

        ScRecord[][] records = new ScRecord[4][3];
        int startingRow = -1;
        int seenRows = 0;
        int halfSize = -1;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (puzzleRegex.matcher(line).find()) {
                String[] pieces = line.trim().split("\\s*\\|\\s*");
                List<String> tableCols = Arrays.asList(pieces).subList(2, pieces.length);
                List<String> cyclesHalf = tableCols.subList(0, tableCols.size() / 2);
                parseHalfTable(cyclesHalf, records[2 * seenRows]);
                List<String> symbolsHalf = tableCols.subList(tableCols.size() / 2, tableCols.size());
                parseHalfTable(symbolsHalf, records[2 * seenRows + 1]);

                if (startingRow == -1) {
                    startingRow = i;
                    halfSize = tableCols.size() / 2;
                }
                seenRows++;
            }
            else if (startingRow != -1) {
                // we've already found the point and now we're past it, we're done
                break;
            }
        }

        ScCategory[][] categories = {{C,  CNB,  CNP},  {S,  SNB,  SNP},
                                     {RC, RCNB, RCNP}, {RS, RSNB, RSNP}};

        Map<ScCategory, ScScore> beatenScores = new EnumMap<>(ScCategory.class);
        // |Puzzle | [(**ccc**/r/ss) author](https://li.nk) | ← | [(ccc/r/**ss**) author](https://li.nk) | ←
        // |Puzzle - 1 Reactor | [(**ccc**/**r**/ss) author](https://li.nk) | ← | [(ccc/**r**/**ss**) author](https://li.nk) | ←
        for (int rowIdx = 0; rowIdx < seenRows; rowIdx++) {
            String[] prevElems = lines[startingRow + rowIdx].trim().split("\\s*\\|\\s*");
            StringBuilder row = new StringBuilder("|");
            int minReactors = Integer.MAX_VALUE;
            if (rowIdx == 1) {
                minReactors = Math.min(record.getScore().getReactors(), records[2][0].getScore().getReactors());
                row.append(puzzle.getDisplayName()).append(" - ").append(minReactors).append(" Reactor")
                   .append(minReactors == 1 ? "" : "s");
            }
            else {
                row.append(prevElems[1]);
            }

            for (int block = 0; block < 2; block++) {
                ScRecord[] blockRecords = records[2 * rowIdx + block];
                ScCategory[] blockCategories = categories[2 * rowIdx + block];
                elemloop:
                for (int i = 0; i < halfSize; i++) {
                    ScCategory thisCategory = blockCategories[i];
                    row.append(" | ");
                    if (thisCategory.supportsScore(record.getScore()) &&
                        thisCategory.getScoreComparator().compare(record.getScore(), blockRecords[i].getScore()) <= 0) {
                        beatenScores.put(thisCategory, blockRecords[i].getScore());
                        blockRecords[i] = record;

                        for (int prev = 0; prev < i; prev++) {
                            if (blockRecords[i] == blockRecords[prev]) {
                                row.append("←".repeat(i - prev));
                                continue elemloop;
                            }
                        }

                        if (blockRecords[i].getScore().getReactors() > minReactors)
                            row.append("† ");
                        row.append(makeLeaderboardString(blockRecords[i], thisCategory));
                    }
                    else {
                        row.append(prevElems[block * halfSize + i + 2]);
                    }
                }
            }
            lines[startingRow + rowIdx] = row.toString();
        }

        if (!beatenScores.isEmpty()) {
            redditService.updateWikiPage(Subreddit.SPACECHEM, puzzle.getGroup().getWikiPage(), String.join("\r\n", lines),
                                         puzzle.getDisplayName() + " " + record.getScore().toDisplayString() + " by " +
                                         record.getAuthor());
            return new UpdateResult.Success<>(beatenScores);
        }
        else {
            return new UpdateResult.BetterExists<>(Collections.emptyMap());
        }
    }

    @NotNull
    private static String makeLeaderboardString(ScRecord record, ScCategory category) {
        ScScore score = record.getScore();
        String cyclesStr = score.getCycles() >= 100000 ? NumberFormat.getNumberInstance(Locale.ROOT)
                                                                     .format(score.getCycles()) : Integer
                .toString(score.getCycles());
        return String.format("[(" + category.getFormatStringLb() + ") %s](%s)", cyclesStr,
                             record.isOldVideoRNG() ? "\\*" : "", score.getReactors(), score.getSymbols(),
                             record.getAuthor(), record.getLink());
    }

    @Nullable
    private static ScRecord parseLeaderboardRecord(String recordCell) {
        Pattern scoreRegex = Pattern.compile("(?:†\\s*)?\\[?\\((?<score>" + ScScore.REGEX_SIMPLE_SCORE +
                                             ")\\)\\s+(?<author>[^]]+)(?:]\\((?<link>[^)]+)\\).*?)?");
        Matcher m = scoreRegex.matcher(recordCell);
        if (m.matches()) {
            ScScore score = ScScore.parseSimpleScore(m);
            return new ScRecord(score, m.group("author"), m.group("link"), m.group("oldRNG") != null);
        }
        return null;
    }

    private static void parseHalfTable(List<String> halfTable, ScRecord[] records) {
        records[0] = parseLeaderboardRecord(halfTable.get(0));

        if (halfTable.get(1).startsWith("X"))
            records[1] = ScRecord.IMPOSSIBLE_CATEGORY;
        else if (halfTable.get(1).equals("←"))
            records[1] = records[0];
        else
            records[1] = parseLeaderboardRecord(halfTable.get(1));
        if (records[1] != null)
            records[1].getScore().setBugged(false);

        if (halfTable.size() == 3 && !halfTable.get(2).startsWith("X")) {
            if (halfTable.get(2).equals("←←"))
                records[2] = records[0];
            else if (halfTable.get(2).equals("←"))
                records[2] = records[1];
            else {
                records[2] = parseLeaderboardRecord(halfTable.get(2));
            }
            if (records[2] != null)
                records[2].getScore().setPrecognitive(false);
        }
        else {
            if (records[0] != null)
                records[0].getScore().setPrecognitive(false);
            if (records[1] != null)
                records[1].getScore().setPrecognitive(false);
            records[2] = ScRecord.IMPOSSIBLE_CATEGORY;
        }

        if (records[0] == null) // incomplete main score, there is always a certified bugless
            records[0] = records[1];
    }
}