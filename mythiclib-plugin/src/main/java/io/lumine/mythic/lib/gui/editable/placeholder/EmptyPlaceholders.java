package io.lumine.mythic.lib.gui.editable.placeholder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EmptyPlaceholders extends Placeholders {

    @Nullable
    @Override
    public String parsePlaceholder(@NotNull String key) {
        return null;
    }
}
