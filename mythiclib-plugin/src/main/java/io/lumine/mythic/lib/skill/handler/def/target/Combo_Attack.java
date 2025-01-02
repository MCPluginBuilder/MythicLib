package io.lumine.mythic.lib.skill.handler.def.target;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.comp.interaction.InteractionType;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class Combo_Attack extends SkillHandler<TargetSkillResult> {
    public Combo_Attack() {
        super();

        registerModifiers("damage", "count");
    }

    @Override
    public TargetSkillResult getResult(SkillMetadata meta) {
        return new TargetSkillResult(meta, 10, InteractionType.OFFENSE_SKILL);
    }

    private static final long ATTACK_PERIOD = 5;

    @Override
    public void whenCast(TargetSkillResult result, SkillMetadata skillMeta) {
        final int count = (int) Math.max(1, skillMeta.getParameter("count"));
        final double damage = skillMeta.getParameter("damage") / count;
        final LivingEntity target = result.getTarget();

        playEffect(target);
        skillMeta.getCaster().attack(target, damage, DamageType.SKILL, DamageType.PHYSICAL);

        new BukkitRunnable() {
            int counter = 1;

            @Override
            public void run() {
                if (counter++ >= count || UtilityMethods.isInvalidated(skillMeta.getCaster()) || target.isDead()) {
                    cancel();
                    return;
                }

                playEffect(target);
                MythicLib.plugin.getDamage().registerAttack(new AttackMetadata(new DamageMetadata(damage, DamageType.SKILL, DamageType.PHYSICAL), target, skillMeta.getCaster()), true, true);
            }
        }.runTaskTimer(MythicLib.plugin, ATTACK_PERIOD, ATTACK_PERIOD);
    }

    private void playEffect(Entity target) {
        target.getWorld().playSound(target.getLocation(), Sounds.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1, 2);
        target.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0, target.getHeight() / 2, 0), 24, 0, 0, 0, .7);
    }
}
