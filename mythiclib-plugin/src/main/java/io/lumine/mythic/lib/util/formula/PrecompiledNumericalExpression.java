package io.lumine.mythic.lib.util.formula;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.Lazy;
import org.jetbrains.annotations.NotNull;

public class PrecompiledNumericalExpression extends NumericalExpression {
    //private final Expression precompiled;

    public PrecompiledNumericalExpression(@NotNull String expression) {

        // TODO recognize internal <...> placeholders
        // TODO recognize PAPI placeholders
        // TODO recognize recursive placeholders????

        throw new RuntimeException("TODO");
    }

    @Override
    public double evaluate(@NotNull Lazy<SkillMetadata> meta) {
        throw new RuntimeException("TODO");
    }
}
