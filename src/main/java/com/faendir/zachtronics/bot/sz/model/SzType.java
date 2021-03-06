package com.faendir.zachtronics.bot.sz.model;

import com.faendir.zachtronics.bot.model.Type;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SzType implements Type {
    STANDARD("standard"),
    SANDBOX("sandbox");

    private final String displayName;
}