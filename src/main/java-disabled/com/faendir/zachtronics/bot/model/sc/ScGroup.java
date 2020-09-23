package com.faendir.zachtronics.bot.model.sc;

import com.faendir.zachtronics.bot.model.Group;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ScGroup implements Group {
    MAIN("Main Game", "index"),
    TF2("TF2", "index"),
    CORVI63("63 Corvi", "index"),
    RESEARCHNET1("ResearchNet", "researchnet"),
    RESEARCHNET2("ResearchNet", "researchnet2");

    private final String displayName;
    private final String wikiPage;
}
