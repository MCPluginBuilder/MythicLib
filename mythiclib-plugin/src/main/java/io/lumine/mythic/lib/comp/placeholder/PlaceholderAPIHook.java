package io.lumine.mythic.lib.comp.placeholder;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.manager.StatManager;
import io.lumine.mythic.lib.util.DefenseFormula;
import io.lumine.mythic.lib.util.lang3.Validate;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * A mythic placeholder that just passes
 * on to PAPI to do all the parsing.
 *
 * @author Gunging
 */
public class PlaceholderAPIHook extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "mythiclib";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Indyuce";
    }

    @Override
    public @NotNull String getVersion() {
        return MythicLib.plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {

        // Negative space
        if (params.startsWith("space_"))
            return UtilityMethods.getSpaceFont(Integer.parseInt(params.substring(6)));

        // Player-related Placeholders
        if (player == null) return null;

        if (params.startsWith("defense_damage_reduction")) {
            final double defenseStat = MMOPlayerData.get(player).getStatMap().getStat("DEFENSE");
            final double damageReduction = 100 - DefenseFormula.calculateDamage(false, defenseStat, 100);
            return MythicLib.plugin.getMMOConfig().decimal.format(damageReduction);
        }

        if (params.startsWith("raw_stat_")) {
            final String stat = UtilityMethods.enumName(params.substring(9));
            return String.valueOf(MMOPlayerData.get(player).getStatMap().getInstance(stat).getFinal());
        }

        if (params.startsWith("stat_")) {
            final String stat = UtilityMethods.enumName(params.substring(5));
            return StatManager.format(stat, MMOPlayerData.get(player));
        }

        if (params.startsWith("cooldown_")) {
            final String key = params.substring(9);
            return MythicLib.plugin.getMMOConfig().decimal.format(MMOPlayerData.get(player).getCooldownMap().getCooldown(key));
        }

        if (params.startsWith("decfmt_")) {
            final var split = params.substring(7).split("_", 2);
            final var format = MythicLib.plugin.getMMOConfig().newDecimalFormat(split[0]);
            final var value = Double.parseDouble(split[1]);
            return format.format(value);
        }

        if (params.startsWith("round_")) {
            final var split = params.substring(6).split("_", 2);
            final int places = Integer.parseInt(split[0]);
            final double value = Double.parseDouble(split[1]);
            if (places == 0) return String.valueOf(Math.round(value));
            Validate.isTrue(places > 0, "Decimal places must be non-negative");
            return String.format("%." + places + "f", value);
        }

        return null;
    }
}
