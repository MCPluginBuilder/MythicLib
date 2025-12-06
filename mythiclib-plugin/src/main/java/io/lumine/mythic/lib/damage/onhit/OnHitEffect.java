package io.lumine.mythic.lib.damage.onhit;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.player.cooldown.CooldownObject;
import io.lumine.mythic.lib.script.Script;
import io.lumine.mythic.lib.script.util.expression.numeric.NumericExpression;
import io.lumine.mythic.lib.util.PostLoadAction;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OnHitEffect implements CooldownObject {
    private final String id, cooldownPath;
    private final boolean skipEvent;

    private final @NotNull Script onAttack;
    private final @Nullable Script preAttack;
    private final @Nullable NumericExpression cooldownFormula, rollFormula;

    private final PostLoadAction postLoadAction = new PostLoadAction(config -> {
    });

    public OnHitEffect(@NotNull ConfigurationSection config) {
        this.id = config.getName();
        this.cooldownPath = "mitigation:" + id;
        this.skipEvent = config.getBoolean("skip_event", false);

        this.cooldownFormula = config.contains("cooldown") ? NumericExpression.compile(config.getString("cooldown")) : null;
        this.rollFormula = config.contains("roll") ? NumericExpression.compile(config.getString("roll")) : null;

        this.onAttack = MythicLib.plugin.getSkills().loadScript(config.get("on_attack"));
        this.preAttack = config.contains("pre_attack") ? MythicLib.plugin.getSkills().loadScript(config.get("pre_attack")) : null;
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

    @Nullable
    public NumericExpression getRoll() {
        return rollFormula;
    }

    @NotNull
    public Script onAttack() {
        return onAttack;
    }

    public boolean skipsEvent() {
        return skipEvent;
    }

    @Nullable
    public Script preAttack() {
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
