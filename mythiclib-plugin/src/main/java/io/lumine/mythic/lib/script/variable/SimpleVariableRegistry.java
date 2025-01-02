package io.lumine.mythic.lib.script.variable;

import io.lumine.mythic.lib.util.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class SimpleVariableRegistry<D> implements VariableRegistry<Variable<D>> {

    /**
     * A subvariable type is defined by its name and a function
     * which takes as input the variable and returns the corresponding subvariable.
     */
    private final Map<String, Function<D, Variable<?>>> registered = new HashMap<>();

    /**
     * Called when retrieving a subvariable from a variable.
     * <p>
     * Throws a runtime exception if subvariable type cannot be found.
     *
     * @param d    Input variable
     * @param name Name of the subvariable
     * @return The corresponding subvariable
     */
    @NotNull
    public Variable<?> accessVariable(@NotNull Variable<D> d, @NotNull String name) {
        final Function<D, ? extends Variable<?>> supplier = registered.get(name);
        Validate.notNull(supplier, "Cannot find subvariable '" + name + "' in variable type '" + d.getClass().getAnnotation(VariableMetadata.class).name() + "'");
        return supplier.apply(d.getStored());
    }

    /**
     * @param name Subvariable name
     * @return If there exists a variable which such name
     */
    public boolean hasVariable(String name) {
        return registered.containsKey(name);
    }

    /**
     * Registers all variables from another variable registry.
     * For example, the player variable type inherits all the
     * variables from the entity variable type.
     *
     * @param registry Parent variable registry
     */
    public <E extends D> void transferTo(@NotNull SimpleVariableRegistry<E> registry) {
        this.registered.forEach((key, getter) -> registry.registered.put(key, var -> getter.apply((E) var)));
    }

    /**
     * Registers a subvariable for a specific variable
     *
     * @param name     Subvariable name
     * @param supplier Function that takes as input a variable with type given
     *                 as generic parameter which outputs the corresponding subvariable
     */
    public void registerVariable(@NotNull String name, @NotNull Function<D, Variable<?>> supplier, String... aliases) {
        Validate.isTrue(!registered.containsKey(name), "A subvariable with the name '" + name + "' already exists");
        Validate.notNull(supplier, "Supplier cannot be null");

        registered.put(name, supplier);

        for (String alias : aliases)
            registerVariable(alias, supplier);
    }
}
