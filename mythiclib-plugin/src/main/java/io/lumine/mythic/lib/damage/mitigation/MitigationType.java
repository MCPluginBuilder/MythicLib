package io.lumine.mythic.lib.damage.mitigation;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.player.cooldown.CooldownObject;
import io.lumine.mythic.lib.script.Script;
import io.lumine.mythic.lib.util.PostLoadAction;
import io.lumine.mythic.lib.util.formula.NumericalExpression;
import io.lumine.mythic.lib.util.formula.PrecompiledNumericalExpression;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MitigationType implements CooldownObject {
    private final String id, cooldownPath;

    private @NotNull Script onDamage;
    private @Nullable Script preDamage;
    private final @Nullable NumericalExpression cooldownFormula, rollFormula;

    private final PostLoadAction postLoadAction = new PostLoadAction(config -> {
        this.onDamage = MythicLib.plugin.getSkills().loadScript(config.get("on_damage"));
        if (config.contains("pre_damage"))
            this.preDamage = MythicLib.plugin.getSkills().loadScript(config.get("pre_damage"));
    });

    /**
     * Used for still calling old events from the API
     */
    private final LegacyMitigationType legacy;

    public MitigationType(@NotNull ConfigurationSection config) {
        this.id = config.getName();
        this.cooldownPath = "mitigation:" + id;

        this.postLoadAction.cacheConfig(config);

        this.legacy = config.contains("legacy") ? UtilityMethods.prettyValueOf(LegacyMitigationType::valueOf, config.getString("legacy"), "No legacy mitigation mechanic with ID %s") : null;
        this.cooldownFormula = config.contains("cooldown") ? NumericalExpression.compile(config.getString("cooldown")) : null;
        this.rollFormula = config.contains("roll") ? NumericalExpression.compile(config.getString("roll")) : null;
    }

    public PostLoadAction getPostLoadAction() {
        return postLoadAction;
    }

    @Nullable
    public NumericalExpression getCooldown() {
        return cooldownFormula;
    }

    public boolean hasCooldown() {
        return cooldownFormula != null;
    }

    @Nullable
    public NumericalExpression getRoll() {
        return rollFormula;
    }

    @NotNull
    public Script onDamage() {
        return onDamage;
    }

    @Nullable
    public Script preDamage() {
        return preDamage;
    }

    @NotNull
    public String getId() {
        return id;
    }

    @Nullable
    public LegacyMitigationType asLegacy() {
        return legacy;
    }

    @Override
    public String getCooldownPath() {
        return cooldownPath;
    }
}
