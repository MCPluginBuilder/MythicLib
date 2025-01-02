package io.lumine.mythic.lib.skill.handler.def.target;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import io.lumine.mythic.lib.util.ParabolicProjectile;
import io.lumine.mythic.lib.version.VParticle;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Fire_Storm extends SkillHandler<TargetSkillResult> {
    public Fire_Storm() {
        super();

        registerModifiers("damage", "ignite");
    }

    @Override
    public TargetSkillResult getResult(SkillMetadata meta) {
        return new TargetSkillResult(meta);
    }

    @Override
    public void whenCast(TargetSkillResult result, SkillMetadata skillMeta) {
        Player caster = skillMeta.getCaster().getPlayer();
        LivingEntity target = result.getTarget();

        final double damage = skillMeta.getParameter("damage");
        final int ignite = (int) (20 * skillMeta.getParameter("ignite"));

        caster.getPlayer().getWorld().playSound(caster.getPlayer().getLocation(), Sounds.ENTITY_FIREWORK_ROCKET_BLAST, 1, 1);
        new BukkitRunnable() {
            int j = 0;

            @Override
            public void run() {
                if (j++ > 5 || UtilityMethods.isInvalidated(caster) || target.isDead() || !caster.getPlayer().getWorld().equals(target.getWorld())) {
                    cancel();
                    return;
                }

                // TODO dynamic target location

                caster.getPlayer().getWorld().playSound(caster.getPlayer().getLocation(), Sounds.BLOCK_FIRE_AMBIENT, 1, 1);
                new ParabolicProjectile(caster.getPlayer().getLocation().add(0, 1, 0), target.getLocation().add(0, target.getHeight() / 2, 0),
                        randomVector(caster.getPlayer()), () -> {
                    target.getWorld().playSound(target.getLocation(), Sounds.ENTITY_FIREWORK_ROCKET_TWINKLE, 1, 2);
                    target.getWorld().spawnParticle(VParticle.SMOKE.get(), target.getLocation().add(0, target.getHeight() / 2, 0), 8, 0, 0, 0, .15);
                    skillMeta.getCaster().attack(target, damage, DamageType.SKILL, DamageType.MAGIC, DamageType.PROJECTILE);
                    target.setFireTicks(ignite);

                }, 2, Particle.FLAME);
            }
        }.runTaskTimer(MythicLib.plugin, 0, 4);
    }

    private Vector randomVector(Player player) {
        double a = Math.toRadians(player.getEyeLocation().getYaw() + 90);
        a += (RANDOM.nextBoolean() ? 1 : -1) * (RANDOM.nextDouble() * 2 + 1) * Math.PI / 6;
        return new Vector(Math.cos(a), .8, Math.sin(a)).normalize().multiply(.4);
    }
}
