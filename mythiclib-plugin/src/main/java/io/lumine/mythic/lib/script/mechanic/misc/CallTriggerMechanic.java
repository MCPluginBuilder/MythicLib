package io.lumine.mythic.lib.script.mechanic.misc;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.script.mechanic.Mechanic;
import io.lumine.mythic.lib.script.targeter.EntityTargeter;
import io.lumine.mythic.lib.script.targeter.LocationTargeter;
import io.lumine.mythic.lib.script.util.Parsers;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CallTriggerMechanic extends Mechanic {
    private final EntityTargeter caster, targetEntity;
    private final LocationTargeter origin, targetLocation;
    private final TriggerType trigger;

    public CallTriggerMechanic(@NotNull ConfigObject config) {
        this.trigger = config.parse(Parsers.SKILL_TRIGGER, "trigger", "called");
        this.caster = config.contains("caster") ? config.getEntityTargeter("caster") : null;
        this.targetEntity = config.contains("target_entity") ? config.getEntityTargeter("target_entity") : null;
        this.origin = config.contains("origin") ? config.getLocationTargeter("origin") : null;
        this.targetLocation = config.contains("target_location") ? config.getLocationTargeter("target_location") : null;
    }

    private PlayerMetadata findCaster(SkillMetadata previousMeta) {
        var candidates = this.caster.findTargets(previousMeta);

        for (var candidate : candidates) {
            MMOPlayerData playerData;
            if (candidate instanceof Player && (playerData = MMOPlayerData.getOrNull(candidate.getUniqueId())) != null) {
                return playerData.getStatMap().cache(EquipmentSlot.MAIN_HAND);
            }
        }

        throw new RuntimeException("Caster not found (got " + candidates.size() + " candidates)");
    }

    @Override
    public void cast(@NotNull SkillMetadata previousMeta) {

        // Compute new skill meta. Defaults to current
        var newSkillMeta = previousMeta;
        if (this.caster != null) newSkillMeta = newSkillMeta.withCaster(this.findCaster(previousMeta));
        if (this.origin != null) newSkillMeta = newSkillMeta.withOrigin(this.origin.findTargets(previousMeta).getFirst());
        if (this.targetEntity != null) newSkillMeta = newSkillMeta.withTargetEntity(this.targetEntity.findTargets(previousMeta).getFirst());
        if (this.targetLocation != null) newSkillMeta = newSkillMeta.withTargetLocation(this.targetLocation.findTargets(previousMeta).getFirst());

        // Call trigger
        newSkillMeta.getCaster().getData().triggerSkills(this.trigger, newSkillMeta);
    }
}
