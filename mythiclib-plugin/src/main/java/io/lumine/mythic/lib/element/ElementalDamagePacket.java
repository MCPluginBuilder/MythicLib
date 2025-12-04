package io.lumine.mythic.lib.element;

import io.lumine.mythic.lib.damage.DamagePacket;
import io.lumine.mythic.lib.damage.DamageType;

import java.util.Arrays;
import java.util.Objects;

@Deprecated
public class ElementalDamagePacket extends DamagePacket {

    @Deprecated
    public ElementalDamagePacket(double value, Element element, DamageType... types) {
        super(value, Arrays.asList(types));

        setElement(element);
    }

    @Deprecated
    public Element getElement() {
        return super.getElement();
    }

    @Deprecated
    public void setElement(Element element) {
        super.setElement(Objects.requireNonNull(element, "Element cannot be null"));
    }
}