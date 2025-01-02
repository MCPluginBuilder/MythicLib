package io.lumine.mythic.lib.skill.handler.def.target;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import io.lumine.mythic.lib.version.VParticle;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Starfall extends SkillHandler<TargetSkillResult> {
    public Starfall() {
        super();

        registerModifiers("damage");
    }

    @Override
    public TargetSkillResult getResult(SkillMetadata meta) {
        return new TargetSkillResult(meta);
    }

    @Override
    public void whenCast(TargetSkillResult result, SkillMetadata skillMeta) {
        final LivingEntity target = result.getTarget();

        new BukkitRunnable() {
            final double ran = RANDOM.nextDouble() * Math.PI * 2;
            final Location loc = target.getLocation().add(Math.cos(ran) * 3, 6, Math.sin(ran) * 3);
            final Vector vec = target.getLocation().add(0, .65, 0).toVector().subtract(loc.toVector()).multiply(.05);
            double ti = 0;

            public void run() {
                loc.getWorld().playSound(loc, Sounds.BLOCK_NOTE_BLOCK_HAT, 2, 2);
                for (int j = 0; j < 2; j++) {
                    ti += .05;

                    loc.add(vec);
                    loc.getWorld().spawnParticle(VParticle.FIREWORK.get(), loc, 1, .04, 0, .04, 0);
                    if (ti >= 1) {
                        loc.getWorld().spawnParticle(VParticle.FIREWORK.get(), loc, 24, 0, 0, 0, .12);
                        loc.getWorld().playSound(loc, Sounds.ENTITY_FIREWORK_ROCKET_BLAST, 1, 2);
                        cancel();
                    }
                }
            }
        }.runTaskTimer(MythicLib.plugin, 0, 1);
        target.getWorld().playSound(target.getLocation(), Sounds.ENTITY_WITHER_SHOOT, 2, 2);

        skillMeta.getCaster().attack(target, skillMeta.getParameter("damage"), DamageType.SKILL, DamageType.MAGIC);
    }
}
