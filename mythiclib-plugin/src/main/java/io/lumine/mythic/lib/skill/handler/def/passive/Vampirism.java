package io.lumine.mythic.lib.skill.handler.def.passive;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.player.skill.PassiveSkill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.AttackSkillResult;
import io.lumine.mythic.lib.skill.trigger.TriggerMetadata;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class Vampirism extends SkillHandler<AttackSkillResult> implements Listener {
    public Vampirism() {
        super(false);
        registerModifiers("drain");
    }

    @Override
    public AttackSkillResult getResult(SkillMetadata meta) {
        return new AttackSkillResult(meta);
    }

    @Override
    public void whenCast(AttackSkillResult result, SkillMetadata skillMeta) {
        LivingEntity target = result.getTarget();
        Player caster = skillMeta.getCaster().getPlayer();

        new BukkitRunnable() {
            final Location loc = target.getLocation();
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
                if (ti >= 17)
                    cancel();
            }
        }.runTaskTimer(MythicLib.plugin, 0, 1);
        target.getWorld().playSound(target.getLocation(), Sounds.ENTITY_WITCH_DRINK, 1, 2);
        UtilityMethods.heal(caster, skillMeta.getAttackSource().getDamage().getDamage() * skillMeta.getParameter("drain") / 100);
    }


    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void a(PlayerAttackEvent event) {
        if (!event.getAttack().getDamage().hasType(DamageType.WEAPON))
            return;

        PassiveSkill skill = event.getAttacker().getData().getPassiveSkillMap().getSkill(this);
        if (skill == null)
            return;

        skill.getTriggeredSkill().cast(new TriggerMetadata(event, TriggerType.API));
    }
}
