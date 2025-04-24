package io.lumine.mythic.lib.skill;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.event.skill.PlayerCastSkillEvent;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import io.lumine.mythic.lib.api.util.TemporaryListener;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import io.lumine.mythic.lib.skill.result.SkillResult;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BossBar;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class CastingDelayHandler extends TemporaryListener {
    private final SkillMetadata metadata;

    private final double slowness;
    private final int delayTicks;

    @Nullable
    private final BossBar bossbar;
    @Nullable
    private final NamespacedKey bossbarKey;

    private static final String MOVEMENT_SPEED_MODIFIER_KEY = "mythiclibSkillCasting";

    /**
     * Called when a skill has a non-null casting delay.
     *
     * @param metadata Information about skill being cast
     * @param result   Result of skill being cast
     */
    public CastingDelayHandler(SkillMetadata metadata, SkillResult result) {
        this.metadata = metadata;
        this.delayTicks = (int) (metadata.getParameter("delay") * 20);

        // Implement a runnable to run the task later
        registerRunnable(new BukkitRunnable() {
            private int counter = delayTicks;

            @Override
            public void run() {

                // If player dies or logs out
                if (UtilityMethods.isInvalidated(getCaster())) {
                    close();
                    return;
                }

                // Update bossbar
                if (bossbar != null) bossbar.setProgress((double) (delayTicks - counter) / (double) delayTicks);

                counter--;

                // Terminate and cast
                if (counter <= 0) {
                    close();
                    if (bossbarKey != null) Bukkit.getServer().removeBossBar(bossbarKey);

                    metadata.getCast().castInstantly(metadata, result);
                }
            }
        }, runnable -> runnable.runTaskTimer(MythicLib.plugin, 0, 1));

        // Play sound
        castIfNotNull(MythicLib.plugin.getMMOConfig().skillCastScript);

        // Slowness
        slowness = MythicLib.plugin.getMMOConfig().castingDelaySlowness;
        if (slowness > 0)
            new StatModifier(MOVEMENT_SPEED_MODIFIER_KEY, "MOVEMENT_SPEED", -slowness, ModifierType.RELATIVE).register(getCaster());

        // Bossbar
        if (MythicLib.plugin.getMMOConfig().enableCastingDelayBossbar) {
            bossbarKey = new NamespacedKey(MythicLib.plugin, "skill_casting_" + getCaster().getUniqueId());
            bossbar = Bukkit.createBossBar(bossbarKey,
                    MythicLib.plugin.getMMOConfig().castingDelayBossbarFormat,
                    MythicLib.plugin.getMMOConfig().castingDelayBarColor,
                    MythicLib.plugin.getMMOConfig().castingDelayBarStyle);
            bossbar.addPlayer(getCaster().getPlayer());
        } else {
            bossbarKey = null;
            bossbar = null;
        }
    }

    @NotNull
    public MMOPlayerData getCaster() {
        return metadata.getCaster().getData();
    }

    public boolean hasBossbar() {
        return bossbar != null;
    }

    private void castIfNotNull(@Nullable Skill skill) {
        if (skill != null) skill.cast(metadata);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ())
            return;

        if (!event.getPlayer().equals(getCaster().getPlayer()))
            return;

        // Should moving cancel skill casting
        if (MythicLib.plugin.getMMOConfig().castingDelayCancelOnMove) {
            event.setCancelled(true);
            castIfNotNull(MythicLib.plugin.getMMOConfig().skillCancelScript);
            close();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCast(PlayerCastSkillEvent event) {
        if (event.getPlayer().equals(getCaster().getPlayer()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onLogout(PlayerQuitEvent event) {
        if (event.getPlayer().equals(getCaster().getPlayer())) close();
    }

    @Override
    public void whenClosed() {
        bossbar.removeAll();
        // Clear slowness
        if (slowness > 0)
            getCaster().getStatMap().getInstance("MOVEMENT_SPEED").removeIf(MOVEMENT_SPEED_MODIFIER_KEY::equals);
    }
}
