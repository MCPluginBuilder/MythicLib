package io.lumine.mythic.lib.skill.handler.def.location;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.LocationSkillResult;
import io.lumine.mythic.lib.version.VParticle;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Arcane_Hail extends SkillHandler<LocationSkillResult> {
    public Arcane_Hail() {
        super();

        registerModifiers("damage", "duration", "radius");
    }

    @Override
    public LocationSkillResult getResult(SkillMetadata meta) {
        return new LocationSkillResult(meta);
    }

    @Override
    public void whenCast(LocationSkillResult result, SkillMetadata skillMeta) {
        Location loc = result.getTarget();
        Player caster = skillMeta.getCaster().getPlayer();

        double damage = skillMeta.getParameter("damage");
        double duration = skillMeta.getParameter("duration") * 10;
        double radius = skillMeta.getParameter("radius");

        new BukkitRunnable() {
            int j = 0;

            public void run() {
                if (j++ > duration) {
                    cancel();
                    return;
                }

                Location loc1 = loc.clone().add(randomCoordMultiplier() * radius, 0, randomCoordMultiplier() * radius);
                loc1.getWorld().playSound(loc1, Sounds.ENTITY_ENDERMAN_HURT, 1, 0);
                for (Entity entity : UtilityMethods.getNearbyChunkEntities(loc1))
                    if (UtilityMethods.canTarget(caster, entity) && entity.getLocation().distanceSquared(loc1) <= 4)
                        skillMeta.getCaster().attack((LivingEntity) entity, damage, DamageType.SKILL, DamageType.MAGIC);
                loc1.getWorld().spawnParticle(VParticle.WITCH.get(), loc1, 12, 0, 0, 0, .1);
                loc1.getWorld().spawnParticle(VParticle.SMOKE.get(), loc1, 6, 0, 0, 0, .1);

                Vector vector = new Vector(randomCoordMultiplier() * .03, .3, randomCoordMultiplier() * .03);
                for (double k = 0; k < 60; k++)
                    loc1.getWorld().spawnParticle(VParticle.WITCH.get(), loc1.add(vector), 0);
            }
        }.runTaskTimer(MythicLib.plugin, 0, 2);
    }

    // random double between -1 and 1
    private double randomCoordMultiplier() {
        return (RANDOM.nextDouble() - .5) * 2;
    }
}
