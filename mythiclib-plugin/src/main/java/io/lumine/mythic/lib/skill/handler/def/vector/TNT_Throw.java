package io.lumine.mythic.lib.skill.handler.def.vector;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.BuiltinSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.VectorSkillResult;
import io.lumine.mythic.lib.util.TemporaryHandler;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

@BuiltinSkillHandler(mods = {"force"})
public class TNT_Throw extends SkillHandler<VectorSkillResult> {
    public TNT_Throw(ConfigurationSection config) {
        super(config);
    }

    @Override
    public @NotNull VectorSkillResult getResult(SkillMetadata meta) {
        return new VectorSkillResult(meta);
    }

    @Override
    public void whenCast(VectorSkillResult result, SkillMetadata skillMeta) {
        Player caster = skillMeta.getCaster().getPlayer();

        Vector vec = result.getTarget().multiply(2 * skillMeta.getParameter("force"));
        TNTPrimed tnt = caster.getWorld().spawn(caster.getLocation().add(0, 1, 0), TNTPrimed.class);
        tnt.setFuseTicks(80);
        tnt.setVelocity(vec);
        new Handler(skillMeta.getCaster().getData(), tnt);
        caster.getWorld().playSound(caster.getLocation(), Sounds.ENTITY_SNOWBALL_THROW, 1, 0);
        caster.getWorld().spawnParticle(VParticle.EXPLOSION.get(), caster.getLocation().add(0, 1, 0), 12, 0, 0, 0, .1);
    }

    /**
     * Used to cancel team damage and other things
     */
    static class Handler extends TemporaryHandler {
        private final Player player;
        private final TNTPrimed tnt;

        public Handler(MMOPlayerData playerData, TNTPrimed tnt) {
            super(playerData);

            this.player = playerData.getPlayer();
            this.tnt = tnt;

            closeAfter(120);
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void a(EntityDamageByEntityEvent event) {
            if (event.getDamager().equals(tnt) && !UtilityMethods.canTarget(player, event.getEntity()))
                event.setCancelled(true);
        }
    }
}
