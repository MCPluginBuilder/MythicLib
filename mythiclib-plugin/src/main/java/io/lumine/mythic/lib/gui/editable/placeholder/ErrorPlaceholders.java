package io.lumine.mythic.lib.gui.editable.placeholder;

import org.jetbrains.annotations.NotNull;

public class ErrorPlaceholders extends Placeholders {

    @NotNull
    @Override
    public String parsePlaceholder(@NotNull String key) {
        return "???";
    }
}
