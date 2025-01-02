package io.lumine.mythic.lib.skill.handler.def.location;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.LocationSkillResult;
import io.lumine.mythic.lib.util.Line3D;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import io.lumine.mythic.lib.version.VPotionEffectType;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class Ice_Spikes extends SkillHandler<LocationSkillResult> {
    private static final double radius = 3;

    public Ice_Spikes() {
        super();

        registerModifiers("damage", "slow");
    }

    @NotNull
    @Override
    public LocationSkillResult getResult(SkillMetadata meta) {
        return new LocationSkillResult(meta, 20);
    }

    @Override
    public void whenCast(LocationSkillResult result, SkillMetadata skillMeta) {
        Location loc = result.getTarget();
        Player caster = skillMeta.getCaster().getPlayer();

        double damage = skillMeta.getParameter("damage");
        int slow = (int) (20 * skillMeta.getParameter("slow"));

        new BukkitRunnable() {
            int j = 0;

            @Override
            public void run() {

                if (j++ > 8) {
                    cancel();
                    return;
                }

                Location loc1 = loc.clone().add(offset() * radius, 0, offset() * radius).add(0, 2, 0);
                loc.getWorld().spawnParticle(VParticle.FIREWORK.get(), loc1, 32, 0, 2, 0, 0);
                loc.getWorld().spawnParticle(VParticle.SNOWFLAKE.get(), loc1, 32, 0, 2, 0, 0);
                loc.getWorld().playSound(loc1, Sounds.BLOCK_GLASS_BREAK, 2, 0);

                Line3D line = new Line3D(loc, new Vector(0, 1, 0));
                for (Entity entity : UtilityMethods.getNearbyChunkEntities(loc1))
                    if (line.distanceSquared(entity.getLocation().toVector()) < radius && Math.abs(entity.getLocation().getY() - loc1.getY()) < 10 && UtilityMethods.canTarget(caster, entity)) {
                        skillMeta.getCaster().attack((LivingEntity) entity, damage, DamageType.SKILL, DamageType.MAGIC);
                        ((LivingEntity) entity).addPotionEffect(new PotionEffect(VPotionEffectType.SLOWNESS.get(), slow, 0));
                    }
            }
        }.runTaskTimer(MythicLib.plugin, 0, 5);
    }

    private double offset() {
        return RANDOM.nextDouble() * (RANDOM.nextBoolean() ? 1 : -1);
    }
}
