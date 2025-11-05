package io.lumine.mythic.lib.profile;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ProfileSessionCallback {

    public void callback(@NotNull ProfileSession session);
}
