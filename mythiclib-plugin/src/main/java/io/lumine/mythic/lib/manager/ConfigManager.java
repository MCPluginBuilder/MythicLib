package io.lumine.mythic.lib.manager;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.comp.interaction.relation.EmptyPvPInteractionRules;
import io.lumine.mythic.lib.comp.interaction.relation.InteractionRules;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.module.MMOPlugin;
import io.lumine.mythic.lib.module.Module;
import io.lumine.mythic.lib.module.ModuleInfo;
import io.lumine.mythic.lib.rpg.provided.ResourceDisplayOptions;
import io.lumine.mythic.lib.script.Script;
import io.lumine.mythic.lib.util.annotation.BackwardsCompatibility;
import io.lumine.mythic.lib.util.config.YamlFile;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.EnumMap;
import java.util.List;
import java.util.logging.Level;

@ModuleInfo(key = "config")
public class ConfigManager extends Module {
    public final DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols();

    public DecimalFormat decimal, decimals;
    public boolean playerAbilityDamage, castingDelayCancelOnMove, enableCastingDelayBossbar, fixTooLargePackets, debugMode = true,
            ignoreShiftTriggers, ignoreOffhandClickTriggers, skipElementalDamageApplication, flagCheckSkills;
    public String naturalDefenseFormula, elementalDefenseFormula, castingDelayBossbarFormat;
    public BarColor castingDelayBarColor;
    public BarStyle castingDelayBarStyle;
    public double castingDelaySlowness;
    public int maxSyncTries;
    public List<DamageType> meleeWeaponAttackTypes, meleeUnarmedAttackTypes, meleeRandomAttackTypes, bowAttackTypes, skillAttackTypes;
    public final EnumMap<EntityDamageEvent.DamageCause, List<DamageType>> damageCauseMap = new EnumMap<>(EntityDamageEvent.DamageCause.class);

    @NotNull
    public InteractionRules interactionRules;

    @Nullable
    public Script skillCastScript, skillCancelScript;

    public int manaRefreshRate;
    public double manaLoginRatio, staminaLoginRatio;
    public ResourceDisplayOptions manaDisplay, staminaDisplay;


    public ConfigManager(MMOPlugin plugin) {
        super(plugin);
    }

    @Override
    protected void onReset() {
        this.damageCauseMap.clear();
    }

    @Override
    protected void onReload() {
        final var config = MythicLib.plugin.getConfig();

        // Decimal formatting
        formatSymbols.setDecimalSeparator(getFirstChar(config.getString("number-format.decimal-separator")));
        decimal = newDecimalFormat("0.#");
        decimals = newDecimalFormat("0.##");

        // Combat
        interactionRules = config.getBoolean("interaction_rules.enabled") ? new InteractionRules(config.getConfigurationSection("interaction_rules")) : new EmptyPvPInteractionRules();

        // Other options
        playerAbilityDamage = config.getBoolean("player-ability-damage");
        naturalDefenseFormula = config.getString("defense-application.natural");
        elementalDefenseFormula = config.getString("defense-application.elemental");
        fixTooLargePackets = config.getBoolean("fix-too-large-packets");
        debugMode = config.getBoolean("debug");
        maxSyncTries = config.getInt("max-sync-tries", 7);
        ignoreShiftTriggers = config.getBoolean("ignore_shift_triggers");
        ignoreOffhandClickTriggers = config.getBoolean("ignore_offhand_click_triggers");
        skipElementalDamageApplication = config.getBoolean("skip_elemental_damage_application");

        flagCheckSkills = config.getBoolean("enable_flag_checks.skills");

        ///////////////////
        // Casting delay
        ///////////////////
        castingDelaySlowness = config.getDouble("casting-delay.slowness");
        castingDelayCancelOnMove = config.getBoolean("casting-delay.cancel-on-move");
        enableCastingDelayBossbar = config.getBoolean("casting-delay.bossbar.enabled");
        castingDelayBossbarFormat = config.getString("casting-delay.bossbar.format");
        castingDelayBarColor = UtilityMethods.resolveField(BarColor::valueOf, () -> BarColor.PURPLE, config.getString("casting-delay.bossbar.color", "PURPLE"));
        castingDelayBarStyle = UtilityMethods.resolveField(BarStyle::valueOf, () -> BarStyle.SEGMENTED_20, config.getString("casting-delay.bossbar.style", "SEGMENTED_20"));
        try {
            skillCastScript = config.getBoolean("casting-delay.cast-script.enabled") ?
                    MythicLib.plugin.getSkills().loadScript(config.get("casting-delay.cast-script.script")) : null;
        } catch (IllegalArgumentException exception) {
            skillCastScript = null;
        }
        try {
            skillCancelScript = config.getBoolean("casting-delay.cancel-script.enabled") ?
                    MythicLib.plugin.getSkills().loadScript(config.get("casting-delay.cancel-script.script")) : null;
        } catch (IllegalArgumentException exception) {
            skillCancelScript = null;
        }

        ///////////////////
        // Attack damage types
        ///////////////////
        try {
            meleeWeaponAttackTypes = DamageType.listFromConfig(config.getStringList("damage_types.default.melee_weapon"));
        } catch (Exception exception) {
            meleeWeaponAttackTypes = List.of(DamageType.PHYSICAL, DamageType.WEAPON);
        }

        try {
            meleeUnarmedAttackTypes = DamageType.listFromConfig(config.getStringList("damage_types.default.melee_unarmed"));
        } catch (Exception exception) {
            meleeUnarmedAttackTypes = List.of(DamageType.PHYSICAL, DamageType.UNARMED);
        }

        try {
            meleeRandomAttackTypes = DamageType.listFromConfig(config.getStringList("damage_types.default.melee_random"));
        } catch (Exception exception) {
            meleeRandomAttackTypes = List.of(DamageType.PHYSICAL);
        }

        try {
            bowAttackTypes = DamageType.listFromConfig(config.getStringList("damage_types.default.bow"));
        } catch (Exception exception) {
            bowAttackTypes = List.of(DamageType.PHYSICAL, DamageType.PROJECTILE, DamageType.WEAPON);
        }

        try {
            skillAttackTypes = DamageType.listFromConfig(config.getStringList("damage_types.default.skills"));
        } catch (Exception exception) {
            skillAttackTypes = List.of(DamageType.SKILL, DamageType.MAGIC);
        }

        ///////////////////
        // DamageCause -> DamageType mapping
        ///////////////////
        final var rawConfigFile = new YamlFile("config"); // Ignore default keys
        damageTypeConfigMigration(rawConfigFile);
        final var damageCauseSection = rawConfigFile.getContent().getConfigurationSection("damage_types.bukkit");
        if (damageCauseSection != null) for (var key : damageCauseSection.getKeys(false))
            try {
                var cause = UtilityMethods.prettyValueOf(EntityDamageEvent.DamageCause::valueOf, key, "No damage cause with ID " + key + " exists!");
                var types = DamageType.listFromConfig(damageCauseSection.getStringList(key));
                damageCauseMap.put(cause, types);
            } catch (Exception exception) {
                MythicLib.plugin.getLogger().log(Level.WARNING, "Could not load DamageCause/DamageType mapping " + key + ": " + exception.getMessage());
            }

        // Mana & Stamina
        this.manaRefreshRate = config.getInt("builtin_mana.refresh_rate");
        this.manaLoginRatio = config.getDouble("builtin_mana.login_ratio.mana") / 100d;
        this.staminaLoginRatio = config.getDouble("builtin_mana.login_ratio.stamina") / 100d;
        this.manaDisplay = new ResourceDisplayOptions(config.getConfigurationSection("builtin_mana.bar.mana"));
        this.staminaDisplay = new ResourceDisplayOptions(config.getConfigurationSection("builtin_mana.bar.stamina"));
    }

    /**
     * MMOCore and MMOItems mostly cache the return value of that method
     * in static fields for easy access, therefore a server restart is
     * required when editing the decimal-separator option in the ML config
     *
     * @param pattern Something like "0.#"
     * @return New decimal format with the decimal separator given in the MythicLib
     *         main plugin config.
     */
    public DecimalFormat newDecimalFormat(String pattern) {
        return new DecimalFormat(pattern, formatSymbols);
    }

    private char getFirstChar(String str) {
        return str == null || str.isEmpty() ? '.' : str.charAt(0);
    }

    @BackwardsCompatibility(version = "1.7.1-SNAPSHOT")
    private static void damageTypeConfigMigration(YamlFile config) {
        // Autoupdate config.yml
        if (!config.getContent().isConfigurationSection("damage_types")) {
            var freshConfig = YamlFile.fromJarFile(MythicLib.plugin, "", "config").getContent();

            config.getContent().set("damage_types", freshConfig.getConfigurationSection("damage_types"));
            config.save();
        }
    }
}
