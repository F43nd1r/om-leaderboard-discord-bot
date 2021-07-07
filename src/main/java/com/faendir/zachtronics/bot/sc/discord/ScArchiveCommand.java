package com.faendir.zachtronics.bot.sc.discord;

import com.faendir.discord4j.command.annotation.ApplicationCommand;
import com.faendir.discord4j.command.annotation.Converter;
import com.faendir.zachtronics.bot.generic.discord.AbstractArchiveCommand;
import com.faendir.zachtronics.bot.generic.discord.LinkConverter;
import com.faendir.zachtronics.bot.sc.archive.ScArchive;
import com.faendir.zachtronics.bot.sc.model.ScPuzzle;
import com.faendir.zachtronics.bot.sc.model.ScScore;
import com.faendir.zachtronics.bot.sc.model.ScSolution;
import discord4j.core.event.domain.interaction.InteractionCreateEvent;
import discord4j.core.object.command.Interaction;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

@RequiredArgsConstructor
@Component
public class ScArchiveCommand extends AbstractArchiveCommand<ScSolution> {
    @Getter
    private final ScArchive archive;

    @NotNull
    @Override
    public Mono<ScSolution> parseSolution(@NotNull InteractionCreateEvent interaction) {
        return ScArchiveCommand$DataParser.parse(interaction).map(data -> {
            ScScore score = null;
            if (data.score != null) {
                score = ScScore.parseBPScore(data.score);
                if (score == null) throw new IllegalArgumentException("couldn't parse score");
            }

            ScSolution solution;
            if (data.link != null) {
                try (InputStream is = new URL(data.link).openStream()) {
                    String content = new String(is.readAllBytes());
                    solution = new ScSolution(data.puzzle, score, content);
                } catch (MalformedURLException e) {
                    throw new IllegalArgumentException("Could not parse your link");
                } catch (IOException e) {
                    throw new IllegalArgumentException("Couldn't read your solution");
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Could not parse a valid solution");
                }
            } else {
                if (score == null) {
                    throw new IllegalArgumentException("Need a link or a score (or both), I found neither");
                }
                solution = new ScSolution(data.puzzle, score);
            }
            return solution;
        });
    }

    @NotNull
    @Override
    public ApplicationCommandOptionData buildData() {
        return ScArchiveCommand$DataParser.buildData();
    }

    @ApplicationCommand(name = "archive", subCommand = true)
    @Value
    public static class Data {
        @NonNull ScPuzzle puzzle;
        String score;
        String link;

        public Data(@Converter(ScPuzzleConverter.class) @NonNull ScPuzzle puzzle, String score, @Converter(LinkConverter.class) String link) {
            this.puzzle = puzzle;
            this.score = score;
            this.link = link;
        }
    }
}
