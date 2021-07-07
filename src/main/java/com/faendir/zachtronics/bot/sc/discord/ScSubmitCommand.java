package com.faendir.zachtronics.bot.sc.discord;

import com.faendir.discord4j.command.annotation.ApplicationCommand;
import com.faendir.discord4j.command.annotation.Converter;
import com.faendir.zachtronics.bot.generic.discord.AbstractSubmitCommand;
import com.faendir.zachtronics.bot.generic.discord.LinkConverter;
import com.faendir.zachtronics.bot.model.Leaderboard;
import com.faendir.zachtronics.bot.sc.archive.ScArchive;
import com.faendir.zachtronics.bot.sc.model.ScPuzzle;
import com.faendir.zachtronics.bot.sc.model.ScRecord;
import com.faendir.zachtronics.bot.sc.model.ScScore;
import com.faendir.zachtronics.bot.sc.model.ScSolution;
import discord4j.core.event.domain.interaction.InteractionCreateEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.Interaction;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ScSubmitCommand extends AbstractSubmitCommand<ScPuzzle, ScRecord> {
    private final ScArchive archive;
    @Getter
    private final List<Leaderboard<?, ScPuzzle, ScRecord>> leaderboards;

    @NotNull
    @Override
    public Mono<Tuple2<ScPuzzle, ScRecord>> parseSubmission(@NotNull InteractionCreateEvent interaction) {
        return ScSubmitCommand$DataParser.parse(interaction).map(data -> {
            ScScore score = ScScore.parseBPScore(data.score);
            if (score == null)
                throw new IllegalArgumentException("Couldn't parse score: \"" + data.score + "\"");
            // we also archive the score here
            ScSolution solution = new ScSolution(data.puzzle, score);
            archive.archive(solution);
            ScRecord record = new ScRecord(score, data.author, data.link, false);
            return Tuples.of(data.puzzle, record);
        });
    }

    @NotNull
    @Override
    public ApplicationCommandOptionData buildData() {
        return ScSubmitCommand$DataParser.buildData();
    }

    @ApplicationCommand(name = "submit", subCommand = true)
    @Value
    public static class Data {
        @NonNull ScPuzzle puzzle;
        @NotNull String score;
        @NotNull String author;
        @NotNull String link;

        public Data(@Converter(ScPuzzleConverter.class) @NonNull ScPuzzle puzzle, @NonNull String score,
                    @NotNull String author, @NotNull @Converter(LinkConverter.class) String link) {
            this.puzzle = puzzle;
            this.score = score;
            this.link = link;
            this.author = author;
        }
    }
}
