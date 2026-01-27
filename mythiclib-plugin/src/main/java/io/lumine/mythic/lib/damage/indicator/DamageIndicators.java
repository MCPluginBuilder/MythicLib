package io.lumine.mythic.lib.damage.indicator;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.event.AttackUnregisteredEvent;
import io.lumine.mythic.lib.api.event.IndicatorDisplayEvent;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.module.Module;
import io.lumine.mythic.lib.module.ModuleInfo;
import io.lumine.mythic.lib.module.ModuleListener;
import io.lumine.mythic.lib.util.CustomFont;
import io.lumine.mythic.lib.util.FileUtils;
import io.lumine.mythic.lib.util.IndicatorConfig;
import io.lumine.mythic.lib.util.config.YamlFile;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Since 1.3.4 damage holograms are split into multiple damage types which
 * lets the user know what type of damage their are dealing.
 * Available "damage types" (elements are not damage types) are:
 * - physical damage
 * - magic damage
 * - elemental damage
 *
 * @author jules
 */
@ModuleInfo(key = "damage_indicators")
public class DamageIndicators extends Module {
    private final Map<DamageType, DamageTypeIcon> icons = new HashMap<>();

    private IndicatorConfig indicatorConfig;
    private boolean splitHolograms;
    private final List<DamageType> damageTypeSplits = new ArrayList<>();
    private double minDamage;
    private IndicatorGroupMode groupMode;
    private String splitHologramJoin, damageTypeIconJoin;

    @Nullable
    private CustomFont font, fontCrit;

    @ModuleListener
    @SuppressWarnings("unused")
    final Listener mainListener = new InternalListener();

    public DamageIndicators(MythicLib plugin) {
        super(plugin);
    }

    @Override
    protected void onReset() {
        damageTypeSplits.clear();
        icons.clear();
    }

    private ConfigurationSection config;

    @Override
    protected boolean shouldEnable() {
        FileUtils.copyDefaultFile(MythicLib.plugin, "indicators.yml");

        this.config = new YamlFile("indicators").getContent().getConfigurationSection("damage_indicators");
        Validate.notNull(config, "Damage indicators config section cannot be null");
        return config.getBoolean("enabled");
    }

    @Override
    protected void onReload() {

        // General config of indicators
        this.indicatorConfig = new IndicatorConfig(config);

        // Groupby option
        groupMode = UtilityMethods.prettyValueOf(IndicatorGroupMode::valueOf, config.getString("group_by"), "No group mode with ID %s");

        // Damage type icons
        for (var damageType : DamageType.values())
            if (config.contains("icon." + damageType.name().toLowerCase()))
                this.icons.put(damageType, DamageTypeIcon.fromConfig(config.get("icon." + damageType.name().toLowerCase())));

        // Damage type splits
        for (var damageTypeName : config.getStringList("damage_type_splits"))
            this.damageTypeSplits.add(UtilityMethods.prettyValueOf(DamageType::valueOf, damageTypeName, "No damage type with ID %s"));

        this.splitHolograms = config.getBoolean("split_holograms");
        this.minDamage = Math.max(config.getDouble("min_damage"), DamageMetadata.MINIMAL_DAMAGE);
        this.splitHologramJoin = config.getString("holograms_join", " ");
        this.damageTypeIconJoin = config.getString("damage_type_icon_join", "");

        // Custom fonts
        if (config.getBoolean("custom_font.enabled")) {
            font = new CustomFont(config.getConfigurationSection("custom_font.normal"));
            fontCrit = new CustomFont(config.getConfigurationSection("custom_font.crit"));
        } else {
            font = null;
            fontCrit = null;
        }
    }

    public List<DamageType> getDamageTypeSplits() {
        return damageTypeSplits;
    }

    private class InternalListener implements Listener {

        @EventHandler
        public void displayIndicators(AttackUnregisteredEvent event) {
            final var entity = event.getEntity();
            if (event.getDamage().getDamage() < minDamage) return;

            /*
             * Bukkit calls multiple damage events during the invulnerability
             * period of the entity. This creates invalid events which do not
             * lead to any damage dealt to the entity.
             *
             * https://hub.spigotmc.org/jira/browse/SPIGOT-7983
             *
             * This is only an issue with Spigot, Paper builds do not have this
             * issue. Once the bug above is fixed, this code can be safely removed.
             *
             * Fixes MMOItems#1637
             */
            if (!MythicLib.plugin.getVersion().isPaper()) {
                final var invulLeft = event.getEntity().getNoDamageTicks();
                final var invulMax = event.getEntity().getMaximumNoDamageTicks();
                if (invulLeft > invulMax / 2) return;
            }

            // Display no indicator around vanished player
            if (entity instanceof Player && UtilityMethods.isVanished((Player) entity)) return;

            // Calculate holograms
            // Take into account DAMAGE MODIFIERS (bug fix) i.e change in event damage for external compatibility
            final var indicators = groupMode.getIndicators(event.getDamage());
            final var holograms = new ArrayList<String>();
            final var modifierDue = (event.toBukkit().getFinalDamage() - event.getDamage().getDamage()) / Math.max(1, indicators.size());
            for (var indicator : groupMode.getIndicators(event.getDamage())) {
                // Ignore minimal damage values
                final var damageValue = indicator.getValue() + modifierDue;
                if (damageValue > 0) holograms.add(computeFormat(event.getDamage(), indicator));
            }

            // Use multiple holograms
            if (splitHolograms) {
                for (var holo : holograms)
                    DamageIndicators.this.indicatorConfig.displayIndicator(entity, holo, getDirection(event.toBukkit()), IndicatorDisplayEvent.IndicatorType.DAMAGE);
            }

            // One single hologram
            else {
                final var joined = String.join(splitHologramJoin, holograms);
                DamageIndicators.this.indicatorConfig.displayIndicator(entity, joined, getDirection(event.toBukkit()), IndicatorDisplayEvent.IndicatorType.DAMAGE);
            }
        }
    }

    /**
     * If MythicLib can find a damager, display the hologram
     * in a cone which direction is the damager-target line.
     *
     * @param event Damage event
     * @return Direction of the hologram
     */
    @NotNull
    private Vector getDirection(EntityDamageEvent event) {

        if (event instanceof EntityDamageByEntityEvent) {
            Vector dir = event.getEntity().getLocation().toVector().subtract(((EntityDamageByEntityEvent) event).getDamager().getLocation().toVector()).setY(0);
            if (dir.lengthSquared() > 0) {

                // Calculate angle of attack
                double a = Math.atan2(dir.getZ(), dir.getX());

                // Random angle offset
                a += Math.PI / 2 * (Math.random() - .5);

                return new Vector(Math.cos(a), 0, Math.sin(a));
            }
        }

        double a = Math.random() * Math.PI * 2;
        return new Vector(Math.cos(a), 0, Math.sin(a));
    }

    @NotNull
    private String computeFormat(DamageMetadata damage, DamageIndicator indicator) {
        final var crit = damage.isCrit(indicator);
        final @Nullable var indicatorFont = crit && fontCrit != null ? fontCrit : font;
        final @NotNull var formattedDamage = indicatorFont == null
                ? indicatorConfig.formatNumber(indicator.getValue())
                : indicatorFont.format(indicatorConfig.formatNumber(indicator.getValue()));

        return MythicLib.plugin.getPlaceholderParser().parse(null, indicatorConfig.getRaw()
                .replace("{icon}", computeIcon(indicator, crit))
                .replace("{value}", formattedDamage));
    }

    @NotNull
    private String computeIcon(DamageIndicator indicator, boolean crit) {
        // TODO rewrite after custom damage type update
        final var build = new StringBuilder();
        var empty = true;

        //dtypes
        for (var dtype : indicator.getTooltips()) {
            final var mapping = icons.get(dtype);
            if (mapping == null) continue;
            if (!empty) build.append(damageTypeIconJoin);
            empty = false;
            build.append(mapping.getIcon(crit));
        }

        // element
        if (indicator.getElement() != null) {
            if (!empty) build.append(damageTypeIconJoin);
            empty = false;
            build.append(indicator.getElement().getColor() + indicator.getElement().getLoreIcon());
        }

        return build.toString();
    }
}
