package com.faendir.zachtronics.bot.sc.discord;

import com.faendir.discord4j.command.annotation.ApplicationCommand;
import com.faendir.discord4j.command.annotation.Converter;
import com.faendir.zachtronics.bot.generic.discord.AbstractListCommand;
import com.faendir.zachtronics.bot.generic.discord.AbstractShowCommand;
import com.faendir.zachtronics.bot.model.Leaderboard;
import com.faendir.zachtronics.bot.sc.model.ScCategory;
import com.faendir.zachtronics.bot.sc.model.ScPuzzle;
import com.faendir.zachtronics.bot.sc.model.ScRecord;
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
public class ScListCommand extends AbstractListCommand<ScCategory, ScPuzzle, ScRecord> {
    @Getter
    private final List<Leaderboard<ScCategory, ScPuzzle, ScRecord>> leaderboards;

    @NotNull
    @Override
    public ApplicationCommandOptionData buildData() {
        return ScListCommand$DataParser.buildData();
    }

    @NotNull
    @Override
    public Mono<Tuple2<ScPuzzle, List<ScCategory>>> findPuzzleAndCategories(@NotNull InteractionCreateEvent interaction) {
        return ScListCommand$DataParser.parse(interaction)
                                       .map(Data::getPuzzle)
                                       .map(puzzle -> Tuples.of(puzzle, Arrays.stream(ScCategory.values())
                                                                              .filter(c -> c.supportsPuzzle(puzzle))
                                                                              .collect(Collectors.toList())));
    }

    @ApplicationCommand(name = "list", subCommand = true)
    @Value
    public static class Data {
        @NonNull ScPuzzle puzzle;

        public Data(@Converter(ScPuzzleConverter.class) @NonNull ScPuzzle puzzle) {
            this.puzzle = puzzle;
        }
    }
}
