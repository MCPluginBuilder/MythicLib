package io.lumine.mythic.lib.skill.handler.def.target;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.event.PlayerClickEvent;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.BuiltinSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import io.lumine.mythic.lib.util.ParabolicProjectile;
import io.lumine.mythic.lib.util.TemporaryHandler;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@BuiltinSkillHandler(mods = {"knockback", "duration"})
public class Telekinesy extends SkillHandler<TargetSkillResult> {
    public Telekinesy(ConfigurationSection config) {
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
        new Handler(skillMeta.getCaster().getData(), result.getTarget(), skillMeta.getParameter("duration"), skillMeta.getParameter("knockback") / 100);
    }

    static class Handler extends TemporaryHandler {
        private final Entity entity;
        private final MMOPlayerData caster;

        private final long duration;
        private final double d, f;

        private int j;

        public Handler(MMOPlayerData caster, Entity entity, double duration, double force) {
            super(caster);

            this.entity = entity;
            this.caster = caster;

            d = caster.getPlayer().getLocation().distance(entity.getLocation());
            f = force;
            this.duration = (long) (20 * duration);

            runTask(r -> r.runTaskTimer(MythicLib.plugin, 0, 1));
        }

        @Override
        protected @Nullable BukkitRunnable newTask() {
            return new BukkitRunnable() {
                @Override
                public void run() {
                    if (entity.isDead() || j++ >= duration) {
                        Handler.this.close();
                        return;
                    }

                    if (j % 8 == 0)
                        new ParabolicProjectile(caster.getPlayer().getEyeLocation(), entity.getLocation().add(0, entity.getHeight() / 2, 0), VParticle.WITCH.get());

                    Location loc = caster.getPlayer().getEyeLocation().add(caster.getPlayer().getEyeLocation().getDirection().multiply(d));
                    entity.setVelocity(loc.subtract(entity.getLocation().add(0, entity.getHeight() / 2, 0)).toVector().multiply(2));
                    entity.setFallDistance(0);
                }
            };
        }

        @EventHandler
        public void a(PlayerClickEvent event) {
            if (event.isLeftClick() && event.getPlayer().equals(caster.getPlayer())) {
                entity.setVelocity(caster.getPlayer().getEyeLocation().getDirection().multiply(1.5 * f));
                entity.getWorld().playSound(entity.getLocation(), Sounds.ENTITY_FIREWORK_ROCKET_BLAST, 2, 1);
                entity.getWorld().spawnParticle(VParticle.WITCH.get(), entity.getLocation().add(0, entity.getHeight() / 2, 0), 16);
                close();
            }
        }
    }
}
