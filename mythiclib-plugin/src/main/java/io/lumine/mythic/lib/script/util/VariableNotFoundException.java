package io.lumine.mythic.lib.script.util;

public class VariableNotFoundException extends ScriptException {
    public VariableNotFoundException(String variableName) {
        super("Variable '" + variableName + "' not found");
    }

    public VariableNotFoundException(String variableName, Class<?> variableClass) {
        super("Variable '" + variableName + "' not found in variable of type " + variableClass.getSimpleName());
    }

    public VariableNotFoundException(String variableName, String[] subvariables, int index) {
        super("Variable '" + subvariables[index] + "' not found in variable '" + variableName + "'");
    }
}
