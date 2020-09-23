package com.faendir.zachtronics.bot.model.sc;

import com.faendir.zachtronics.bot.model.Type;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ScType implements Type {
    RESEARCH("research"),
    PRODUCTION("production"),
    PRODUCTION_TRIVIAL("production"),
    BOSS("boss");

    private final String displayName;
}