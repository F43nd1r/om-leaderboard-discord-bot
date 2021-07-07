package com.faendir.zachtronics.bot.sz.discord;

import com.faendir.discord4j.command.annotation.ApplicationCommand;
import com.faendir.discord4j.command.annotation.Converter;
import com.faendir.zachtronics.bot.generic.discord.AbstractListCommand;
import com.faendir.zachtronics.bot.model.Leaderboard;
import com.faendir.zachtronics.bot.sz.discord.SzListCommand$DataParser;
import com.faendir.zachtronics.bot.sz.discord.SzPuzzleConverter;
import com.faendir.zachtronics.bot.sz.model.SzCategory;
import com.faendir.zachtronics.bot.sz.model.SzPuzzle;
import com.faendir.zachtronics.bot.sz.model.SzRecord;
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
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class SzListCommand extends AbstractListCommand<SzCategory, SzPuzzle, SzRecord> {
    @Getter
    private final List<Leaderboard<SzCategory, SzPuzzle, SzRecord>> leaderboards;

    @NotNull
    @Override
    public ApplicationCommandOptionData buildData() {
        return SzListCommand$DataParser.buildData();
    }

    @NotNull
    @Override
    public Mono<Tuple2<SzPuzzle, List<SzCategory>>> findPuzzleAndCategories(@NotNull InteractionCreateEvent interaction) {
        return SzListCommand$DataParser.parse(interaction)
                                       .map(Data::getPuzzle)
                                       .map(puzzle -> Tuples.of(puzzle, Arrays.stream(SzCategory.values())
                                                                              .filter(c -> c.supportsPuzzle(puzzle))
                                                                              .collect(Collectors.toList())));
    }

    @ApplicationCommand(name = "list", subCommand = true)
    @Value
    public static class Data {
        @NonNull SzPuzzle puzzle;

        public Data(@Converter(SzPuzzleConverter.class) @NonNull SzPuzzle puzzle) {
            this.puzzle = puzzle;
        }
    }
}
