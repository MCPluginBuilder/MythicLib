package io.lumine.mythic.lib.stat;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.stat.StatInstance;
import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

// TODO improve? change StatModifier to interface. this could be used
//   later for dynamic stat modifiers with arbitrary formulas
public class ProxyStatModifier extends StatModifier {
    private final EquipmentSlot actionHand;
    private final StatInstance parentInstance;
    private final double coefficient;

    private static final String MOD_KEY = "StatProxy";

    public ProxyStatModifier(@NotNull StatInstance parentInstance,
                             @NotNull String stat,
                             @NotNull EquipmentSlot actionHand,
                             @NotNull ModifierType type,
                             double coefficient) {
        super(generateUniqueId(parentInstance.getStat(), actionHand), MOD_KEY, stat, 0, type, actionHand, ModifierSource.MELEE_WEAPON);

        this.actionHand = actionHand;
        this.parentInstance = parentInstance;
        this.coefficient = coefficient;
    }

    @Override
    public double getValue() {
        return this.parentInstance.getTotal(this.actionHand) * coefficient;
    }

    @Override
    public @NotNull StatModifier add(double offset) {
        throw new RuntimeException("Not supported");
    }

    @Override
    public @NotNull StatModifier multiply(double coef) {
        throw new RuntimeException("Not supported");
    }

    private static UUID generateUniqueId(String sourceStat, EquipmentSlot actionHand) {
        var asString = sourceStat + actionHand.name();
        return UUID.nameUUIDFromBytes(asString.getBytes());
    }
}