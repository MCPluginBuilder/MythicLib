package io.lumine.mythic.lib.listener.option;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.event.IndicatorDisplayEvent;
import io.lumine.mythic.lib.version.Attributes;
import io.lumine.mythic.lib.util.CustomFont;
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

    public RegenIndicators(ConfigurationSection config) {
        super(config);

        font = config.getBoolean("custom-font.enabled") ? new CustomFont(config.getConfigurationSection("custom-font")) : null;
    }

    private static final double HEAL_EPSILON = 1e-3;

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void displayIndicators(EntityRegainHealthEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity)
                || event.getAmount() <= 0
                || ((LivingEntity) entity).getHealth() + HEAL_EPSILON > ((LivingEntity) entity).getAttribute(Attributes.MAX_HEALTH).getValue())
            return;

        // Display no indicator around vanished player
        if (entity instanceof Player && UtilityMethods.isVanished((Player) entity)) return;

        final String formattedNumber = formatNumber(event.getAmount());
        final String formattedDamage = font == null ? formattedNumber : font.format(formattedNumber);
        final String indicatorMessage = getRaw().replace("#", formattedDamage);
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
