package io.lumine.mythic.lib.script.targeter.location;

import io.lumine.mythic.lib.script.variable.Variable;
import io.lumine.mythic.lib.script.targeter.LocationTargeter;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.script.variable.def.PositionVariable;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Location;

import java.util.Arrays;
import java.util.List;

@Orientable
public class VariableLocationTargeter extends LocationTargeter {
    private final String[] args;

    public VariableLocationTargeter(ConfigObject config) {
        super(config);

        config.validateKeys("name");

        args = config.getString("name").split("\\.");
    }

    @Override
    public List<Location> findTargets(SkillMetadata meta) {

        Variable var = meta.getVariable(args[0]);
        for (int i = 1; i < args.length; i++)
            var = var.getVariable(args[i]);

        Validate.isTrue(var instanceof PositionVariable, "Variable '" + var.getName() + "' is not a vector");
        return Arrays.asList(((PositionVariable) var).getStored().toLocation());
    }
}
