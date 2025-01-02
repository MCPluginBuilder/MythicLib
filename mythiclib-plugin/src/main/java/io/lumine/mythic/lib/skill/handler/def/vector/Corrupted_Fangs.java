package io.lumine.mythic.lib.skill.handler.def.vector;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.util.TemporaryListener;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.VectorSkillResult;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class Corrupted_Fangs extends SkillHandler<VectorSkillResult> {
    public Corrupted_Fangs() {
        super();

        registerModifiers("damage", "fangs");
    }

    @Override
    public VectorSkillResult getResult(SkillMetadata meta) {
        return new VectorSkillResult(meta);
    }

    @Override
    public void whenCast(VectorSkillResult result, SkillMetadata skillMeta) {
        Player caster = skillMeta.getCaster().getPlayer();

        caster.getWorld().playSound(caster.getLocation(), Sounds.ENTITY_WITHER_SHOOT, 2, 2);
        new BukkitRunnable() {
            final Vector vec = result.getTarget().setY(0).multiply(2);
            final Location loc = caster.getLocation();
            final FangsHandler handler = new FangsHandler(skillMeta.getCaster(), skillMeta.getParameter("damage"));
            final double fangAmount = skillMeta.getParameter("fangs");
            double ti = 0;

            public void run() {
                if (ti++ >= fangAmount) {
                    handler.close(3 * 20);
                    cancel();
                    return;
                }

                loc.add(vec);
                EvokerFangs evokerFangs = (EvokerFangs) caster.getWorld().spawnEntity(loc, EntityType.EVOKER_FANGS);
                handler.entities.add(evokerFangs.getEntityId());
            }
        }.runTaskTimer(MythicLib.plugin, 0, 1);
    }

    public static class FangsHandler extends TemporaryListener {
        private final Set<Integer> entities = new HashSet<>();
        private final PlayerMetadata caster;
        private final double skillDamage;

        public FangsHandler(PlayerMetadata caster, double skillDamage) {
            this.caster = caster;
            this.skillDamage = skillDamage;
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void a(EntityDamageByEntityEvent event) {
            if (event.getDamager() instanceof EvokerFangs && entities.contains(event.getDamager().getEntityId())) {
                event.setCancelled(true);

                if (UtilityMethods.canTarget(caster.getPlayer(), event.getEntity()))
                    caster.attack((LivingEntity) event.getEntity(), skillDamage, DamageType.MAGIC, DamageType.SKILL);
            }
        }
    }
}
