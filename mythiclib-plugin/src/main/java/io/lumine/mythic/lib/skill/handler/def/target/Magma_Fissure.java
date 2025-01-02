package io.lumine.mythic.lib.skill.handler.def.target;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import io.lumine.mythic.lib.version.VParticle;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Magma_Fissure extends SkillHandler<TargetSkillResult> {
    public Magma_Fissure() {
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
            final Location loc = caster.getLocation().add(0, .2, 0);
            int j = 0;

            public void run() {
                j++;
                if (target.isDead() || !target.getWorld().equals(loc.getWorld()) || j > 200) {
                    cancel();
                    return;
                }

                Vector vec = target.getLocation().add(0, .2, 0).subtract(loc).toVector().normalize().multiply(.6);
                loc.add(vec);

                loc.getWorld().spawnParticle(Particle.LAVA, loc, 2, .2, 0, .2, 0);
                loc.getWorld().spawnParticle(Particle.FLAME, loc, 2, .2, 0, .2, 0);
                loc.getWorld().spawnParticle(VParticle.SMOKE.get(), loc, 2, .2, 0, .2, 0);
                loc.getWorld().playSound(loc, Sounds.BLOCK_NOTE_BLOCK_HAT, 1, 1);

                if (target.getLocation().distanceSquared(loc) < 1) {
                    loc.getWorld().playSound(loc, Sounds.ENTITY_BLAZE_HURT, 2, 1);
                    target.setFireTicks((int) (target.getFireTicks() + skillMeta.getParameter("ignite") * 20));
                    skillMeta.getCaster().attack(target, skillMeta.getParameter("damage"), DamageType.SKILL, DamageType.MAGIC);
                    cancel();
                }
            }
        }.runTaskTimer(MythicLib.plugin, 0, 1);
    }
}