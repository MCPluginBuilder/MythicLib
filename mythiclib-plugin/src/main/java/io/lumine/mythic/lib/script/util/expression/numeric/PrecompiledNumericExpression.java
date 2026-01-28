package io.lumine.mythic.lib.script.util.expression.numeric;

import io.lumine.mythic.lib.script.util.expression.EvaluationException;
import io.lumine.mythic.lib.script.util.expression.placeholder.ExpressionPlaceholder;
import io.lumine.mythic.lib.script.util.expression.placeholder.MythicLibVariablePlaceholder;
import io.lumine.mythic.lib.script.util.expression.placeholder.PAPIPlaceholder;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import redempt.crunch.CompiledExpression;
import redempt.crunch.Crunch;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrecompiledNumericExpression extends NumericExpression {
    private final CompiledExpression precompiled;

    private final String originalExpression;

    // Crunch uses no variable names to avoid hashmap usage, it
    // uses numbered variables which allows to use arrays instead
    private final List<ExpressionPlaceholder> placeholders = new ArrayList<>();

    private static final Pattern PAPI_PLACEHOLDER_PATTERN = Pattern.compile("%[^!&|<>=%]+%");
    private static final Pattern CUSTOM_PLACEHOLDER_PATTERN = Pattern.compile("\\{([^{}]*)}");

    public PrecompiledNumericExpression(@NotNull String expression,
                                        @Nullable Function<String, ExpressionPlaceholder> customPlaceholders) {
        this.originalExpression = expression;

        // Internal (skill) placeholders
        expression = resolvePlaceholders(expression, SkillMetadata.INTERNAL_PLACEHOLDER_PATTERN, MythicLibVariablePlaceholder::new);

        // PAPI placeholders
        expression = resolvePlaceholders(expression, PAPI_PLACEHOLDER_PATTERN, PAPIPlaceholder::new);

        // Custom placeholders
        if (customPlaceholders != null)
            expression = resolvePlaceholders(expression, CUSTOM_PLACEHOLDER_PATTERN, customPlaceholders);

        // TODO further precompile <stat.xxxx> placeholders into StatInstance#getFinal
        // TODO recognize recursive (PAPI?) placeholders? need custom parser for that...

        // Finally, try to precompile
        // Might fail if unparsed placeholders remain
        this.precompiled = Crunch.compileExpression(expression, ENV);
    }

    private String resolvePlaceholders(@NotNull String input,
                                       @NotNull Pattern placeholderPattern,
                                       @NotNull Function<String, ExpressionPlaceholder> placeholderParser) {
        final var matcher = placeholderPattern.matcher(input);
        final var stringBuilder = new StringBuilder(input.length());
        while (matcher.find()) {
            final var variableName = matcher.group(1);
            final var replacementVariable = addNewVariable(placeholderParser.apply(variableName));
            matcher.appendReplacement(stringBuilder, Matcher.quoteReplacement(replacementVariable));
        }
        matcher.appendTail(stringBuilder);
        return stringBuilder.toString();
    }

    /**
     * @return The new variable name associated to this placeholder
     */
    @NotNull
    private String addNewVariable(@NotNull ExpressionPlaceholder placeholder) {
        this.placeholders.add(placeholder);
        // Crunch variables names start at $1
        // so return size() after adding to list
        return "$" + this.placeholders.size();
    }

    public synchronized double evaluate(@NotNull SkillMetadata skillMetadata) {
        return evaluate(Lazy.of(skillMetadata));
    }

    /**
     * Crunch expressions are not thread safe, so use mutex.
     * <p>
     * This should not cause problems as formulas are usually only
     * referenced by one object at a time in the JVM.
     */
    @Override
    public synchronized double evaluate(@NotNull Lazy<SkillMetadata> meta) {
        try {

            // Evaluate Crunch-external variables
            var varArray = new double[placeholders.size()];
            var varCount = 0;
            for (var placeholder : this.placeholders) varArray[varCount++] = placeholder.parse(meta.get());

            // Evaluate expression
            return precompiled.evaluate(varArray);
        } catch (EvaluationException exception) {
            throw new EvaluationException("Error evaluating expression '" + originalExpression + "'", exception);
        }
    }
}
