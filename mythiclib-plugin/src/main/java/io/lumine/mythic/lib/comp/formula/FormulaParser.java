package io.lumine.mythic.lib.comp.formula;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.script.util.expression.bool.BooleanExpression;
import io.lumine.mythic.lib.script.util.expression.numeric.NumericExpression;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Deprecated
public class FormulaParser {

    private static FormulaParser instance;

    private FormulaParser() {
        // Nothing
    }

    public static FormulaParser getInstance() {
        if (instance == null) instance = new FormulaParser();
        return instance;
    }

    @NotNull
    @Deprecated
    public Object eval(@NotNull String str) {

        try {
            return NumericExpression.eval(str);
        } catch (Exception ignored) {
            try {
                return BooleanExpression.eval(str);
            } catch (Exception exception) {
                throw new RuntimeException("could not evaluate string '" + exception.getMessage() + "'", exception);
            }
        }
    }

    @NotNull
    @Deprecated
    public Object evaluate(@NotNull String str) {
        return eval(str);
    }

    @NotNull
    @Deprecated
    public Object eval(@Nullable OfflinePlayer player, @NotNull String str) {
        return evaluate(MythicLib.plugin.getPlaceholderParser().parse(player, str));
    }

    @Deprecated
    public double evaluateAsDouble(@NotNull OfflinePlayer player, @NotNull String str) {
        return evaluateAsDouble(MythicLib.plugin.getPlaceholderParser().parse(player, str));
    }

    @Deprecated
    public double evaluateAsDouble(@NotNull String str) {
        return evaluateAs(str, Number.class).doubleValue();
    }

    @Deprecated
    public int evaluateAsInt(@NotNull String str) {
        return evaluateAs(str, Number.class).intValue();
    }

    @Deprecated
    public long evaluateAsLong(@NotNull String str) {
        return evaluateAs(str, Number.class).longValue();
    }

    @NotNull
    @Deprecated
    public <O> O evaluateAs(@NotNull String str, @NotNull Class<O> targetClass) {
        final Object result = evaluate(str);
        if (!targetClass.isInstance(result))
            throw new IllegalArgumentException("Expression did not return a " + targetClass.getSimpleName());
        return (O) result;
    }
}
