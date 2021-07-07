package com.faendir.zachtronics.bot.sz.discord;

import com.faendir.discord4j.command.annotation.OptionConverter;
import com.faendir.zachtronics.bot.sc.model.ScPuzzle;
import com.faendir.zachtronics.bot.sc.model.SpaceChem;
import com.faendir.zachtronics.bot.sz.model.ShenzhenIO;
import com.faendir.zachtronics.bot.sz.model.SzPuzzle;
import discord4j.core.event.domain.interaction.InteractionCreateEvent;
import discord4j.core.object.command.Interaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.util.Optional;

public class SzPuzzleConverter implements OptionConverter<SzPuzzle> {
    @NotNull
    @Override
    public Mono<Optional<SzPuzzle>> fromString(@NotNull InteractionCreateEvent context, @Nullable String s) {
        if(s == null) throw new IllegalArgumentException();
        return Mono.fromCallable(() -> Optional.of(ShenzhenIO.parsePuzzle(s)));
    }
}
