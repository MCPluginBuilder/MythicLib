package io.lumine.mythic.lib.script.condition.location;

import io.lumine.mythic.lib.script.condition.type.LocationCondition;
import io.lumine.mythic.lib.script.variable.Variable;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.script.variable.def.PositionVariable;
import io.lumine.mythic.lib.util.Position;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

/**
 * Checks if the skill target location is within the two boundaries
 */
public class CuboidCondition extends LocationCondition {
    private final String varName1, varName2;

    public CuboidCondition(ConfigObject config) {
        super(config, false);

        config.validateKeys("first", "second", "third");

        varName1 = config.getString("loc1");
        varName2 = config.getString("loc2");
    }

    @Override
    public boolean isMet(SkillMetadata meta, Location loc) {
        Variable var1 = meta.getVariable(varName1);
        Validate.isTrue(var1 instanceof PositionVariable, "Variable '" + varName1 + "' is not a vector");
        Vector vec1 = ((Position) var1.getStored()).toVector();

        Variable var2 = meta.getVariable(varName1);
        Validate.isTrue(var2 instanceof PositionVariable, "Variable '" + varName2 + "' is not a vector");
        Vector vec2 = ((Position) var2.getStored()).toVector();

        return BoundingBox.of(vec1, vec2).contains(loc.toVector());
    }
}
