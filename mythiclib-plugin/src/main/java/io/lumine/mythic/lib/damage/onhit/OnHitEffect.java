package io.lumine.mythic.lib.damage.onhit;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.player.cooldown.CooldownObject;
import io.lumine.mythic.lib.script.util.expression.numeric.NumericExpression;
import io.lumine.mythic.lib.skill.SimpleSkill;
import io.lumine.mythic.lib.skill.Skill;
import io.lumine.mythic.lib.util.PostLoadAction;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OnHitEffect implements CooldownObject {
    private final String id, cooldownPath;
    private final boolean skipEvent;

    private final @NotNull Skill onAttack;
    private final @Nullable Skill preAttack;
    private final @Nullable NumericExpression cooldownFormula, rollFormula;

    private final PostLoadAction postLoadAction = new PostLoadAction(config -> {
    });

    public OnHitEffect(@NotNull ConfigurationSection config) {
        this.id = config.getName();
        this.cooldownPath = "mitigation:" + id;
        this.skipEvent = config.getBoolean("skip_event", false);

        this.cooldownFormula = config.contains("cooldown") ? NumericExpression.compile(config.getString("cooldown")) : null;
        this.rollFormula = config.contains("roll") ? NumericExpression.compile(config.getString("roll")) : null;

        this.onAttack = new SimpleSkill(MythicLib.plugin.getSkills().loadSkillHandler(config.get("on_attack")));
        this.preAttack = config.contains("pre_attack") ? new SimpleSkill(MythicLib.plugin.getSkills().loadSkillHandler(config.get("pre_attack"))) : null;
    }

    public PostLoadAction getPostLoadAction() {
        return postLoadAction;
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
    public Skill onAttack() {
        return onAttack;
    }

    @Nullable
    public Skill preAttack() {
        return preAttack;
    }

    @NotNull
    public String getId() {
        return id;
    }

    @Override
    public String getCooldownPath() {
        return cooldownPath;
    }
}
