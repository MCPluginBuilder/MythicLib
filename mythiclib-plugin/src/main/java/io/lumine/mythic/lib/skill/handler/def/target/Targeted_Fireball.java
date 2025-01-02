package io.lumine.mythic.lib.skill.handler.def.target;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Targeted_Fireball extends SkillHandler<TargetSkillResult> {
    public Targeted_Fireball() {
        super();

        registerModifiers("ignite", "damage");
    }

    @Override
    public TargetSkillResult getResult(SkillMetadata meta) {
        return new TargetSkillResult(meta);
    }

    @Override
    public void whenCast(TargetSkillResult result, SkillMetadata skillMeta) {
        LivingEntity target = result.getTarget();
        Player caster = skillMeta.getCaster().getPlayer();

        new BukkitRunnable() {
            final Location loc = caster.getLocation().add(0, 1.3, 0);
            int j = 0;

            public void run() {
                j++;
                if (target.isDead() || !target.getWorld().equals(loc.getWorld()) || j > 200) {
                    cancel();
                    return;
                }

                Vector dir = target.getLocation().add(0, target.getHeight() / 2, 0).subtract(loc).toVector().normalize();
                loc.add(dir.multiply(.6));

                loc.setDirection(dir);
                for (double a = 0; a < Math.PI * 2; a += Math.PI / 6) {
                    Vector rotated = UtilityMethods.rotate(new Vector(Math.cos(a), Math.sin(a), 0), loc.getDirection());
                    loc.getWorld().spawnParticle(Particle.FLAME, loc, 0, rotated.getX(), rotated.getY(), rotated.getZ(), .06);
                }

                loc.getWorld().playSound(loc, Sounds.BLOCK_NOTE_BLOCK_HAT, 1, 1);
                if (target.getLocation().add(0, target.getHeight() / 2, 0).distanceSquared(loc) < 1.3) {
                    loc.getWorld().spawnParticle(Particle.LAVA, loc, 8);
                    loc.getWorld().spawnParticle(Particle.FLAME, loc, 32, 0, 0, 0, .1);
                    loc.getWorld().playSound(loc, Sounds.ENTITY_BLAZE_HURT, 2, 1);
                    target.setFireTicks((int) (target.getFireTicks() + skillMeta.getParameter("ignite") * 20));
                    skillMeta.getCaster().attack(target, skillMeta.getParameter("damage"), DamageType.SKILL, DamageType.MAGIC, DamageType.PROJECTILE);
                    cancel();
                }
            }
        }.runTaskTimer(MythicLib.plugin, 0, 1);
    }
}