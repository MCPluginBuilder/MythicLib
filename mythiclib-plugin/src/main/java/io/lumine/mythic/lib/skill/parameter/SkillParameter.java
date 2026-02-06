package io.lumine.mythic.lib.skill.parameter;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.skill.parameter.value.NonScalingFormula;
import io.lumine.mythic.lib.skill.parameter.value.ScalingFormula;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.Objects;

public class SkillParameter {
    private final String translate;
    private final double itemDefaultValue;
    private final DecimalFormat decimalFormat;
    private final ScalingFormula scalingFormula;

    public static final DecimalFormat DEFAULT_DECIMAL_FORMAT = new DecimalFormat("0.####");

    private SkillParameter(String modifier) {
        this(modifier, 0);
    }

    private SkillParameter(String modifier, double itemDefaultValue) {
        this.translate = modifier;
        this.itemDefaultValue = itemDefaultValue;
        this.decimalFormat = null;
        this.scalingFormula = new NonScalingFormula(itemDefaultValue);
    }

    private SkillParameter(ConfigurationSection config) {
        this.translate = Objects.requireNonNullElseGet(config.getString("name"), () -> inferModifierName(config.getName()));
        this.itemDefaultValue = config.getDouble("item");
        this.scalingFormula = ScalingFormula.fromConfig(config.get("player"));
        this.decimalFormat = config.contains("format") ? new DecimalFormat(config.getString("format")) : null;
    }

    @NotNull
    private String inferModifierName(String modifierId) {
        return UtilityMethods.caseOnWords(modifierId.replace("-", " ").replace("_", " ").toLowerCase());
    }

    @NotNull
    public String getTranslate() {
        return translate;
    }

    public double getItemDefaultValue() {
        return itemDefaultValue;
    }

    @NotNull
    public DecimalFormat getDecimalFormat() {
        return Objects.requireNonNullElse(decimalFormat, DEFAULT_DECIMAL_FORMAT);
    }

    @NotNull
    public ScalingFormula getScalingFormula() {
        return scalingFormula;
    }

    @NotNull
    public String format(double modifierValue) {
        if (decimalFormat != null) return decimalFormat.format(modifierValue);
        return String.valueOf(modifierValue);
    }

    //region Static Methods

    @NotNull
    public static SkillParameter empty(String modifier) {
        return new SkillParameter(modifier);
    }

    @NotNull
    public static SkillParameter fromConfig(@Nullable Object object, @NotNull String modifier) {

        // Fallback
        if (object == null) {
            return new SkillParameter(modifier);
        }

        // Number -> item default value, that's it
        if (object instanceof Number) {
            return new SkillParameter(modifier, ((Number) object).doubleValue());
        }

        // Config, load everything babyyyy
        if (object instanceof ConfigurationSection) {
            return new SkillParameter((ConfigurationSection) object);
        }

        throw new IllegalArgumentException("Skill parameter must be a number or config section");
    }

    //endregion
}
