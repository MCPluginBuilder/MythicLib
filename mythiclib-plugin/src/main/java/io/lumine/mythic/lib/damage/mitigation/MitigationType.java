package io.lumine.mythic.lib.damage.mitigation;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.player.cooldown.CooldownObject;
import io.lumine.mythic.lib.script.util.expression.numeric.NumericExpression;
import io.lumine.mythic.lib.skill.SimpleSkill;
import io.lumine.mythic.lib.skill.Skill;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MitigationType implements CooldownObject {
    private final String id, cooldownPath;
    private final boolean skipEvent;

    private final @NotNull Skill onDamage;
    private final @Nullable Skill preDamage;
    private final @Nullable NumericExpression cooldownFormula, rollFormula;

    /**
     * Used for still calling old events from the API
     */
    private final LegacyMitigationType legacy;

    public MitigationType(@NotNull ConfigurationSection config) {
        this.id = config.getName();
        this.cooldownPath = "mitigation:" + id;
        this.skipEvent = config.getBoolean("skip_event", false);

        this.legacy = config.contains("legacy") ? UtilityMethods.prettyValueOf(LegacyMitigationType::valueOf, config.getString("legacy"), "No legacy mitigation mechanic with ID %s") : null;
        this.cooldownFormula = config.contains("cooldown") ? NumericExpression.compile(config.getString("cooldown")) : null;
        this.rollFormula = config.contains("roll") ? NumericExpression.compile(config.getString("roll")) : null;

        this.onDamage = new SimpleSkill(MythicLib.plugin.getSkills().loadSkillHandler(config.get("on_damage")));
        this.preDamage = config.contains("pre_damage") ? new SimpleSkill(MythicLib.plugin.getSkills().loadSkillHandler(config.get("pre_damage"))) : null;
    }

    @Nullable
    public NumericExpression getCooldown() {
        return cooldownFormula;
    }

    public boolean hasCooldown() {
        return cooldownFormula != null;
    }

    public boolean skipsEvent() {
        return skipEvent;
    }

    @Nullable
    public NumericExpression getRoll() {
        return rollFormula;
    }

    @NotNull
    public Skill onDamage() {
        return onDamage;
    }

    @Nullable
    public Skill preDamage() {
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
