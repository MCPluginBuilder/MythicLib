package io.lumine.mythic.lib.script.variable.def;

import io.lumine.mythic.lib.script.variable.SimpleVariableRegistry;
import io.lumine.mythic.lib.script.variable.Variable;
import io.lumine.mythic.lib.script.variable.VariableMetadata;
import io.lumine.mythic.lib.script.variable.VariableRegistry;
import org.bukkit.attribute.Attributable;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

@VariableMetadata(name = "entity")
public class EntityVariable extends Variable<Entity> {
    public static final SimpleVariableRegistry<Entity> VARIABLE_REGISTRY = new SimpleVariableRegistry<>();

    static {
        VARIABLE_REGISTRY.registerVariable("id", var -> new IntegerVariable("temp", var.getEntityId()));
        VARIABLE_REGISTRY.registerVariable("uuid", var -> new StringVariable("temp", var.getUniqueId().toString()));
        VARIABLE_REGISTRY.registerVariable("type", var -> new StringVariable("temp", var.getType().name()));
        VARIABLE_REGISTRY.registerVariable("location", var -> new PositionVariable("temp", var.getLocation()));
        VARIABLE_REGISTRY.registerVariable("bb_center", var -> new PositionVariable("temp", var.getBoundingBox().getCenter().toLocation(var.getWorld())));
        VARIABLE_REGISTRY.registerVariable("eye_location", var -> new PositionVariable("temp", ((LivingEntity) var).getEyeLocation()));
        VARIABLE_REGISTRY.registerVariable("health", var -> new DoubleVariable("temp", ((Damageable) var).getHealth()));
        VARIABLE_REGISTRY.registerVariable("looking", var -> new PositionVariable("temp", var.getWorld(), ((LivingEntity) var).getEyeLocation().getDirection()));
        VARIABLE_REGISTRY.registerVariable("velocity", var -> new PositionVariable("temp", var.getWorld(), var.getVelocity()));
        VARIABLE_REGISTRY.registerVariable("height", var -> new DoubleVariable("temp", var.getHeight()));
        VARIABLE_REGISTRY.registerVariable("attribute", var -> new AttributesVariable("temp", (Attributable) var));
        VARIABLE_REGISTRY.registerVariable("fire_ticks", var -> new IntegerVariable("temp", var.getFireTicks()));
    }

    public EntityVariable(String name, Entity entity) {
        super(name, entity);
    }

    @Override
    public VariableRegistry<Variable<Entity>> getVariableRegistry() {
        return VARIABLE_REGISTRY;
    }

    @Override
    public String toString() {
        return getStored() == null ? "None" : String.valueOf(getStored().getEntityId());
    }
}