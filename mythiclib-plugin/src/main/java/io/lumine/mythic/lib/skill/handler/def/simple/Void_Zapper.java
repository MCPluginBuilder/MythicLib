package io.lumine.mythic.lib.skill.handler.def.simple;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.version.VParticle;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Color;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class Void_Zapper extends SkillHandler<SimpleSkillResult> {
    public Void_Zapper() {
        super();

        registerModifiers("damage", "knockback", "length", "max", "extra");
    }

    @NotNull
    @Override
    public SimpleSkillResult getResult(SkillMetadata meta) {
        return new SimpleSkillResult();
    }

    @Override
    public void whenCast(SimpleSkillResult result, SkillMetadata skillMeta) {
        Player caster = skillMeta.getCaster().getPlayer();
        caster.getWorld().playSound(caster.getLocation(), Sounds.ENTITY_ENDERMAN_HURT, 1, 1);
        new SkillHandler(skillMeta);
    }

    class SkillHandler {
        private final PlayerMetadata caster;
        private final SkillMetadata skillMeta;

        /**
         * Amount of bounces so far
         */
        private int bounces;

        private final int maxBounces;

        SkillHandler(SkillMetadata skillMeta) {
            this.caster = skillMeta.getCaster();
            this.skillMeta = skillMeta;
            this.maxBounces = (int) skillMeta.getParameter("max");

            castRay(caster.getPlayer().getEyeLocation(), caster.getPlayer().getEyeLocation().getDirection(), skillMeta.getParameter("length"));
        }

        void castRay(Location loc, Vector dir, double lengthRemaining) {
            RayTraceResult result = loc.getWorld().rayTrace(loc, dir, lengthRemaining, FluidCollisionMode.NEVER, true, .2, entity -> UtilityMethods.canTarget(caster.getPlayer(), entity));
            double traveled = result == null ? lengthRemaining : result.getHitPosition().distance(loc.toVector());
            draw(loc, dir, traveled);

            bounces++;

            // Nothing hit
            if (result == null)
                return;

            // Damage entity and apply knockback
            if (result.getHitEntity() != null)
                hit((LivingEntity) result.getHitEntity(), dir);

                // Draw next line after reflection recursively
            else if (bounces++ < maxBounces)
                castRay(result.getHitPosition().toLocation(loc.getWorld()), getReflection(dir, result.getHitBlockFace().getDirection()), lengthRemaining - traveled);
        }

        void hit(LivingEntity target, Vector dir) {
            double damage = skillMeta.getParameter("damage") * (1 + skillMeta.getParameter("extra") * bounces / 100d);
            skillMeta.getCaster().attack(target, damage, DamageType.SKILL, DamageType.MAGIC, DamageType.PROJECTILE);
            target.setVelocity(dir.multiply(skillMeta.getParameter("knockback")));
        }

        private static final double STEP = .2;

        void draw(Location loc, Vector dir, double length) {
            for (double dis = 0; dis < length; dis += STEP) {
                Location intermediate = loc.clone().add(dir.clone().multiply(dis));
                loc.getWorld().spawnParticle(VParticle.REDSTONE.get(), intermediate, 1, new Particle.DustOptions(Color.PURPLE, 1.2f));
            }
        }
    }

    /**
     * Reflects a vector onto a surface. The normal vector
     * must be normalized for this to work.
     * <p>
     * Basically project the vector onto the surface's normal vector
     * and add the inverse double of that projection to the initial vector.
     * <p>
     * The fact that we're taking a factor of 2 guarantees that if the
     * initial vector <code>vec</code> was normalized, then the
     * new reflected vector is normalized as well as long as <code>normal</code>
     * is normalized too.
     *
     * @param vec    Vector being reflected
     * @param normal Normal vector of surface
     * @return Reflected vector
     */
    private Vector getReflection(Vector vec, Vector normal) {
        double projection = vec.dot(normal);
        return vec.clone().add(normal.multiply(-2 * projection));
    }
}
