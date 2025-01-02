package io.lumine.mythic.lib.skill.handler.def.location;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.LocationSkillResult;
import io.lumine.mythic.lib.version.VParticle;
import io.lumine.mythic.lib.version.VPotionEffectType;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Freeze extends SkillHandler<LocationSkillResult> {
    public Freeze() {
        super();

        registerModifiers("duration", "amplifier", "radius");
    }

    @NotNull
    @Override
    public LocationSkillResult getResult(SkillMetadata meta) {
        return new LocationSkillResult(meta);
    }

    @Override
    public void whenCast(LocationSkillResult result, SkillMetadata skillMeta) {
        Location loc = result.getTarget();
        Player caster = skillMeta.getCaster().getPlayer();

        double duration = skillMeta.getParameter("duration");
        int amplifier = (int) (skillMeta.getParameter("amplifier") - 1);
        double radiusSquared = Math.pow(skillMeta.getParameter("radius"), 2);

        loc.getWorld().spawnParticle(VParticle.LARGE_EXPLOSION.get(), loc.add(0, .1, 0), 0);
        loc.getWorld().spawnParticle(VParticle.ITEM_SNOWBALL.get(), loc, 48, 0, 0, 0, .2);
        loc.getWorld().spawnParticle(VParticle.FIREWORK.get(), loc, 24, 0, 0, 0, .2);
        loc.getWorld().playSound(loc, Sounds.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 2, 1);

        for (Entity entity : UtilityMethods.getNearbyChunkEntities(loc))
            if (entity.getLocation().distanceSquared(loc) < radiusSquared && UtilityMethods.canTarget(caster, entity))
                UtilityMethods.forcePotionEffect((LivingEntity) entity, VPotionEffectType.SLOWNESS.get(), duration, amplifier);
    }
}
