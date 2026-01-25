package io.lumine.mythic.lib.script.variable;

import io.lumine.mythic.lib.script.variable.def.PositionVariable;
import org.jetbrains.annotations.NotNull;

/**
 * A huge part of the ML scripting language: being able to
 * have variables with methods which return other variables. This
 * allows the user to manipulate variables and give the skill
 * mechanics more complex parameters.
 * <p>
 * Any variable registry can be accessed just like a bukkit event
 * handler list.
 *
 * @param <T> One variable class like {@link PositionVariable}
 */
public interface VariableRegistry<T> {

    /**
     * Called when retrieving a subvariable from a variable.
     * <p>
     * Throws a runtime exception if subvariable type cannot be found.
     *
     * @param t    Input variable
     * @param name Name of the subvariable
     * @return The corresponding subvariable
     */
    @NotNull
    public Variable<?> accessVariable(@NotNull T t, @NotNull String name);
}
