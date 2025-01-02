package io.lumine.mythic.lib.skill.handler.def.simple;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.util.TemporaryListener;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.util.SmallParticleEffect;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;

public class Evade extends SkillHandler<SimpleSkillResult> {
    public Evade() {
        super();

        registerModifiers("duration");
    }

    @Override
    public SimpleSkillResult getResult(SkillMetadata meta) {
        return new SimpleSkillResult();
    }

    @Override
    public void whenCast(SimpleSkillResult result, SkillMetadata skillMeta) {
        Player caster = skillMeta.getCaster().getPlayer();
        caster.getWorld().playSound(caster.getLocation(), Sounds.ENTITY_ENDERMAN_TELEPORT, 1, 2);
        new SmallParticleEffect(caster, Particle.CLOUD);
        new EvadeSkill(skillMeta.getCaster().getData(), skillMeta.getParameter("duration"));
    }

    private static class EvadeSkill extends TemporaryListener {
        private final MMOPlayerData data;

        public EvadeSkill(MMOPlayerData data, double duration) {
            this.data = data;

            close((long) (20 * duration));
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void a(EntityDamageEvent event) {
            if (UtilityMethods.isInvalidated(data)) {
                close();
                return;
            }

            if (event.getEntity().equals(data.getPlayer())) event.setCancelled(true);
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void b(PlayerAttackEvent event) {
            if ((event.getAttack().getDamage().hasType(DamageType.WEAPON) || event.getAttack().getDamage().hasType(DamageType.UNARMED))
                    && event.getAttacker().getData().equals(data))
                close();
        }
    }
}
