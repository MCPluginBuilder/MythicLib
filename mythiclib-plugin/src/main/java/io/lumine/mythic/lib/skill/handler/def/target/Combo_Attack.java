package io.lumine.mythic.lib.skill.handler.def.target;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.comp.interaction.InteractionType;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.BuiltinSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import io.lumine.mythic.lib.util.TemporaryHandler;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@BuiltinSkillHandler(mods = {"damage", "count"})
public class Combo_Attack extends SkillHandler<TargetSkillResult> {
    private final List<DamageType> damageTypes;

    public Combo_Attack(ConfigurationSection config) {
        super(config);

        damageTypes = DamageType.listFromConfig(List.of(DamageType.SKILL, DamageType.PHYSICAL), config.get("damage_types"));
    }

    @Override
    public @NotNull TargetSkillResult getResult(SkillMetadata meta) {
        return new TargetSkillResult(meta, 10, InteractionType.OFFENSE_SKILL);
    }

    private static final long ATTACK_PERIOD = 5;

    @Override
    public void whenCast(TargetSkillResult result, SkillMetadata skillMeta) {
        final int count = (int) Math.max(1, skillMeta.getParameter("count"));
        final double damage = skillMeta.getParameter("damage") / count;
        final LivingEntity target = result.getTarget();

        playEffect(target);
        skillMeta.getCaster().attack(target, damage, damageTypes);

        TemporaryHandler.task(skillMeta.getCaster().getData(),
                runnable -> runnable.runTaskTimer(MythicLib.plugin, ATTACK_PERIOD, ATTACK_PERIOD),
                handler -> new BukkitRunnable() {
                    int counter = 1;

                    @Override
                    public void run() {
                        if (counter++ >= count || target.isDead()) {
                            handler.close();
                            return;
                        }

                        playEffect(target);
                        MythicLib.plugin.getDamage().registerAttack(new AttackMetadata(new DamageMetadata(damage, damageTypes), target, skillMeta.getCaster()), true, true);
                    }
                });
    }

    private void playEffect(Entity target) {
        target.getWorld().playSound(target.getLocation(), Sounds.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1, 2);
        target.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0, target.getHeight() / 2, 0), 24, 0, 0, 0, .7);
    }
}
