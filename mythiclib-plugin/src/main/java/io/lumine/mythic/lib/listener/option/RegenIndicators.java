package io.lumine.mythic.lib.listener.option;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.event.IndicatorDisplayEvent;
import io.lumine.mythic.lib.util.CustomFont;
import io.lumine.mythic.lib.version.Attributes;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

public class RegenIndicators extends GameIndicators {
    @Nullable
    private final CustomFont font;
    private final double minRegen;

    public RegenIndicators(ConfigurationSection config) {
        super(config);

        this.font = config.getBoolean("custom-font.enabled") ? new CustomFont(config.getConfigurationSection("custom-font")) : null;
        this.minRegen = Math.max(config.getDouble("min_regen"), 0);
    }

    private static final double HEAL_EPSILON = 1e-3;

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void displayIndicators(EntityRegainHealthEvent event) {
        var entity = event.getEntity();
        if (!(entity instanceof LivingEntity)
                || event.getAmount() <= minRegen
                || ((LivingEntity) entity).getHealth() + HEAL_EPSILON > ((LivingEntity) entity).getAttribute(Attributes.MAX_HEALTH).getValue())
            return;

        // Display no indicator around vanished player
        if (entity instanceof Player && UtilityMethods.isVanished((Player) entity)) return;

        final var formattedNumber = formatNumber(event.getAmount());
        final var formattedDamage = font == null ? formattedNumber : font.format(formattedNumber);
        final var indicatorMessage = getRaw().replace("{value}", formattedDamage);
        displayIndicator(entity, indicatorMessage, getIndicatorDirection(entity), IndicatorDisplayEvent.IndicatorType.REGENERATION);
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
    private Vector getIndicatorDirection(Entity entity) {

        if (entity instanceof Player) {
            final double a = Math.toRadians(((Player) entity).getEyeLocation().getYaw()) + Math.PI * (1 + (RANDOM.nextDouble() - .5) / 2);
            return new Vector(Math.cos(a), 0, Math.sin(a));
        }

        final double a = RANDOM.nextDouble() * Math.PI * 2;
        return new Vector(Math.cos(a), 0, Math.sin(a));
    }
}
