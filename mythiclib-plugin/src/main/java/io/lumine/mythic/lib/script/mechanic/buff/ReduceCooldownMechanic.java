package io.lumine.mythic.lib.script.mechanic.buff;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.player.cooldown.CooldownInfo;
import io.lumine.mythic.lib.script.mechanic.type.TargetMechanic;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.DoubleFormula;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import org.bukkit.entity.Entity;

import java.util.function.BiConsumer;

public class ReduceCooldownMechanic extends TargetMechanic {
    private final DoubleFormula value;
    private final ReductionType type;
    private final String cooldownPath;

    public ReduceCooldownMechanic(ConfigObject config) {
        super(config);

        config.validateKeys("value", "path");

        this.cooldownPath = config.getString("path");
        this.type = config.contains("reduction") ? ReductionType.valueOf(UtilityMethods.enumName(config.getString("reduction"))) : ReductionType.FLAT;
        this.value = new DoubleFormula(config.getString("value"));
    }

    @Override
    public void cast(SkillMetadata meta, Entity target) {

        // Check if it's on cooldown first
        CooldownInfo info = meta.getCaster().getData().getCooldownMap().getInfo(cooldownPath);
        if (info == null || info.hasEnded())
            return;

        type.apply(info, value.evaluate(meta));
    }

    public static enum ReductionType {
        FLAT(CooldownInfo::reduceFlat),
        INITIAL(CooldownInfo::reduceInitialCooldown),
        REMAINING(CooldownInfo::reduceRemainingCooldown);

        private final BiConsumer<CooldownInfo, Double> effect;

        ReductionType(BiConsumer<CooldownInfo, Double> effect) {
            this.effect = effect;
        }

        public void apply(CooldownInfo info, double value) {
            effect.accept(info, value);
        }
    }
}
