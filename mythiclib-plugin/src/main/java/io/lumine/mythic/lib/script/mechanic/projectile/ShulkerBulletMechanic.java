package io.lumine.mythic.lib.script.mechanic.projectile;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.script.Script;
import io.lumine.mythic.lib.script.mechanic.type.DirectionMechanic;
import io.lumine.mythic.lib.script.util.expression.numeric.NumericExpression;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.TemporaryHandler;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

public class ShulkerBulletMechanic extends DirectionMechanic {
    private final NumericExpression lifeSpan;
    private final Script onHitEntity;

    public ShulkerBulletMechanic(ConfigObject config) {
        super(config);

        onHitEntity = config.contains("hit_entity") ? MythicLib.plugin.getSkills().getScriptOrThrow(config.getString("hit_entity")) : null;
        lifeSpan = config.numericExpr(NumericExpression.of(60), "life_span", "lifespan", "lifetime", "l", "ticks", "duration", "dur", "d");
    }

    @Override
    public void cast(SkillMetadata meta, Location source, Vector dir) {
        final long lifespan = (long) this.lifeSpan.evaluate(meta);
        Validate.isTrue(lifespan > 0, "Life spawn must be strictly positive");
        final ShulkerBullet shulkerBullet = (ShulkerBullet) source.getWorld().spawnEntity(source, EntityType.SHULKER_BULLET);
        shulkerBullet.setShooter(meta.getCaster().getPlayer());
        new Handler(shulkerBullet, meta, dir).closeAfter(lifespan);
    }

    class Handler extends TemporaryHandler {
        private final ShulkerBullet bullet;
        private final Vector direction;
        private final SkillMetadata skillMetadata;

        public Handler(ShulkerBullet bullet, SkillMetadata skillMetadata, Vector direction) {
            super(skillMetadata.getCaster().getData());

            this.direction = direction;
            this.bullet = bullet;
            this.skillMetadata = skillMetadata;

            runTask(runnable -> runnable.runTaskTimer(MythicLib.plugin, 0, 1));
        }

        @Override
        protected @Nullable BukkitRunnable newTask() {
            return new BukkitRunnable() {
                @Override
                public void run() {
                    if (bullet.isDead()) Handler.this.close();
                    else bullet.setVelocity(direction);
                }
            };
        }

        @Override
        protected void onClose() {
            if (!bullet.isDead()) bullet.remove();
        }

        @EventHandler
        public void registerHit(EntityDamageByEntityEvent event) {
            if (event.getDamager().equals(bullet)) {
                event.setCancelled(true);
                close();
                onHitEntity.cast(skillMetadata.clone(skillMetadata.getSourceLocation(), skillMetadata.getTargetLocationOrNull(), event.getEntity()));
            }
        }
    }
}
