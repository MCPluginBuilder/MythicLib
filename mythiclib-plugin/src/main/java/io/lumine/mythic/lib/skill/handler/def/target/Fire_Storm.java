package io.lumine.mythic.lib.skill.handler.def.target;

import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.BuiltinSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import io.lumine.mythic.lib.util.ParabolicProjectile;
import io.lumine.mythic.lib.util.TemporaryHandler;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@BuiltinSkillHandler(mods = {"damage", "ignite"})
public class Fire_Storm extends SkillHandler<TargetSkillResult> {
    private final List<DamageType> damageTypes;

    public Fire_Storm(ConfigurationSection config) {
        super(config);

        damageTypes = DamageType.listFromConfig(List.of(DamageType.SKILL, DamageType.MAGIC, DamageType.PROJECTILE), config.get("damage_types"));
    }

    @Override
    public @NotNull TargetSkillResult getResult(SkillMetadata meta) {
        return new TargetSkillResult(meta);
    }

    @Override
    public void whenCast(TargetSkillResult result, SkillMetadata skillMeta) {
        Player caster = skillMeta.getCaster().getPlayer();
        LivingEntity target = result.getTarget();

        final double damage = skillMeta.getParameter("damage");
        final int ignite = (int) (20 * skillMeta.getParameter("ignite"));

        caster.getPlayer().getWorld().playSound(caster.getPlayer().getLocation(), Sounds.ENTITY_FIREWORK_ROCKET_BLAST, 1, 1);
        TemporaryHandler.timerTask(skillMeta.getCaster().getData(), 4, handler -> new BukkitRunnable() {
            int j = 0;

            @Override
            public void run() {
                if (j++ > 5 || target.isDead() || !caster.getPlayer().getWorld().equals(target.getWorld())) {
                    handler.close();
                    return;
                }

                // TODO dynamic target location

                caster.getPlayer().getWorld().playSound(caster.getPlayer().getLocation(), Sounds.BLOCK_FIRE_AMBIENT, 1, 1);
                new ParabolicProjectile(caster.getPlayer().getLocation().add(0, 1, 0), target.getLocation().add(0, target.getHeight() / 2, 0),
                        randomVector(caster.getPlayer()), () -> {
                    target.getWorld().playSound(target.getLocation(), Sounds.ENTITY_FIREWORK_ROCKET_TWINKLE, 1, 2);
                    target.getWorld().spawnParticle(VParticle.SMOKE.get(), target.getLocation().add(0, target.getHeight() / 2, 0), 8, 0, 0, 0, .15);
                    skillMeta.getCaster().attack(target, damage, damageTypes);
                    target.setFireTicks(ignite);

                }, 2, Particle.FLAME);
            }
        });
    }

    private Vector randomVector(Player player) {
        double a = Math.toRadians(player.getEyeLocation().getYaw() + 90);
        a += (RANDOM.nextBoolean() ? 1 : -1) * (Math.random() * 2 + 1) * Math.PI / 6;
        return new Vector(Math.cos(a), .8, Math.sin(a)).normalize().multiply(.4);
    }
}
