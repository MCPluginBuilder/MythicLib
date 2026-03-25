package io.lumine.mythic.lib.skill.handler.def.simple;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.event.PlayerClickEvent;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.BuiltinSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.util.TemporaryHandler;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VPotionEffectType;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@BuiltinSkillHandler(mods = {"duration", "count", "damage", "ignite"})
public class Fire_Rage extends SkillHandler<SimpleSkillResult> {
    private final List<DamageType> damageTypes;

    public Fire_Rage(ConfigurationSection config) {
        super(config);

        damageTypes = DamageType.listFromConfig(List.of(DamageType.PROJECTILE, DamageType.SKILL, DamageType.MAGIC), config.get("damage_types"));
    }

    @Override
    public @NotNull SimpleSkillResult getResult(SkillMetadata meta) {
        return new SimpleSkillResult();
    }

    @Override
    public void whenCast(SimpleSkillResult result, SkillMetadata skillMeta) {
        new Handler(skillMeta);
    }

    class Handler extends TemporaryHandler {
        private final PlayerMetadata caster;
        private final int maxCount, ignite;
        private final double damage;

        private int counter;
        private double b;
        private long last = System.currentTimeMillis();

        /**
         * Time the player needs to wait before firing two consecutive fireballs.
         */
        private static final long FIREBALL_COOLDOWN = 700;

        public Handler(SkillMetadata skillMeta) {
            super(skillMeta.getCaster().getData());

            this.caster = skillMeta.getCaster();
            this.ignite = (int) (20 * skillMeta.getParameter("ignite"));
            this.damage = skillMeta.getParameter("damage");
            counter = maxCount = (int) skillMeta.getParameter("count");

            runTask(runnable -> runnable.runTaskTimer(MythicLib.plugin, 0, 1));

            // Time out for skill
            closeAfter(20 * (long) skillMeta.getParameter("duration"));
        }

        @Override
        protected @Nullable BukkitRunnable newTask() {
            return new BukkitRunnable() {

                @Override
                public void run() {
                    b += Math.PI / 30;
                    for (int j = 0; j < counter; j++) {
                        double a = Math.PI * 2 * j / maxCount + b;
                        caster.getPlayer().spawnParticle(Particle.FLAME, caster.getPlayer().getLocation().add(Math.cos(a) * 1.5, 1 + Math.sin(a * 1.5) * .7, Math.sin(a) * 1.5), 0);
                    }
                }
            };
        }

        @EventHandler
        public void a(PlayerClickEvent event) {
            if (event.isLeftClick() && event.getPlayer().equals(caster.getPlayer()) && (System.currentTimeMillis() - last) > FIREBALL_COOLDOWN) {
                last = System.currentTimeMillis();
                castEffect();

                counter--;
                var isLastFireball = counter == 0;

                throwFireball(isLastFireball);
                if (isLastFireball) {
                    caster.getPlayer().removePotionEffect(VPotionEffectType.SLOWNESS.get());
                    caster.getPlayer().removePotionEffect(VPotionEffectType.SLOWNESS.get());
                    Handler.this.close();
                }
            }
        }

        void castEffect() {
            for (double a = 0; a < Math.PI * 2; a += Math.PI / 13) {
                Vector vec = UtilityMethods.rotate(new Vector(Math.cos(a), Math.sin(a), 0), caster.getPlayer().getEyeLocation().getDirection()).add(caster.getPlayer().getEyeLocation().getDirection().multiply(.5)).multiply(.3);
                caster.getPlayer().getWorld().spawnParticle(Particle.FLAME, caster.getPlayer().getLocation().add(0, 1.3, 0).add(caster.getPlayer().getEyeLocation().getDirection().multiply(.5)), 0, vec.getX(), vec.getY(), vec.getZ(), .3);
            }
        }

        void throwFireball(boolean last) {
            caster.getPlayer().getWorld().playSound(caster.getPlayer().getLocation(), Sounds.ENTITY_FIREWORK_ROCKET_BLAST, 1, last ? 0 : 1);

            TemporaryHandler.timerTask(caster.getData(), 1, handler -> new BukkitRunnable() {
                int j = 0;
                final Vector vec = caster.getPlayer().getEyeLocation().getDirection();
                final Location loc = caster.getPlayer().getLocation().add(0, 1.3, 0);

                public void run() {
                    if (++j > 40) {
                        handler.close();
                        return;
                    }

                    loc.add(vec);

                    if (j % 2 == 0)
                        loc.getWorld().playSound(loc, Sounds.BLOCK_FIRE_AMBIENT, 2, 1);
                    loc.getWorld().spawnParticle(Particle.FLAME, loc, 4, .1, .1, .1, 0);
                    loc.getWorld().spawnParticle(Particle.LAVA, loc, 0);

                    for (Entity target : UtilityMethods.getNearbyChunkEntities(loc))
                        if (UtilityMethods.canTarget(caster.getPlayer(), loc, target)) {
                            loc.getWorld().spawnParticle(Particle.LAVA, loc, 8);
                            loc.getWorld().spawnParticle(Particle.FLAME, loc, 32, 0, 0, 0, .1);
                            loc.getWorld().playSound(loc, Sounds.ENTITY_BLAZE_HURT, 2, 1);
                            target.setFireTicks(target.getFireTicks() + ignite);
                            caster.attack((LivingEntity) target, damage, damageTypes);
                            handler.close();
                        }
                }
            });
        }
    }
}
