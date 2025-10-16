package io.lumine.mythic.lib.script.variable.def;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.script.variable.SimpleVariableRegistry;
import io.lumine.mythic.lib.script.variable.Variable;
import io.lumine.mythic.lib.script.variable.VariableMetadata;
import io.lumine.mythic.lib.script.variable.VariableRegistry;
import org.bukkit.entity.Player;

@VariableMetadata(name = "player")
public class PlayerVariable extends Variable<Player> {
    public static final SimpleVariableRegistry<Player> VARIABLE_REGISTRY = new SimpleVariableRegistry<>(EntityVariable.VARIABLE_REGISTRY);

    static {
        VARIABLE_REGISTRY.registerVariable("stat", var -> new StatsVariable("temp", MMOPlayerData.get(var).getStatMap()));
        VARIABLE_REGISTRY.registerVariable("cooldown", var -> new CooldownsVariable("temp", MMOPlayerData.get(var).getCooldownMap()));
        VARIABLE_REGISTRY.registerVariable("name", var -> new StringVariable("temp", var.getName()));
        VARIABLE_REGISTRY.registerVariable("eye_direction", var -> new PositionVariable("temp", var.getEyeLocation().getWorld(), var.getEyeLocation().getDirection()));
    }

    public PlayerVariable(String name, Player player) {
        super(name, player);
    }

    @Override
    public VariableRegistry<Variable<Player>> getVariableRegistry() {
        return VARIABLE_REGISTRY;
    }

    @Override
    public String toString() {
        return getStored() == null ? "None" : getStored().getName();
    }
}