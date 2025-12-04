package io.lumine.mythic.lib.damage.mitigation;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.player.cooldown.CooldownObject;
import io.lumine.mythic.lib.script.Script;
import io.lumine.mythic.lib.util.formula.NumericalExpression;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MitigationType implements CooldownObject {
    private final String id, cooldownPath;

    private final @NotNull Script onDamage;
    private final @Nullable Script preDamage;
    private final @Nullable NumericalExpression cooldownFormula, rollFormula;

    /**
     * Used for still calling old events from the API
     */
    private final LegacyMitigationType legacy;

    public MitigationType(@NotNull ConfigurationSection config) {
        this.id = config.getName();
        this.cooldownPath = "mitigation:" + id;

        this.legacy = config.contains("legacy") ? UtilityMethods.prettyValueOf(LegacyMitigationType::valueOf, config.getString("legacy"), "No legacy mitigation mechanic with ID %s") : null;
        this.cooldownFormula = config.contains("cooldown") ? NumericalExpression.compile(config.getString("cooldown")) : null;
        this.rollFormula = config.contains("roll") ? NumericalExpression.compile(config.getString("roll")) : null;

        this.onDamage = MythicLib.plugin.getSkills().loadScript(config.get("on_damage"));
        this.preDamage = config.contains("pre_damage") ? MythicLib.plugin.getSkills().loadScript(config.get("pre_damage")) : null;
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
