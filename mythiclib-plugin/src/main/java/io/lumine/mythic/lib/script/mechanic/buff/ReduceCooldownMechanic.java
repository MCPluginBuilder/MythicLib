package io.lumine.mythic.lib.script.mechanic.buff;

import io.lumine.mythic.lib.player.cooldown.CooldownInfo;
import io.lumine.mythic.lib.script.mechanic.type.TargetMechanic;
import io.lumine.mythic.lib.script.util.Parsers;
import io.lumine.mythic.lib.script.util.expression.numeric.NumericExpression;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import org.bukkit.entity.Entity;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class ReduceCooldownMechanic extends TargetMechanic {
    private final NumericExpression value;
    private final ReductionType type;
    private final String cooldownPath;

    public static final Function<String, ReductionType> PARSER_REDUCTION_TYPE = Parsers.ofEnum(ReductionType.class, ReductionType::valueOf);

    public ReduceCooldownMechanic(ConfigObject config) {
        super(config);

        this.cooldownPath = config.string("path");
        this.type = config.parse(ReductionType.FLAT, PARSER_REDUCTION_TYPE, "reduction");
        this.value = config.numericExpr("value", "val", "v");
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
