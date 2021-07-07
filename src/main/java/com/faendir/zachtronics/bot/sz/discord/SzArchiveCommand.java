package com.faendir.zachtronics.bot.sz.discord;

import com.faendir.discord4j.command.annotation.ApplicationCommand;
import com.faendir.discord4j.command.annotation.Converter;
import com.faendir.zachtronics.bot.generic.discord.AbstractArchiveCommand;
import com.faendir.zachtronics.bot.generic.discord.LinkConverter;
import com.faendir.zachtronics.bot.sz.archive.SzArchive;
import com.faendir.zachtronics.bot.sz.model.SzPuzzle;
import com.faendir.zachtronics.bot.sz.model.SzSolution;
import discord4j.core.event.domain.interaction.InteractionCreateEvent;
import discord4j.core.object.command.Interaction;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import lombok.Getter;
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
public class SzArchiveCommand extends AbstractArchiveCommand<SzSolution> {
    @Getter
    private final SzArchive archive;

    @NotNull
    @Override
    public Mono<SzSolution> parseSolution(@NotNull InteractionCreateEvent interaction) {
        return SzArchiveCommand$DataParser.parse(interaction).map(data -> {
            SzSolution solution;
            try (InputStream is = new URL(data.link).openStream()) {
                String content = new String(is.readAllBytes());
                solution = new SzSolution(content);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("Could not parse your link");
            } catch (IOException e) {
                throw new IllegalArgumentException("Couldn't read your solution");
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Could not parse a valid solution");
            }
            return solution;
        });
    }

    @NotNull
    @Override
    public ApplicationCommandOptionData buildData() {
        return SzArchiveCommand$DataParser.buildData();
    }

    @ApplicationCommand(name = "archive", subCommand = true)
    @Value
    public static class Data {
        @NotNull SzPuzzle puzzle;
        @NotNull String link;

        public Data(@Converter(SzPuzzleConverter.class) @NotNull SzPuzzle puzzle, @NotNull @Converter(LinkConverter.class) String link) {
            this.puzzle = puzzle;
            this.link = link;
        }
    }
}
