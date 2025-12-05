package io.lumine.mythic.lib.skill.handler.def.target;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.event.AttackEvent;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.comp.interaction.InteractionType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.BuiltinSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import io.lumine.mythic.lib.util.TemporaryHandler;
import io.lumine.mythic.lib.version.Attributes;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@BuiltinSkillHandler(mods = {"reduction", "redirect", "duration", "low"})
public class Human_Shield extends SkillHandler<TargetSkillResult> {
    public Human_Shield(ConfigurationSection config) {
        super(config);
    }

    @Override
    public @NotNull TargetSkillResult getResult(SkillMetadata meta) {
        TargetSkillResult result = new TargetSkillResult(meta, 7, InteractionType.SUPPORT_SKILL);
        return result.isSuccessful() && result.getTarget() instanceof Player ? result : new TargetSkillResult((LivingEntity) null);
    }

    @Override
    public void whenCast(TargetSkillResult result, SkillMetadata skillMeta) {
        Player caster = skillMeta.getCaster().getPlayer();
        caster.getWorld().playSound(caster.getLocation(), Sounds.ENTITY_BLAZE_AMBIENT, 1, 1);
        new Handler(skillMeta.getCaster().getData(), (Player) result.getTarget(), skillMeta.getParameter("reduction"), skillMeta.getParameter("redirect"), skillMeta.getParameter("duration"), skillMeta.getParameter("low"));
    }

    static class Handler extends TemporaryHandler {
        private final MMOPlayerData caster;
        private final Player target;
        private final double damageCoefficient, redirectRate, duration, minimumHealthPercentage;

        private int j;

        public Handler(MMOPlayerData caster, Player target, double reduction, double redirect, double duration, double low) {
            super(caster);

            this.target = target;
            this.caster = caster;

            damageCoefficient = 1 - Math.min(1, reduction / 100);
            redirectRate = redirect / 100;
            this.duration = duration * 20;
            minimumHealthPercentage = low / 100;

            runTask(r -> r.runTaskTimer(MythicLib.plugin, 0, 1));
        }

        @Override
        protected @Nullable BukkitRunnable newTask() {
            return new BukkitRunnable() {

                @Override
                public void run() {
                    if (UtilityMethods.isInvalidated(target) || j++ >= duration) {
                        Handler.this.close();
                        return;
                    }

                    double a = (double) j / 5;
                    target.getWorld().spawnParticle(VParticle.HAPPY_VILLAGER.get(), target.getLocation().add(Math.cos(a), 1 + Math.sin(a / 3) / 1.3, Math.sin(a)), 0);
                }
            };
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void a(AttackEvent event) {
            if (event.getEntity().equals(target)) {

                final double initialDamage = event.getDamage().getDamage();
                event.getDamage().multiplicativeModifier(damageCoefficient);

                double health = caster.getPlayer().getHealth() - initialDamage * redirectRate;
                if (health > caster.getPlayer().getAttribute(Attributes.MAX_HEALTH).getValue() * minimumHealthPercentage)
                    caster.getPlayer().setHealth(health);
                else {
                    caster.getPlayer().setHealth(1);
                    close();
                }
            }
        }
    }
}
