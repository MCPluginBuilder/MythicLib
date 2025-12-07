package io.lumine.mythic.lib.listener.option;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.script.util.expression.numeric.NumericExpression;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.logging.Level;

public class VanillaDamageModifiers implements Listener {
    private final EnumMap<EntityDamageEvent.DamageCause, String> formulas = new EnumMap<>(EntityDamageEvent.DamageCause.class);

    public VanillaDamageModifiers(@NotNull ConfigurationSection config) {
        config = config.getConfigurationSection("source");
        if (config == null) return;

        for (String key : config.getKeys(false))
            try {
                EntityDamageEvent.DamageCause cause = EntityDamageEvent.DamageCause.valueOf(UtilityMethods.enumName(key));
                formulas.put(cause, config.getString(key));
            } catch (RuntimeException exception) {
                MythicLib.plugin.getLogger().log(Level.WARNING, "Could not load vanilla damage modifier '" + key + "': " + exception.getMessage());
            }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void a(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player) || UtilityMethods.isFake(event)) return;
        final Player player = (Player) event.getEntity();

        @Nullable String formula = formulas.get(event.getCause());
        if (formula == null) return;

        try {
            formula = applyPlaceholders(formula, event, player);
            final double result = NumericExpression.eval(formula);
            event.setDamage(result);
        } catch (Exception exception) {
            MythicLib.plugin.getLogger().log(Level.WARNING, "Could not evaluate formula '" + formula + "' for player '" + player.getUniqueId() + "': " + exception.getMessage());
        }
    }

    @NotNull
    private String applyPlaceholders(@NotNull String str, @NotNull EntityDamageEvent event, @NotNull Player player) {
        str = str.replace("{event_damage}", String.valueOf(event.getDamage()));
        str = str.replace("{final_damage}", String.valueOf(event.getFinalDamage()));
        str = MythicLib.plugin.getPlaceholderParser().parse(player, str);
        return str;
    }
}
