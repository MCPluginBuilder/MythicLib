package io.lumine.mythic.lib.listener.option;

import io.lumine.mythic.bukkit.events.MythicHealMechanicEvent;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.event.IndicatorDisplayEvent;
import io.lumine.mythic.lib.module.ListenerToggle;
import io.lumine.mythic.lib.module.Module;
import io.lumine.mythic.lib.module.ModuleInfo;
import io.lumine.mythic.lib.module.ModuleListener;
import io.lumine.mythic.lib.util.CustomFont;
import io.lumine.mythic.lib.util.IndicatorConfig;
import io.lumine.mythic.lib.util.config.YamlFile;
import io.lumine.mythic.lib.util.lang3.Validate;
import io.lumine.mythic.lib.version.Attributes;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ModuleInfo(key = "regen_indicators")
public class RegenIndicators extends Module {
    @Nullable
    private CustomFont font;
    private double minRegen;
    private IndicatorConfig indicatorConfig;

    @ModuleListener
    final InternalListener mainListener = new InternalListener();
    @ModuleListener
    final ListenerToggle mmListener = new ListenerToggle(this, HealIndicatorPatch::new);

    public RegenIndicators(MythicLib plugin) {
        super(plugin);
    }

    private ConfigurationSection config;

    @Override
    protected boolean shouldEnable() {
        this.config = new YamlFile("indicators").getContent().getConfigurationSection("regen_indicators");
        Validate.notNull(config, "Config cannot be null");
        return config.getBoolean("enabled");
    }

    @Override
    protected void onReload() {

        // General options
        indicatorConfig = new IndicatorConfig(config);

        this.font = config.getBoolean("custom_font.enabled") ? new CustomFont(config.getConfigurationSection("custom_font")) : null;
        this.minRegen = Math.max(config.getDouble("min_regen"), 0);
    }

    @Override
    protected void onEnable() {
        this.mmListener.toggle(Bukkit.getPluginManager().getPlugin("MythicMobs") != null);
    }

    public void triggerIndicators(EntityRegainHealthEvent event) {
        this.mainListener.displayIndicators(event);
    }

    private static final double HEAL_EPSILON = 1e-3;

    private class InternalListener implements Listener {

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void displayIndicators(EntityRegainHealthEvent event) {
            var entity = event.getEntity();
            if (!(entity instanceof LivingEntity)
                    || event.getAmount() <= minRegen
                    || ((LivingEntity) entity).getHealth() + HEAL_EPSILON > ((LivingEntity) entity).getAttribute(Attributes.MAX_HEALTH).getValue())
                return;

            // Display no indicator around vanished player
            if (entity instanceof Player && UtilityMethods.isVanished((Player) entity)) return;

            final var formattedNumber = indicatorConfig.formatNumber(event.getAmount());
            final var formattedDamage = font == null ? formattedNumber : font.format(formattedNumber);
            final var indicatorMessage = indicatorConfig.getRaw().replace("{value}", formattedDamage);
            indicatorConfig.displayIndicator(entity, indicatorMessage, getIndicatorDirection(entity), IndicatorDisplayEvent.IndicatorType.REGENERATION);
        }
    }

    private class HealIndicatorPatch implements Listener {

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void adaptMythicEvent(MythicHealMechanicEvent event) {
            final var called = new EntityRegainHealthEvent(event.getTarget(), event.getHealAmount(), EntityRegainHealthEvent.RegainReason.CUSTOM);
            triggerIndicators(called);
        }
    }

    /**
     * For non-player entities, a random direction is taken.
     * <p>
     * For players, direction is taken randomly in a PI/2
     * cone behind the player so that it does not bother the player
     *
     * @param entity Player or monster
     * @return Random (normalized) direction for the hologram
     */
    @NotNull
    private Vector getIndicatorDirection(Entity entity) {

        if (entity instanceof Player) {
            final double a = Math.toRadians(((Player) entity).getEyeLocation().getYaw()) + Math.PI * (1 + (Math.random() - .5) / 2);
            return new Vector(Math.cos(a), 0, Math.sin(a));
        }

        final double a = Math.random() * Math.PI * 2;
        return new Vector(Math.cos(a), 0, Math.sin(a));
    }
}
