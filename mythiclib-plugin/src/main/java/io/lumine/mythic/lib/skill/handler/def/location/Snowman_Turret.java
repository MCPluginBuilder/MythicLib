package io.lumine.mythic.lib.skill.handler.def.location;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.BuiltinSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.LocationSkillResult;
import io.lumine.mythic.lib.util.TemporaryHandler;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import io.lumine.mythic.lib.version.VPotionEffectType;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Snowman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@BuiltinSkillHandler(mods = {"duration", "damage", "radius"})
public class Snowman_Turret extends SkillHandler<LocationSkillResult> {
    public Snowman_Turret(ConfigurationSection config) {
        super(config);
    }

    @Override
    public @NotNull LocationSkillResult getResult(SkillMetadata meta) {
        return new LocationSkillResult(meta);
    }

    @Override
    public void whenCast(LocationSkillResult result, SkillMetadata skillMeta) {
        Location loc = result.getTarget();

        new Handler(skillMeta, loc);
        loc.getWorld().playSound(loc, Sounds.ENTITY_ENDERMAN_TELEPORT, 2, 1);
    }

    static class Handler extends TemporaryHandler {
        private final List<UUID> entities = new ArrayList<>();
        private final Snowman snowman;
        private final PlayerMetadata caster;

        private final double damage, duration, radiusSquared;

        public Handler(SkillMetadata skillMeta, Location loc) {
            super(skillMeta.getCaster().getData());

            this.snowman = loc.getWorld().spawn(loc.add(0, 1, 0), Snowman.class);
            snowman.setInvulnerable(true);
            snowman.addPotionEffect(new PotionEffect(VPotionEffectType.SLOWNESS.get(), 100000, 254, true));

            this.caster = skillMeta.getCaster();

            this.damage = skillMeta.getParameter("damage");
            this.duration = Math.min(skillMeta.getParameter("duration") * 20, 300);
            this.radiusSquared = Math.pow(skillMeta.getParameter("radius"), 2);

            runTask(runnable -> runnable.runTaskTimer(MythicLib.plugin, 0, 1));
        }

        @Override
        protected BukkitRunnable newTask() {
            return new BukkitRunnable() {
                int ti = 0;
                double j = 0;

                public void run() {
                    if (ti++ > duration || UtilityMethods.isInvalidated(caster) || snowman.isDead()) {
                        Handler.this.closeAfter(3 * 20);
                        snowman.remove();
                    }

                    j += Math.PI / 24 % (2 * Math.PI);
                    for (double k = 0; k < 3; k++)
                        VParticle.INSTANT_EFFECT.spawnSafeSpell(snowman.getLocation().add(
                                Math.cos(j + k / 3 * 2 * Math.PI) * 1.3,
                                1,
                                Math.sin(j + k / 3 * 2 * Math.PI) * 1.3));
                    VParticle.INSTANT_EFFECT.spawnSafeSpell(snowman.getLocation().add(0, 1, 0), 1, 0, 0, 0, .2);

                    if (ti % 2 == 0)
                        for (Entity entity : UtilityMethods.getNearbyChunkEntities(snowman.getLocation()))
                            if (!entity.equals(snowman) && UtilityMethods.canTarget(caster.getPlayer(), entity)
                                    && entity.getLocation().distanceSquared(snowman.getLocation()) < radiusSquared) {
                                snowman.getWorld().playSound(snowman.getLocation(), Sounds.ENTITY_SNOWBALL_THROW, 1, 1.3f);

                                // Throw snowball
                                final var snowball = snowman.launchProjectile(Snowball.class);
                                snowball.setVelocity(entity.getLocation().add(0, entity.getHeight() / 2, 0).toVector()
                                        .subtract(snowman.getLocation().add(0, 1, 0).toVector()).normalize().multiply(1.3));
                                Handler.this.entities.add(snowball.getUniqueId());

                                break;
                            }
                }
            };
        }

        @Override
        public void onClose() {
            // TODO remove snowballs?
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void registerDamage(EntityDamageByEntityEvent event) {
            if (entities.contains(event.getDamager().getUniqueId()))
                event.setDamage(damage);
        }
    }
}
