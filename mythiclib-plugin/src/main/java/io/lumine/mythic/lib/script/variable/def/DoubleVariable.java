package io.lumine.mythic.lib.script.variable.def;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.script.variable.SimpleVariableRegistry;
import io.lumine.mythic.lib.script.variable.Variable;
import io.lumine.mythic.lib.script.variable.VariableMetadata;
import io.lumine.mythic.lib.script.variable.VariableRegistry;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.jetbrains.annotations.NotNull;

@VariableMetadata(name = "double")
public class DoubleVariable extends Variable<Double> {
    public static final SimpleVariableRegistry<Double> VARIABLE_REGISTRY = new SimpleVariableRegistry<>();

    static {
        VARIABLE_REGISTRY.registerVariable("int", val -> new IntegerVariable("temp", (int) (double) val));
        VARIABLE_REGISTRY.registerVariable("round", val -> new Round("temp", val));
    }

    public DoubleVariable(String name, double value) {
        super(name, value);
    }

    @Override
    public VariableRegistry<Variable<Double>> getVariableRegistry() {
        return VARIABLE_REGISTRY;
    }

    @VariableMetadata(name = "double")
    public static class Round extends Variable<Double> {
        public static final VariableRegistry<Variable<Double>> VARIABLE_REGISTRY = new VariableRegistry<>() {
            @Override
            public @NotNull Variable<?> accessVariable(@NotNull Variable<Double> doubleVariable, @NotNull String name) {
                final double val = doubleVariable.getStored();
                final int places = Integer.parseInt(name);
                if (places == 0) return new IntegerVariable("temp", (int) val);
                Validate.isTrue(places > 0, "Decimal places must be non-negative");
                final var format = MythicLib.plugin.getMMOConfig().newDecimalFormat("0." + "0".repeat(places));
                return new StringVariable("temp", format.format(val));
            }
        };

        public Round(String name, double value) {
            super(name, value);
        }

        @Override
        public VariableRegistry<Variable<Double>> getVariableRegistry() {
            return VARIABLE_REGISTRY;
        }
    }
}
