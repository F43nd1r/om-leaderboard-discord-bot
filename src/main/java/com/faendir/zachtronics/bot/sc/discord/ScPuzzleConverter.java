package com.faendir.zachtronics.bot.sc.discord;

import com.faendir.discord4j.command.annotation.OptionConverter;
import com.faendir.zachtronics.bot.sc.model.ScPuzzle;
import com.faendir.zachtronics.bot.sc.model.SpaceChem;
import discord4j.core.event.domain.interaction.InteractionCreateEvent;
import discord4j.core.object.command.Interaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.util.Optional;

public class ScPuzzleConverter implements OptionConverter<ScPuzzle> {
    @NotNull
    @Override
    public Mono<Optional<ScPuzzle>> fromString(@NotNull InteractionCreateEvent context, @Nullable String s) {
        if(s == null) throw new IllegalArgumentException();
        return Mono.fromCallable(() -> Optional.of(SpaceChem.parsePuzzle(s)));
    }
}
