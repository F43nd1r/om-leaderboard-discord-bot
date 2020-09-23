package com.faendir.zachtronics.bot.model.sc;

import com.faendir.zachtronics.bot.model.Record;
import kotlinx.serialization.Serializable;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

@Serializable
@Value
public class ScRecord implements Record {
    ScCategory category;
    ScScore score;
    String author;
    String link;

    @NotNull
    @Override
    public String toDisplayString() {
        return category.getDisplayName() + " " + score.toDisplayString() + " by " + author + " " + link;
    }
}