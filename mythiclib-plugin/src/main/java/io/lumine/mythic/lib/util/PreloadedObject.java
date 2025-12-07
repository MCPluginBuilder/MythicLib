package io.lumine.mythic.lib.util;

import org.jetbrains.annotations.NotNull;

/**
 * @deprecated Now completely useless
 */
@Deprecated
public interface PreloadedObject {

    @NotNull
    PostLoadAction getPostLoadAction();
}
