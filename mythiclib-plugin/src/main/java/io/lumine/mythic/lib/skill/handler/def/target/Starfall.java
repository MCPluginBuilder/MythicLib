package io.lumine.mythic.lib.skill.handler.def.target;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.BuiltinSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@BuiltinSkillHandler(mods = {"damage"})
public class Starfall extends SkillHandler<TargetSkillResult> {
    private final List<DamageType> damageTypes;

    public Starfall(ConfigurationSection config) {
        super(config);

        damageTypes = DamageType.listFromConfig(List.of(DamageType.SKILL, DamageType.MAGIC), config.get("damage_types"));
    }

    @Override
    public @NotNull TargetSkillResult getResult(SkillMetadata meta) {
        return new TargetSkillResult(meta);
    }

    @Override
    public void whenCast(TargetSkillResult result, SkillMetadata skillMeta) {
        final LivingEntity target = result.getTarget();

        this.playParticleEffect(target.getLocation());
        target.getWorld().playSound(target.getLocation(), Sounds.ENTITY_WITHER_SHOOT, 2, 2);

        skillMeta.getCaster().attack(target, skillMeta.getParameter("damage"), damageTypes);
    }

    private void playParticleEffect(Location source) {
        new BukkitRunnable() {
            final double ran = Math.random() * Math.PI * 2;
            final Location origin = source.add(Math.cos(ran) * 3, 6, Math.sin(ran) * 3);
            final Vector vec = source.add(0, .65, 0).toVector().subtract(origin.toVector()).multiply(.05);
            double ti = 0;

            public void run() {
                origin.getWorld().playSound(origin, Sounds.BLOCK_NOTE_BLOCK_HAT, 2, 2);
                for (int j = 0; j < 2; j++) {
                    ti += .05;

                    origin.add(vec);
                    origin.getWorld().spawnParticle(VParticle.FIREWORK.get(), origin, 1, .04, 0, .04, 0);
                    if (ti >= 1) {
                        origin.getWorld().spawnParticle(VParticle.FIREWORK.get(), origin, 24, 0, 0, 0, .12);
                        origin.getWorld().playSound(origin, Sounds.ENTITY_FIREWORK_ROCKET_BLAST, 1, 2);
                        cancel();
                    }
                }
            }
        }.runTaskTimer(MythicLib.plugin, 0, 1);
    }
}
