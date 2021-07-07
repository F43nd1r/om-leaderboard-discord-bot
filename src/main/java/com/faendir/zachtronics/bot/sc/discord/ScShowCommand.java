package com.faendir.zachtronics.bot.sc.discord;

import com.faendir.discord4j.command.annotation.ApplicationCommand;
import com.faendir.discord4j.command.annotation.Converter;
import com.faendir.zachtronics.bot.generic.discord.AbstractShowCommand;
import com.faendir.zachtronics.bot.model.Leaderboard;
import com.faendir.zachtronics.bot.sc.model.ScCategory;
import com.faendir.zachtronics.bot.sc.model.ScPuzzle;
import com.faendir.zachtronics.bot.sc.model.ScRecord;
import discord4j.core.event.domain.interaction.InteractionCreateEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
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

import java.util.List;

@RequiredArgsConstructor
@Component
public class ScShowCommand extends AbstractShowCommand<ScCategory, ScPuzzle, ScRecord> {
    @Getter
    private final List<Leaderboard<ScCategory, ScPuzzle, ScRecord>> leaderboards;

    @NotNull
    @Override
    public Mono<Tuple2<ScPuzzle, ScCategory>> findPuzzleAndCategory(@NotNull InteractionCreateEvent interaction) {
        return ScShowCommand$DataParser.parse(interaction).map(data -> Tuples.of(data.puzzle, data.category));
    }

    @NotNull
    @Override
    public ApplicationCommandOptionData buildData() {
        return ScShowCommand$DataParser.buildData();
    }

    @ApplicationCommand(name = "show", subCommand = true)
    @Value
    public static class Data {
        @NonNull ScPuzzle puzzle;
        @NonNull ScCategory category;

        public Data(@Converter(ScPuzzleConverter.class) @NonNull ScPuzzle puzzle, @NonNull ScCategory category) {
            this.puzzle = puzzle;
            this.category = category;
        }
    }
}
