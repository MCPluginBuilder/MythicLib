package io.lumine.mythic.lib.script.mechanic.misc;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.script.Script;
import io.lumine.mythic.lib.script.mechanic.Mechanic;
import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.script.targeter.EntityTargeter;
import io.lumine.mythic.lib.script.targeter.LocationTargeter;
import io.lumine.mythic.lib.script.variable.def.IntegerVariable;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.DoubleFormula;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

@MechanicMetadata
public class ScriptMechanic extends Mechanic {
    private final Script skill;

    /**
     * Allows to cast the same script multiple times in a row but
     * everytime the counter is increased by 1 just like in a for loop
     */
    private final DoubleFormula iterations;

    /**
     * Variable name used to save the iteration counter
     */
    @Nullable
    private final String counterVarName;

    @Nullable
    private final LocationTargeter targetLocation, sourceLocation;
    @Nullable
    private final EntityTargeter targetEntity;

    public ScriptMechanic(ConfigObject config) {
        config.validateKeys("name");

        // Multiple skill casts
        counterVarName = config.getString("counter", "counter");
        iterations = config.getDoubleFormula("iterations", DoubleFormula.constant(1));

        // Targeters
        sourceLocation = config.contains("source") ? config.getLocationTargeter("source") : null;
        targetEntity = config.contains("target") ? config.getEntityTargeter("target") : null;
        targetLocation = config.contains("target_location") ? config.getLocationTargeter("target_location") : null;

        skill = MythicLib.plugin.getSkills().getScriptOrThrow(config.getString("name"));
    }

    @Override
    public void cast(SkillMetadata meta) {
        if (counterVarName == null)
            castWithNewMeta(meta);
        else {
            final int ni = (int) this.iterations.evaluate(meta);
            Validate.isTrue(ni >= 0, "Number of iterations must be positive");
            for (int i = 0; i < ni; i++) {
                meta.getVariableList().registerVariable(new IntegerVariable(counterVarName, i + 1));
                castWithNewMeta(meta);
            }
        }
    }

    /**
     * Takes into account all the targeters provided by the config and
     * generates new skillMetadatas to cast the skill with these instead.
     * <p>
     * If no targeter is provided, the metadata used to cast the skill
     * is the same as the one used in {@link #cast(SkillMetadata)}
     *
     * @param old Meta used to cast the 'skill' mechanic
     * @implNote Regarding the double for loop which allows to cast the skill
     * with every possible skill metadata, this will not be used very often.
     * Generally the skill is either used with a entity targeter OR a location targeter,
     * neither both at the same time.
     */
    private void castWithNewMeta(SkillMetadata old) {

        // Reduces calculations
        if (sourceLocation == null && targetEntity == null && targetLocation == null) {
            skill.cast(old);
            return;
        }

        // Find new source location
        final Location sourceLocation = this.sourceLocation == null ? old.getSourceLocation() : this.sourceLocation.findTargets(old).get(0);

        // Find new target entities & locations
        final List<Entity> newTargetEntities = targetEntity == null ? Arrays.asList(old.getTargetEntityOrNull()) : this.targetEntity.findTargets(old);
        final List<Location> newTargetLocations = targetLocation == null ? Arrays.asList(old.getTargetLocationOrNull()) : this.targetLocation.findTargets(old);

        // Cast with every mathematically possible skill metadata
        for (Location targetLocation : newTargetLocations)
            for (Entity targetEntity : newTargetEntities)
                skill.cast(old.clone(sourceLocation, targetLocation, targetEntity, old.getOrientationOrNull()));
    }
}
