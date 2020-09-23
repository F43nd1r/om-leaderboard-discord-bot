package com.faendir.zachtronics.bot.model.sc;

import com.faendir.zachtronics.bot.model.Score;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

@Value
public class ScScore implements Score {
    int cycles;
    int reactors;
    int symbols;

    String contentDescription = "c/r/s";

    @NotNull
    @Override
    public String toDisplayString() {
        return cycles + "/" + reactors + "/" + symbols;
    }
}
