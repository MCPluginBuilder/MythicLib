package io.lumine.mythic.lib.script.variable;

import io.lumine.mythic.lib.util.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class SimpleVariableRegistry<D> implements VariableRegistry<Variable<D>> {

    /**
     * A subvariable type is defined by its name and a function
     * which takes as input the variable and returns the corresponding subvariable.
     */
    private final Map<String, Function<D, Variable<?>>> registered = new HashMap<>();

    private final SimpleVariableRegistry<?> parent;

    public SimpleVariableRegistry() {
        this(null);
    }

    public SimpleVariableRegistry(@Nullable SimpleVariableRegistry<?> parent) {
        this.parent = parent;
    }

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
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Variable<?> accessVariable(@NotNull Variable<D> d, @NotNull String name) {

        // Fast code
        final var supplier = registered.get(name);
        if (supplier != null) return supplier.apply(d.getStored());

        // Parent checkup
        var current = this.parent;
        while (current != null) {
            Function parentSupplier = current.registered.get(name);
            if (parentSupplier != null) return (Variable<?>) parentSupplier.apply(d.getStored());
            current = current.parent;
        }
        throw new IllegalArgumentException("Cannot find subvariable '" + name + "' in variable type '" + d.getClass().getAnnotation(VariableMetadata.class).name() + "'");
    }

    /**
     * Registers all variables from another variable registry.
     * For example, the player variable type inherits all the
     * variables from the entity variable type.
     *
     * @param registry Parent variable registry
     * @deprecated No guarantee if parent map has already been statically initialized
     */
    @Deprecated
    public <E extends D> void transferTo(@NotNull SimpleVariableRegistry<E> registry) {
        /*
        MythicLib.plugin.getLogger().log(Level.INFO, "================");
        MythicLib.plugin.getLogger().log(Level.INFO, "Transferring " + this.registered.keySet().toString() + " to " + registry.registered.keySet().toString());
        MythicLib.plugin.getLogger().log(Level.INFO, "================");
         */
        this.registered.forEach((key, getter) -> registry.registered.put(key, getter::apply));
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

        for (var alias : aliases) registerVariable(alias, supplier);
    }
}
