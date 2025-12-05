package io.lumine.mythic.lib.skill.parameter.value;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

// TODO merge with NumericalExpression
public class NonScalingFormula implements ScalingFormula {
    private final double constant;

    @Override
    public boolean isInteger() {
        return false;
    }

    public NonScalingFormula(double constant) {
        this.constant = constant;
    }

    @Override
    public double evaluate(int skillLevel, @Nullable Player player) {
        return constant;
    }
}
