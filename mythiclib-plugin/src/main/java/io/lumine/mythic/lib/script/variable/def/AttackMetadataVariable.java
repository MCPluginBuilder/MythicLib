package io.lumine.mythic.lib.script.variable.def;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.script.util.VariableNotFoundException;
import io.lumine.mythic.lib.script.variable.Variable;
import io.lumine.mythic.lib.script.variable.VariableMetadata;
import io.lumine.mythic.lib.script.variable.VariableRegistry;
import org.jetbrains.annotations.NotNull;

@VariableMetadata(name = "attackMeta")
public class AttackMetadataVariable extends Variable<AttackMetadata> {
    public static final VariableRegistry<Variable<AttackMetadata>> VARIABLE_REGISTRY = new VariableRegistry<>() {

        @Override
        public @NotNull Variable<?> accessVariable(@NotNull Variable<AttackMetadata> var, @NotNull String name) {

            if (name.equals("damage")) {
                return new DoubleVariable("temp", var.getStored().getDamage().getDamage());
            }

            if (name.startsWith("damage_")) {
                final var damageType = UtilityMethods.prettyValueOf(DamageType::valueOf, name.substring(7), "No damage type with ID %s");
                return new DoubleVariable("temp", var.getStored().getDamage().getDamage(damageType));
            }

            throw new VariableNotFoundException(name, AttackMetadata.class);
        }
    };

    public AttackMetadataVariable(String name, AttackMetadata attackMetadata) {
        super(name, attackMetadata);
    }

    @Override
    public VariableRegistry<Variable<AttackMetadata>> getVariableRegistry() {
        return VARIABLE_REGISTRY;
    }

    @Override
    public String toString() {
        return getStored() == null ? "None" : "AttackMeta";
    }
}