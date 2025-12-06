package io.lumine.mythic.lib.util;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.script.util.expression.numeric.NumericExpression;

import java.util.logging.Level;

public class DefenseFormula {
    // TODO precompile

    @Deprecated
    private final boolean elemental;

    @Deprecated
    public DefenseFormula() {
        this(false);
    }

    @Deprecated
    public DefenseFormula(boolean elemental) {
        this.elemental = elemental;
    }

    @Deprecated
    public double getAppliedDamage(double defense, double damage) {
        return calculateDamage(elemental, defense, damage);
    }

    public static double calculateDamage(boolean elemental, double defense, double damage) {
        final String formula = elemental ? MythicLib.plugin.getMMOConfig().elementalDefenseFormula : MythicLib.plugin.getMMOConfig().naturalDefenseFormula;
        final String expression = formula.replace("#defense#", String.valueOf(defense)).replace("#damage#", String.valueOf(damage));

        try {
            return Math.max(0, NumericExpression.eval(expression));
        } catch (Exception exception) {

            /**
             * Formula won't evaluate if hanging #'s or unparsed placeholders. Send a
             * friendly warning to console and just return the default damage.
             */
            MythicLib.inst().getLogger().log(Level.WARNING, "Could not evaluate defense formula (please check config): " + exception.getMessage());
            return damage;
        }
    }
}