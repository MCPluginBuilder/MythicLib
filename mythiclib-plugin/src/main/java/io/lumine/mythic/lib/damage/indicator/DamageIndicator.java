package io.lumine.mythic.lib.damage.indicator;

import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.element.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

public class DamageIndicator {
    private final Collection<DamageType> tooltips;
    @Nullable
    private final Element extraTooltip;
    private final double value;

    public DamageIndicator(double value) {
        this(value, new ArrayList<>());
    }

    public DamageIndicator(double value, Collection<DamageType> tooltips) {
        this(value, tooltips, null);
    }

    public DamageIndicator(double value, Collection<DamageType> tooltips, @Nullable Element element) {
        this.value = value;
        this.tooltips = tooltips;
        this.extraTooltip = element;
    }

    @NotNull
    public Collection<DamageType> getTooltips() {
        return tooltips;
    }

    public double getValue() {
        return value;
    }

    public Element getElement() {
        return extraTooltip;
    }
}
