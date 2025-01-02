package io.lumine.mythic.lib.skill.handler.def.location;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.util.TemporaryListener;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.LocationSkillResult;
import io.lumine.mythic.lib.version.VParticle;
import io.lumine.mythic.lib.version.VPotionEffectType;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Snowman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Snowman_Turret extends SkillHandler<LocationSkillResult> {
    public Snowman_Turret() {
        super();

        registerModifiers("duration", "damage", "radius");
    }

    @Override
    public LocationSkillResult getResult(SkillMetadata meta) {
        return new LocationSkillResult(meta);
    }

    @Override
    public void whenCast(LocationSkillResult result, SkillMetadata skillMeta) {
        Location loc = result.getTarget();
        Player caster = skillMeta.getCaster().getPlayer();

        double duration = Math.min(skillMeta.getParameter("duration") * 20, 300);
        double radiusSquared = Math.pow(skillMeta.getParameter("radius"), 2);

        loc.getWorld().playSound(loc, Sounds.ENTITY_ENDERMAN_TELEPORT, 2, 1);
        final Snowman snowman = loc.getWorld().spawn(loc.add(0, 1, 0), Snowman.class);
        snowman.setInvulnerable(true);
        snowman.addPotionEffect(new PotionEffect(VPotionEffectType.SLOWNESS.get(), 100000, 254, true));
        new BukkitRunnable() {
            int ti = 0;
            double j = 0;
            final TurretHandler turret = new TurretHandler(skillMeta.getParameter("damage"));

            public void run() {
                if (ti++ > duration || UtilityMethods.isInvalidated(caster) || snowman.isDead()) {
                    turret.close(3 * 20);
                    snowman.remove();
                    cancel();
                }

                j += Math.PI / 24 % (2 * Math.PI);
                for (double k = 0; k < 3; k++)
                    snowman.getWorld().spawnParticle(VParticle.INSTANT_EFFECT.get(),
                            snowman.getLocation().add(Math.cos(j + k / 3 * 2 * Math.PI) * 1.3, 1, Math.sin(j + k / 3 * 2 * Math.PI) * 1.3), 0);
                snowman.getWorld().spawnParticle(VParticle.INSTANT_EFFECT.get(), snowman.getLocation().add(0, 1, 0), 1, 0, 0, 0, .2);

                if (ti % 2 == 0)
                    for (Entity entity : UtilityMethods.getNearbyChunkEntities(snowman.getLocation()))
                        if (!entity.equals(snowman) && UtilityMethods.canTarget(caster, entity)
                                && entity.getLocation().distanceSquared(snowman.getLocation()) < radiusSquared) {
                            snowman.getWorld().playSound(snowman.getLocation(), Sounds.ENTITY_SNOWBALL_THROW, 1, 1.3f);
                            Snowball snowball = snowman.launchProjectile(Snowball.class);
                            snowball.setVelocity(entity.getLocation().add(0, entity.getHeight() / 2, 0).toVector()
                                    .subtract(snowman.getLocation().add(0, 1, 0).toVector()).normalize().multiply(1.3));
                            turret.entities.add(snowball.getUniqueId());
                            break;
                        }
            }
        }.runTaskTimer(MythicLib.plugin, 0, 1);
    }

    public static class TurretHandler extends TemporaryListener {
        private final List<UUID> entities = new ArrayList<>();
        private final double damage;

        public TurretHandler(double damage) {
            this.damage = damage;
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void a(EntityDamageByEntityEvent event) {
            if (entities.contains(event.getDamager().getUniqueId()))
                event.setDamage(damage);
        }
    }
}
