package io.lumine.mythic.lib.script.variable;

import io.lumine.mythic.lib.util.lang3.Validate;

import javax.annotation.Nullable;

/**
 * A variable which can be stored in a {@link VariableList}
 * and edited within the skills, or provided through placeholders.
 * <p>
 * Names are really important because it's what the user
 * interacts with.
 *
 * @param <D> The object stored in that variable. This can be a string,
 *            double, integer, string list, location, vector...
 */
public abstract class Variable<D> implements VariableContainer {
    private final String name;

    private D stored;

    public Variable(String name, D stored) {
        this.name = name;
        this.stored = stored;

        Validate.isTrue(getClass().isAnnotationPresent(VariableMetadata.class), "Variable type with no VariableMetadata annotation");
    }

    public String getName() {
        return name;
    }

    public D getStored() {
        return stored;
    }

    public void setStored(D d) {
        stored = d;
    }

    public abstract VariableRegistry<Variable<D>> getVariableRegistry();

    @Override
    @Nullable
    public Variable<?> getVariable(String name) {
        return getVariableRegistry().accessVariable(this, name);
    }

    /**
     * Used when calling a variable inside a skill mechanic string parameter.
     *
     * @return How that variable would display if called
     * inside a mechanic string parameter
     */
    @Override
    public String toString() {
        return stored == null ? "None" : stored.toString();
    }
}
