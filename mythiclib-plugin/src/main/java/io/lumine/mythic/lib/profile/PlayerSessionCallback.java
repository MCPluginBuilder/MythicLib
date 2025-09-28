package io.lumine.mythic.lib.profile;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface PlayerSessionCallback {

    public void callback(@NotNull PlayerSession session);
}
