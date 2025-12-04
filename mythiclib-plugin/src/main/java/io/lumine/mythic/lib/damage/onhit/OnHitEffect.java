package io.lumine.mythic.lib.damage.onhit;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.player.cooldown.CooldownObject;
import io.lumine.mythic.lib.script.Script;
import io.lumine.mythic.lib.util.PostLoadAction;
import io.lumine.mythic.lib.util.formula.NumericalExpression;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OnHitEffect implements CooldownObject {
    private final String id, cooldownPath;
    private final boolean skipEvent;

    private @NotNull Script onAttack;
    private @Nullable Script preAttack;
    private final @Nullable NumericalExpression cooldownFormula, rollFormula;

    private final PostLoadAction postLoadAction = new PostLoadAction(config -> {
        this.onAttack = MythicLib.plugin.getSkills().loadScript(config.get("on_attack"));
        if (config.contains("pre_damage"))
            this.preAttack = MythicLib.plugin.getSkills().loadScript(config.get("pre_attack"));
    });

    public OnHitEffect(@NotNull ConfigurationSection config) {
        this.id = config.getName();
        this.cooldownPath = "mitigation:" + id;
        this.skipEvent = config.getBoolean("skip_event", false);

        this.postLoadAction.cacheConfig(config);

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
