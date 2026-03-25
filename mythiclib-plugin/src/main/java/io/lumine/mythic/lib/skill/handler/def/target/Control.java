package io.lumine.mythic.lib.skill.handler.def.target;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.event.PlayerClickEvent;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.BuiltinSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import io.lumine.mythic.lib.util.TemporaryHandler;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import io.lumine.mythic.lib.version.VPotionEffectType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@BuiltinSkillHandler(mods = {"knockback", "duration"})
public class Control extends SkillHandler<TargetSkillResult> {
    public Control(ConfigurationSection config) {
        super(config);
    }

    @Override
    public @NotNull TargetSkillResult getResult(SkillMetadata meta) {
        return new TargetSkillResult(meta);
    }

    @Override
    public void whenCast(TargetSkillResult result, SkillMetadata skillMeta) {
        Player caster = skillMeta.getCaster().getPlayer();
        caster.getWorld().playSound(caster.getLocation(), Sounds.BLOCK_END_PORTAL_FRAME_FILL, 1, 1);
        result.getTarget().addPotionEffect(new PotionEffect(VPotionEffectType.SLOWNESS.get(), 20 * 2, 0));
        new Handler(skillMeta.getCaster(), result.getTarget(), skillMeta.getParameter("knockback") / 100, skillMeta.getParameter("duration"));
    }

    static class Handler extends TemporaryHandler {
        private final LivingEntity entity;
        private final PlayerMetadata caster;

        private final double f, d;

        private int j;

        public Handler(PlayerMetadata caster, LivingEntity entity, double force, double duration) {
            super(caster.getData());

            this.entity = entity;
            this.caster = caster;

            d = duration * 20;
            f = force;

            runTask(r -> r.runTaskTimer(MythicLib.plugin, 0, 1));
        }

        @Override
        protected @Nullable BukkitRunnable newTask() {
            return new BukkitRunnable() {
                @Override
                public void run() {
                    if (entity.isDead() || j++ >= d) {
                        Handler.this.close();
                        return;
                    }

                    double a = (double) j / 3;
                    entity.getWorld().spawnParticle(VParticle.WITCH.get(), entity.getLocation().add(Math.cos(a), entity.getHeight() / 2, Math.sin(a)), 0);
                }
            };
        }

        @EventHandler
        public void a(PlayerClickEvent event) {
            if (event.isLeftClick() && event.getPlayer().equals(caster.getPlayer())) {
                Vector vec = caster.getPlayer().getEyeLocation().getDirection().multiply(3 * f);
                vec.setY(Math.max(.5, vec.getY() / 2));
                entity.setVelocity(vec);

                // Try not to interfere with other potion effects
                PotionEffect effect = entity.getPotionEffect(VPotionEffectType.SLOWNESS.get());
                if (effect.getDuration() < d && effect.getAmplifier() == 0)
                    entity.removePotionEffect(VPotionEffectType.SLOWNESS.get());

                entity.getWorld().spawnParticle(VParticle.WITCH.get(), entity.getLocation().add(0, entity.getHeight() / 2, 0), 16);
                entity.getWorld().playSound(entity.getLocation(), Sounds.ENTITY_FIREWORK_ROCKET_BLAST, 2, 1);
                close();
            }
        }
    }
}
