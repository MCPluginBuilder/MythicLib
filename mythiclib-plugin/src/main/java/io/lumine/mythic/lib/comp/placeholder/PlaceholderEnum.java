package io.lumine.mythic.lib.comp.placeholder;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.SharedStat;
import io.lumine.mythic.lib.comp.placeholder.api.PlaceholderEntry;
import io.lumine.mythic.lib.comp.placeholder.api.PlaceholderMetadata;
import io.lumine.mythic.lib.manager.StatManager;
import io.lumine.mythic.lib.util.DefenseFormula;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;

public enum PlaceholderEnum implements PlaceholderEntry<MMOPlayerData> {

    //region Placeholders

    /**
     * Positive/negative space using the negative space resource pack
     */
    space_(false, "", meta -> UtilityMethods.getSpaceFont(Integer.parseInt(meta.params()))),

    defense_damage_reduction(meta -> {
        var defenseStat = meta.playerData.getStatMap().getStat("DEFENSE");
        var damageReduction = 100 - DefenseFormula.calculateDamage(false, defenseStat, 100);
        return MythicLib.plugin.getMMOConfig().decimal.format(damageReduction);
    }),

    raw_stat_(0, meta -> {
        var stat = UtilityMethods.enumName(meta.params());
        return String.valueOf(meta.playerData.getStatMap().getInstance(stat).getFinal());
    }),

    stat_(0, meta -> {
        var stat = UtilityMethods.enumName(meta.params());
        return StatManager.format(stat, meta.playerData);
    }),

    cooldown_(0, meta -> {
        var key = meta.params();
        var cooldownLeft = meta.playerData.getCooldownMap().getCooldown(key);
        return MythicLib.plugin.getMMOConfig().decimal.format(cooldownLeft);
    }),

    decfmt_(false, "", meta -> {
        var split = meta.params().split("_", 2);
        var format = MythicLib.plugin.getMMOConfig().newDecimalFormat(split[0]);
        var value = Double.parseDouble(split[1]);
        return format.format(value);
    }),

    round_(false, 0, meta -> {
        var split = meta.params().split("_", 2);
        var places = Integer.parseInt(split[0]);
        var value = Double.parseDouble(split[1]);
        if (places == 0) return String.valueOf(Math.round(value));
        Validate.isTrue(places > 0, "Decimal places must be non-negative");
        return String.format("%." + places + "f", value);
    }),

    /**
     * Current mana
     */
    mana(0, meta -> StatManager.format(SharedStat.MAX_MANA, meta.playerData.getResources().getMana())),

    /**
     * Current mana as bar
     */
    mana_bar(meta -> {
        var current = meta.playerData.getResources().getMana();
        var max = meta.playerData.getStatMap().getStat(SharedStat.MAX_MANA);
        return MythicLib.plugin.getMMOConfig().manaDisplay.generateBar(current, max);
    }),

    /**
     * Current stamina
     */
    stamina(0, meta -> StatManager.format(SharedStat.MAX_STAMINA, meta.playerData.getResources().getStamina())),

    /**
     * Current stamina as bar
     */
    stamina_bar(meta -> {
        var current = meta.playerData.getResources().getStamina();
        var max = meta.playerData.getStatMap().getStat(SharedStat.MAX_STAMINA);
        return MythicLib.plugin.getMMOConfig().staminaDisplay.generateBar(current, max);
    }),

    //endregion

    ;

    private final String prefix, fallback;
    private final boolean requiresPlayer;
    private final Function<PlaceholderMetadata<MMOPlayerData>, String> parser;

    private static final String DEFAULT_FALLBACK = "";

    PlaceholderEnum(Function<PlaceholderMetadata<MMOPlayerData>, String> parser) {
        this(DEFAULT_FALLBACK, parser);
    }

    PlaceholderEnum(Object fallback, Function<PlaceholderMetadata<MMOPlayerData>, String> parser) {
        this(true, fallback, parser);
    }

    PlaceholderEnum(boolean requiresPlayer, Object fallback, Function<PlaceholderMetadata<MMOPlayerData>, String> parser) {
        this.requiresPlayer = requiresPlayer;
        this.prefix = name();
        this.parser = parser;
        this.fallback = String.valueOf(Objects.requireNonNull(fallback, "Default value cannot be null"));
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public String getFallback() {
        return fallback;
    }

    @Override
    public boolean requiresPlayer() {
        return requiresPlayer;
    }

    @Override
    public @NotNull String parse(@NotNull PlaceholderMetadata<MMOPlayerData> placeholderMetadata) {
        return this.parser.apply(placeholderMetadata);
    }
}
