package io.lumine.mythic.lib.script.mechanic.projectile;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.util.TemporaryListener;
import io.lumine.mythic.lib.script.Script;
import io.lumine.mythic.lib.script.mechanic.type.DirectionMechanic;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.DoubleFormula;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class ShulkerBulletMechanic extends DirectionMechanic {
    private final DoubleFormula lifeSpan;
    private final Script onHitEntity;

    private static final double DEFAULT_LIFE_SPAN = 60;

    public ShulkerBulletMechanic(ConfigObject config) {
        super(config);

        onHitEntity = config.contains("hit_entity") ? MythicLib.plugin.getSkills().getScriptOrThrow(config.getString("hit_entity")) : null;
        lifeSpan = config.getDoubleFormula("life_span", DoubleFormula.constant(DEFAULT_LIFE_SPAN));
    }

    @Override
    public void cast(SkillMetadata meta, Location source, Vector dir) {
        final long lifespan = (long) this.lifeSpan.evaluate(meta);
        Validate.isTrue(lifespan > 0, "Life spawn must be strictly positive");
        final ShulkerBullet shulkerBullet = (ShulkerBullet) source.getWorld().spawnEntity(source, EntityType.SHULKER_BULLET);
        shulkerBullet.setShooter(meta.getCaster().getPlayer());
        new ShulkerBulletHandler(shulkerBullet, meta, dir).close(lifespan);
    }

    public class ShulkerBulletHandler extends TemporaryListener {
        private final ShulkerBullet bullet;
        //private final Vector direction;
        private final SkillMetadata skillMetadata;

        public ShulkerBulletHandler(ShulkerBullet bullet, SkillMetadata skillMetadata, Vector direction) {
            //this.direction = direction;
            this.bullet = bullet;
            this.skillMetadata = skillMetadata;

            registerRunnable(new BukkitRunnable() {
                @Override
                public void run() {
                    if (bullet.isDead()) close();
                    else bullet.setVelocity(direction);
                }
            }, runnable -> runnable.runTaskTimer(MythicLib.plugin, 0, 1));
        }

        @Override
        public void whenClosed() {
            if (!bullet.isDead()) bullet.remove();
        }

        @EventHandler
        public void registerHit(EntityDamageByEntityEvent event) {
            if (event.getDamager().equals(bullet)) {
                event.setCancelled(true);
                close();
                onHitEntity.cast(skillMetadata.clone(skillMetadata.getSourceLocation(), skillMetadata.getTargetLocationOrNull(), event.getEntity(), skillMetadata.getOrientationOrNull()));
            }
        }
    }
}
