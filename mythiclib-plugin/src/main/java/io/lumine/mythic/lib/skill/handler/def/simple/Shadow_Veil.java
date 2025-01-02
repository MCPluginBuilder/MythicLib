package io.lumine.mythic.lib.skill.handler.def.simple;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.version.VParticle;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class Shadow_Veil extends SkillHandler<SimpleSkillResult> {
    public Shadow_Veil() {
        super();

        registerModifiers("duration", "deception");
    }

    @Override
    public SimpleSkillResult getResult(SkillMetadata meta) {
        return new SimpleSkillResult();
    }

    @Override
    public void whenCast(SimpleSkillResult result, SkillMetadata skillMeta) {
        double duration = skillMeta.getParameter("duration");

        Player caster = skillMeta.getCaster().getPlayer();

        caster.getWorld().playSound(caster.getLocation(), Sounds.ENTITY_ENDERMAN_TELEPORT, 3, 0);
        for (Player online : Bukkit.getOnlinePlayers())
            online.hidePlayer(MythicLib.plugin, caster);

        // Clears the target of any entity around the player
        for (Mob serverEntities : caster.getWorld().getEntitiesByClass(Mob.class))
            if (serverEntities.getTarget() != null && serverEntities.getTarget().equals(caster))
                serverEntities.setTarget(null);

        ShadowVeilEffect svh = new ShadowVeilEffect(caster, duration);
        svh.setDeceptions(SilentNumbers.floor(skillMeta.getParameter("deception")));
    }

    public static class ShadowVeilEffect extends BukkitRunnable implements Listener {
        private final Player player;
        private final double duration;
        private final Location loc;

        int deceptions = 1;

        public void setDeceptions(int dec) {
            deceptions = dec;
        }

        double ti = 0;
        double y = 0;

        public ShadowVeilEffect(Player player, double duration) {
            this.player = player;
            this.duration = duration;
            this.loc = player.getLocation();

            runTaskTimer(MythicLib.plugin, 0, 1);
            Bukkit.getPluginManager().registerEvents(this, MythicLib.plugin);
        }

        private void close() {
            if (ti < 0)
                return;

            player.getWorld().spawnParticle(VParticle.LARGE_SMOKE.get(), player.getLocation().add(0, 1, 0), 32, 0, 0, 0, .13);
            player.getWorld().playSound(player.getLocation(), Sounds.ENTITY_ENDERMAN_TELEPORT, 3, 0);

            // sets time to -1 so that next calls know the handler has already
            // been closed
            ti = -1;
            EntityDamageByEntityEvent.getHandlerList().unregister(this);
            EntityTargetEvent.getHandlerList().unregister(this);

            for (Player online : Bukkit.getOnlinePlayers())
                online.showPlayer(MythicLib.plugin, player);

            cancel();
        }

        @Override
        public void run() {
            if (ti++ > duration * 20 || UtilityMethods.isInvalidated(player)) {
                close();
                return;
            }

            if (y < 4)
                for (int j1 = 0; j1 < 5; j1++) {
                    y += .04;
                    for (int j = 0; j < 4; j++) {
                        double a = y * Math.PI * .8 + (j * Math.PI / 2);
                        player.getWorld().spawnParticle(VParticle.LARGE_SMOKE.get(), loc.clone().add(Math.cos(a) * 2.5, y, Math.sin(a) * 2.5), 0);
                    }
                }

        }

        @EventHandler
        public void cancelShadowVeil(EntityDamageByEntityEvent event) {
            if (event.getDamager().equals(player)) {
                deceptions--;
                if (deceptions <= 0)
                    close();
            }
        }

        @EventHandler
        public void cancelMobTarget(EntityTargetEvent event) {
            if (event.getTarget() != null && event.getTarget().equals(player))
                event.setCancelled(true);
        }
    }
}
