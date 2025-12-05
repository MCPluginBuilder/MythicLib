package io.lumine.mythic.lib.skill.handler.def.simple;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.BuiltinSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.util.TemporaryHandler;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@BuiltinSkillHandler(mods = {"duration", "deception"})
public class Shadow_Veil extends SkillHandler<SimpleSkillResult> {
    public Shadow_Veil(ConfigurationSection config) {
        super(config);
    }

    @Override
    public @NotNull SimpleSkillResult getResult(SkillMetadata meta) {
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

        new ShadowVeilEffect(skillMeta.getCaster(), duration, (int) skillMeta.getParameter("deception"));
    }

    public static class ShadowVeilEffect extends TemporaryHandler {
        private final Player player;
        private final PlayerMetadata caster;
        private final double duration;
        private final Location loc;

        /**
         * Hits left before the veil breaks
         */
        int hitsLeft;


        public ShadowVeilEffect(PlayerMetadata caster, double duration, int hitsLeft) {
            this.player = caster.getPlayer();
            this.caster = caster;
            this.duration = duration;
            this.loc = player.getLocation();
            this.hitsLeft = hitsLeft;

            runTask(r -> r.runTaskTimer(MythicLib.plugin, 0, 1));
        }

        @Override
        protected @Nullable BukkitRunnable newTask() {
            return new BukkitRunnable() {
                double ti = 0;
                double y = 0;

                @Override
                public void run() {
                    if (ti++ > duration * 20 || UtilityMethods.isInvalidated(caster)) {
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
            };
        }

        @Override
        protected void onClose() {
            player.getWorld().spawnParticle(VParticle.LARGE_SMOKE.get(), player.getLocation().add(0, 1, 0), 32, 0, 0, 0, .13);
            player.getWorld().playSound(player.getLocation(), Sounds.ENTITY_ENDERMAN_TELEPORT, 3, 0);

            for (Player online : Bukkit.getOnlinePlayers())
                online.showPlayer(MythicLib.plugin, player);
        }

        @EventHandler
        public void cancelShadowVeil(EntityDamageByEntityEvent event) {
            if (event.getDamager().equals(player)) {
                hitsLeft--;
                if (hitsLeft <= 0)
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
