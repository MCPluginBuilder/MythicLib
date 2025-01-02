package io.lumine.mythic.lib.skill.handler.def.location;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.LocationSkillResult;
import io.lumine.mythic.lib.util.ParabolicProjectile;
import io.lumine.mythic.lib.version.VParticle;
import io.lumine.mythic.lib.version.VPotionEffectType;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Power_Mark extends SkillHandler<LocationSkillResult> {
    public Power_Mark() {
        super();

        registerModifiers("duration", "stun", "ratio");
    }

    @Override
    public LocationSkillResult getResult(SkillMetadata meta) {
        return new LocationSkillResult(meta);
    }

    @Override
    public void whenCast(LocationSkillResult result, SkillMetadata skillMeta) {
        new PowerMark(skillMeta.getCaster(), skillMeta, result.getTarget());
    }

    public class PowerMark extends BukkitRunnable implements Listener {
        private final PlayerMetadata caster;
        private final Location loc;

        private final double duration;
        private final double ratio;
        private double stun;

        private double accumulate;
        private int j;

        public PowerMark(PlayerMetadata caster, SkillMetadata skillMeta, Location loc) {
            this.caster = caster;
            this.loc = loc;

            loc.getWorld().playSound(loc, Sounds.BLOCK_END_PORTAL_FRAME_FILL, 2, 1);

            duration = skillMeta.getParameter("duration");
            ratio = skillMeta.getParameter("ratio") / 100;
            stun = skillMeta.getParameter("stun");

            runTaskTimer(MythicLib.plugin, 0, 1);
            Bukkit.getPluginManager().registerEvents(this, MythicLib.plugin);
        }

        private void unregister() {
            PlayerAttackEvent.getHandlerList().unregister(this);
            cancel();
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void stackDamage(PlayerAttackEvent event) {
            if (!event.isCancelled() && j < 20 * (duration - 2) && radiusCheck(event.getEntity().getLocation()) && event.getAttacker().getPlayer().equals(caster.getPlayer())) {
                accumulate += event.getAttack().getDamage().getDamage() * ratio;
                new ParabolicProjectile(event.getEntity().getLocation().add(0, event.getEntity().getHeight() / 2, 0), loc, () -> loc.getWorld().playSound(loc, Sounds.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1), Color.PURPLE);
            }
        }

        private boolean radiusCheck(Location loc) {
            return loc.getWorld().equals(this.loc.getWorld()) && loc.distanceSquared(this.loc) < 16;
        }

        @Override
        public void run() {
            if (j++ > duration * 20) {
                unregister();

                for (double a = 0; a < Math.PI * 2; a += Math.PI * 2 / 17)
                    new ParabolicProjectile(loc, loc.clone().add(6 * Math.cos(a), 0, 6 * Math.sin(a)), VParticle.WITCH.get());

                loc.getWorld().playSound(loc, Sounds.ENTITY_GENERIC_EXPLODE, 2, 0);
                loc.getWorld().spawnParticle(VParticle.LARGE_EXPLOSION.get(), loc.clone().add(0, 1, 0), 16, 2, 2, 2, 0);
                loc.getWorld().spawnParticle(VParticle.EXPLOSION.get(), loc.clone().add(0, 1, 0), 24, 0, 0, 0, .3f);

                stun += Math.log(Math.max(1, accumulate - 10)) / 8;

                for (Entity entity : UtilityMethods.getNearbyChunkEntities(loc))
                    if (entity.getLocation().distanceSquared(loc) < 25 && UtilityMethods.canTarget(caster.getPlayer(), entity)) {
                        ((LivingEntity) entity).addPotionEffect(new PotionEffect(VPotionEffectType.SLOWNESS.get(), (int) (stun * 20), 10, false, false));
                        caster.attack((LivingEntity) entity, accumulate, DamageType.SKILL, DamageType.MAGIC);
                        entity.setVelocity(format(entity.getLocation().subtract(loc).toVector().setY(0)).setY(.3));
                    }
                return;
            }

            if (j % 2 == 0 && j > 20 * (duration - 2))
                loc.getWorld().playSound(loc, Sounds.BLOCK_NOTE_BLOCK_PLING, 1, (float) (1 + (j - 20 * (duration - 2)) / 40));

            double a = (double) j / 16;
            double r = Math.sqrt(Math.min(duration * 2 - (double) j / 10, 4)) * 2;
            for (double k = 0; k < Math.PI * 2; k += Math.PI * 2 / 5)
                loc.getWorld().spawnParticle(VParticle.WITCH.get(), loc.clone().add(r * Math.cos(k + a), 0, r * Math.sin(k + a)), 0);
        }
    }

    private Vector format(Vector vec) {
        return vec.length() < .01 ? new Vector(0, 0, 0) : vec.normalize();
    }
}
