package io.lumine.mythic.lib.skill.result;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.mobs.GenericCaster;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.skills.SkillMetadataImpl;
import io.lumine.mythic.core.skills.SkillTriggers;
import io.lumine.mythic.core.utils.MythicUtil;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.MythicMobsSkillHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class MythicMobsSkillResult implements SkillResult {
    private final SkillMetadataImpl mmSkillMeta;
    private final boolean success;

    public MythicMobsSkillResult(@NotNull SkillMetadata skillMeta, @NotNull MythicMobsSkillHandler behaviour, boolean skipRayTrace) {

        ///////////////////////////////////////////////////
        // Interfacing ML-MM: Caster and trigger.
        ///////////////////////////////////////////////////

        // TODO Support trigger/caster difference?
        var player = skillMeta.getCaster().getPlayer();
        var trigger = BukkitAdapter.adapt(player);
        var playerDataOpt = MythicBukkit.inst().getPlayerManager().getProfile(player.getUniqueId());
        var caster = playerDataOpt.isPresent() ? playerDataOpt.get() : new GenericCaster(trigger);

        List<AbstractEntity> targetEntities;
        List<AbstractLocation> targetLocations;

        ///////////////////////////////////////////////////
        // Interfacing ML-MM: Add target entity
        ///////////////////////////////////////////////////
        var mlTargetEntity = skillMeta.getTargetEntityOrNull();
        if (mlTargetEntity != null) {
            targetEntities = List.of(BukkitAdapter.adapt(mlTargetEntity));
        }

        // [Optimization] Skip raytrace. See docs of boolean field for explanation.
        else if (skipRayTrace) {
            targetEntities = List.of();
        }

        // If none is found, provide a default entity target using MythicMobs
        // MythicMobs util function, to stay consistent with /mm cast command.
        else {
            // Let MythicMobs handle target entity
            var targetEntity = MythicUtil.getTargetedEntity(player);
            targetEntities = targetEntity == null ? Collections.emptyList() : Collections.singletonList(BukkitAdapter.adapt(targetEntity));
        }

        ///////////////////////////////////////////////////
        // Interfacing ML-MM: Add target location
        ///////////////////////////////////////////////////
        var mlTargetLocation = skillMeta.getTargetLocationOrNull();
        if (mlTargetLocation != null) {
            targetLocations = List.of(BukkitAdapter.adapt(mlTargetLocation));
        }

        // If none is found, expected behaviour is to provide none
        // (Unlike entity target, which needs to be raytraces)
        else targetLocations = List.of();

        ///////////////////////////////////////////////////
        // Interfacing ML-MM: Instantiate MM SkillMetadata
        ///////////////////////////////////////////////////

        // TODO adapt MythicLib trigger into MM triggers.
        mmSkillMeta = new SkillMetadataImpl(SkillTriggers.API, caster, trigger, BukkitAdapter.adapt(skillMeta.getCaster().getPlayer().getLocation()), targetEntities, targetLocations, 1);

        // All MMO skill metadata (stats, cast skill, modifiers.....) are
        // cached inside a MM variable. Small workaround. In theory, the user
        // could OVERRIDE that MM variable but it is unlikely.
        mmSkillMeta.getVariables().putObject(MMO_SKILLMETADATA_TAG, skillMeta);

        success = behaviour.getSkill().isUsable(mmSkillMeta);
    }

    public static final String MMO_SKILLMETADATA_TAG = "MMOSkillMetadata";

    @Override
    public boolean isSuccessful() {
        return success;
    }

    public SkillMetadataImpl getMythicMobsSkillMetadata() {
        return mmSkillMeta;
    }

    @Deprecated
    public SkillMetadataImpl getMythicMobskillMetadata() {
        return getMythicMobsSkillMetadata();
    }
}
