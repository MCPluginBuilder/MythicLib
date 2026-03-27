package io.lumine.mythic.lib.skill.handler.def.passive;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.player.resource.Resources;
import io.lumine.mythic.lib.player.skill.PassiveSkill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.BuiltinSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.AttackSkillResult;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@BuiltinSkillHandler(mods = {"drain"}, triggerable = false)
public class Vampirism extends SkillHandler<AttackSkillResult> implements Listener {
    private final List<DamageType> damageTypes;

    public Vampirism(ConfigurationSection config) {
        super(config);

        damageTypes = DamageType.listFromConfig(List.of(DamageType.WEAPON), config.get("damage_types"));
    }

    @Override
    public @NotNull AttackSkillResult getResult(SkillMetadata meta) {
        return new AttackSkillResult(meta);
    }

    @Override
    public void whenCast(AttackSkillResult result, SkillMetadata skillMeta) {
        LivingEntity target = result.getTarget();
        Player caster = skillMeta.getCaster().getPlayer();

        playParticleEffect(target.getLocation());
        target.getWorld().playSound(target.getLocation(), Sounds.ENTITY_WITCH_DRINK, 1, 2);
        Resources.heal(caster, skillMeta.getAttackSource().getDamage().getDamage() * skillMeta.getParameter("drain") / 100);
    }

    private static void playParticleEffect(Location loc) {
        new BukkitRunnable() {
            double ti = 0;
            double dis = 0;

            public void run() {
                for (int j1 = 0; j1 < 4; j1++) {
                    ti += .75;
                    dis += ti <= 10 ? .15 : -.15;

                    for (double j = 0; j < Math.PI * 2; j += Math.PI / 4)
                        loc.getWorld().spawnParticle(VParticle.REDSTONE.get(),
                                loc.clone().add(Math.cos(j + (ti / 20)) * dis, 0, Math.sin(j + (ti / 20)) * dis), 1,
                                new Particle.DustOptions(Color.RED, 1));
                }
                if (ti >= 17) cancel();
            }
        }.runTaskTimer(MythicLib.plugin, 0, 1);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void a(PlayerAttackEvent event) {
        if (!event.getAttack().getDamage().hasAnyType(damageTypes)) return;

        PassiveSkill skill = event.getAttacker().getData().getPassiveSkillMap().getSkill(this);
        if (skill == null) return;

        skill.getTriggeredSkill().cast(SkillMetadata.of(event));
    }
}
