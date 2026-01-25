package io.lumine.mythic.lib.script.variable.def;

import io.lumine.mythic.lib.script.variable.SimpleVariableRegistry;
import io.lumine.mythic.lib.script.variable.Variable;
import io.lumine.mythic.lib.script.variable.VariableMetadata;
import io.lumine.mythic.lib.script.variable.VariableRegistry;

@VariableMetadata(name = "boolean")
public class BooleanVariable extends Variable<Boolean> {
    public static final SimpleVariableRegistry<Boolean> VARIABLE_REGISTRY = new SimpleVariableRegistry<>();

    static {
        VARIABLE_REGISTRY.registerVariable("negate", bool -> new BooleanVariable("temp", !bool), "not");
        VARIABLE_REGISTRY.registerVariable("int", bool -> new IntegerVariable("temp", bool ? 1 : 0), "integer");
    }

    public BooleanVariable(String name, boolean value) {
        super(name, value);
    }

    @Override
    public VariableRegistry<Variable<Boolean>> getVariableRegistry() {
        return VARIABLE_REGISTRY;
    }
}
