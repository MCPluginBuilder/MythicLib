package io.lumine.mythic.lib.hologram;

import io.lumine.mythic.lib.hologram.factory.*;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public enum HologramFactoryList {
    CORETOOLS("CoreTools", CoreToolsHologramFactory::new),
    TEXT_DISPLAYS("TextDisplays", BukkitHologramFactory::new),
    CMI("CMI", CMIHologramFactory::new),
    DECENT_HOLOGRAMS("DecentHolograms", DecentHologramFactory::new),
    HOLOGRAPHIC_DISPLAYS("HolographicDisplays", HDHologramFactory::new),
    HOLOGRAMS("Holograms", HologramsHologramFactory::new),
    LEGACY_ARMOR_STANDS("LegacyArmorStands", LegacyBukkitHologramFactory::new),
    TR_HOLOGRAM("TrHologram", TrHologramFactory::new),
    ;

    private final String name;
    private final Supplier<HologramFactory> factoryProvider;

    private HologramFactoryList(@NotNull String name, @NotNull Supplier<HologramFactory> factoryProvider) {
        this.name = name;
        this.factoryProvider = factoryProvider;
    }

    public String getName() {
        return name;
    }

    public HologramFactory provide() {
        return factoryProvider.get();
    }
}
