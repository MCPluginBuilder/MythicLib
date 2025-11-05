package io.lumine.mythic.lib.comp.profile;

import io.lumine.mythic.lib.profile.handler.LegacyProfileHandler;
import io.lumine.mythic.lib.profile.handler.NoProfileHandler;
import io.lumine.mythic.lib.profile.handler.ProfileHandler;
import io.lumine.mythic.lib.profile.handler.ProxyProfileHandler;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public enum ProfileMode {

    LEGACY(LegacyProfileHandler::new),

    PROXY(ProxyProfileHandler::new),

    NONE(NoProfileHandler::new);

    private final Supplier<ProfileHandler> profileHandlerSupplier;

    ProfileMode(Supplier<ProfileHandler> profileHandlerSupplier) {
        this.profileHandlerSupplier = profileHandlerSupplier;
    }

    @NotNull
    public ProfileHandler newProfileHandler() {
        return this.profileHandlerSupplier.get();
    }
}
