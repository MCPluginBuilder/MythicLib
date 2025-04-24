package io.lumine.mythic.lib.skill.result;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.mobs.GenericCaster;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.players.PlayerData;
import io.lumine.mythic.core.skills.SkillMetadataImpl;
import io.lumine.mythic.core.skills.SkillTriggers;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.MythicMobsSkillHandler;
import io.lumine.mythic.lib.util.RayTrace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class MythicMobsSkillResult implements SkillResult {
    private final SkillMetadataImpl mmSkillMeta;
    private final boolean success;

    public MythicMobsSkillResult(@NotNull SkillMetadata skillMeta, @NotNull MythicMobsSkillHandler behaviour) {

        // TODO Support trigger/caster difference?
        Player player = skillMeta.getCaster().getPlayer();
        AbstractEntity trigger = BukkitAdapter.adapt(player);
        Optional<PlayerData> playerDataOpt = MythicBukkit.inst().getPlayerManager().getProfile(player.getUniqueId());
        SkillCaster caster = playerDataOpt.isPresent() ? playerDataOpt.get() : new GenericCaster(trigger);

        List<AbstractEntity> targetEntities;
        List<AbstractLocation> targetLocations;

        // Add target entity
        if (skillMeta.hasTargetEntity())
            targetEntities = List.of(BukkitAdapter.adapt(skillMeta.getTargetEntityOrNull()));

            /*
             * If none is found, provide a default entity target. This takes
             * the entity is the player is looking at if there is any. This
             * is purely for simplicity so that skills cast within MythicLib
             * match the /mm test cast command.
             */
        else {
            final RayTrace res = new RayTrace(player, 32, entity -> !entity.equals(player) && entity instanceof LivingEntity);
            targetEntities = res.hasHit() ? List.of(BukkitAdapter.adapt(res.getHit())) : List.of();
        }

        // Add target location
        if (skillMeta.hasTargetLocation())
            targetLocations = List.of(BukkitAdapter.adapt(skillMeta.getTargetLocationOrNull()));
        else targetLocations = List.of();

        mmSkillMeta = new SkillMetadataImpl(SkillTriggers.API, caster, trigger, BukkitAdapter.adapt(skillMeta.getCaster().getPlayer().getEyeLocation()), targetEntities, targetLocations, 1);

        // Stats & cast skill are cached inside a variable
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
