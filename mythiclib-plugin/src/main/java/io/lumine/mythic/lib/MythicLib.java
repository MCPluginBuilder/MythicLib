package io.lumine.mythic.lib;

import com.google.gson.Gson;
import io.lumine.mythic.lib.api.crafting.recipes.MythicCraftingManager;
import io.lumine.mythic.lib.api.crafting.recipes.vmp.MegaWorkbenchMapping;
import io.lumine.mythic.lib.api.crafting.recipes.vmp.SuperWorkbenchMapping;
import io.lumine.mythic.lib.api.crafting.uifilters.MythicItemUIFilter;
import io.lumine.mythic.lib.api.event.armorequip.ArmorEquipEvent;
import io.lumine.mythic.lib.api.placeholders.MythicPlaceholders;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.command.HealthScaleCommand;
import io.lumine.mythic.lib.command.MMOTempStatCommand;
import io.lumine.mythic.lib.comp.FabledModule;
import io.lumine.mythic.lib.comp.McMMOModule;
import io.lumine.mythic.lib.comp.adventure.AdventureParser;
import io.lumine.mythic.lib.comp.anticheat.AntiCheatSupport;
import io.lumine.mythic.lib.comp.anticheat.SpartanPlugin;
import io.lumine.mythic.lib.comp.dualwield.DualWieldHook;
import io.lumine.mythic.lib.comp.dualwield.RealDualWieldHook;
import io.lumine.mythic.lib.comp.flags.FlagHandler;
import io.lumine.mythic.lib.comp.flags.FlagPlugin;
import io.lumine.mythic.lib.comp.flags.ResidenceFlags;
import io.lumine.mythic.lib.comp.flags.WorldGuardFlags;
import io.lumine.mythic.lib.comp.formula.FormulaParser;
import io.lumine.mythic.lib.comp.mythicmobs.MythicMobsAttackHandler;
import io.lumine.mythic.lib.comp.mythicmobs.MythicMobsHook;
import io.lumine.mythic.lib.comp.placeholder.*;
import io.lumine.mythic.lib.comp.profile.LegacyProfiles;
import io.lumine.mythic.lib.comp.profile.ProfileMode;
import io.lumine.mythic.lib.comp.protocollib.DamageParticleCap;
import io.lumine.mythic.lib.glow.GlowModule;
import io.lumine.mythic.lib.glow.provided.MythicGlowModule;
import io.lumine.mythic.lib.gui.PluginInventory;
import io.lumine.mythic.lib.hologram.HologramFactory;
import io.lumine.mythic.lib.hologram.HologramFactoryList;
import io.lumine.mythic.lib.hologram.factory.BukkitHologramFactory;
import io.lumine.mythic.lib.hologram.factory.LegacyBukkitHologramFactory;
import io.lumine.mythic.lib.listener.*;
import io.lumine.mythic.lib.listener.event.AttackEventListener;
import io.lumine.mythic.lib.listener.option.FixAttributeModifiers;
import io.lumine.mythic.lib.listener.option.FixMovementSpeed;
import io.lumine.mythic.lib.listener.option.HealthScale;
import io.lumine.mythic.lib.listener.option.VanillaDamageModifiers;
import io.lumine.mythic.lib.manager.*;
import io.lumine.mythic.lib.module.MMOPluginImpl;
import io.lumine.mythic.lib.util.MMOPlugin;
import io.lumine.mythic.lib.util.gson.MythicLibGson;
import io.lumine.mythic.lib.util.lang3.Validate;
import io.lumine.mythic.lib.util.loadingorder.DependencyCycleCheck;
import io.lumine.mythic.lib.util.loadingorder.DependencyNode;
import io.lumine.mythic.lib.util.network.MythicPacketSniffer;
import io.lumine.mythic.lib.version.ServerVersion;
import io.lumine.mythic.lib.version.SpigotPlugin;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;

public class MythicLib extends MMOPluginImpl {
    public static MythicLib plugin;

    private final DamageManager damageManager = new DamageManager(this);
    private final MythicLibCommandManager commandManager = new MythicLibCommandManager();
    private final EntityManager entityManager = new EntityManager();
    private final StatManager statManager = new StatManager();
    private final JsonManager jsonManager = new JsonManager();
    private final ConfigManager configManager = new ConfigManager(this);
    private final ElementManager elementManager = new ElementManager(this);
    private final SkillManager skillManager = new SkillManager(this);
    private final ModifierManager modifierManager = new ModifierManager();
    private final FlagHandler flagHandler = new FlagHandler();
    private final IndicatorManager indicatorManager = new IndicatorManager();
    private final FakeEventManager fakeEventManager = new FakeEventManager();
    private final List<MMOPlugin> mmoPlugins = new ArrayList<>();
    private Gson gson;
    private AntiCheatSupport antiCheatSupport;
    private ServerVersion version;
    private HologramFactory hologramFactory;
    private AttackEffects attackEffects;
    private MitigationMechanics mitigationMechanics;
    private AdventureParser adventureParser;
    private PlaceholderParser placeholderParser;
    private GlowModule glowModule;
    private @Nullable ProfileMode profileMode;

    @Override
    public void onLoad() {
        plugin = this;
        getLogger().log(Level.INFO, "Plugin file is called '" + getFile().getName() + "'");

        try {
            version = new ServerVersion();
            getLogger().log(Level.INFO, "Detected Bukkit Version: " + version.getCraftBukkitVersion());
        } catch (Exception exception) {
            getLogger().log(Level.WARNING, "Internal error:");
            exception.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            flagHandler.registerPlugin(new WorldGuardFlags());
            getLogger().log(Level.INFO, "Hooked onto WorldGuard");
        }

        adventureParser = new AdventureParser();
    }

    @Override
    public void onEnable() {
        new Metrics(this);
        gson = MythicLibGson.build();
        new SpigotPlugin(90306, this).checkForUpdate();
        saveDefaultConfig();

        final int configVersion = getConfig().contains("config-version", true) ? getConfig().getInt("config-version") : -1;
        final int defConfigVersion = getConfig().getDefaults().getInt("config-version");
        if (configVersion != defConfigVersion) {
            getLogger().warning("You may be using an outdated config.yml!");
            getLogger().warning("(Your config version: '" + configVersion + "' | Expected config version: '" + defConfigVersion + "')");
        }

        // Fixes left clicks
        new MythicPacketSniffer(this, version);

        // Hologram provider TODO remove
        Bukkit.getServicesManager().register(HologramFactory.class, new LegacyBukkitHologramFactory(), this, ServicePriority.Lowest);
        if (version.isAbove(1, 19, 4))
            Bukkit.getServicesManager().register(HologramFactory.class, new BukkitHologramFactory(), this, ServicePriority.Low);

        // Detect MMO plugins
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins())
            if (plugin instanceof MMOPlugin) mmoPlugins.add((MMOPlugin) plugin);

        // Register listeners
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        Bukkit.getPluginManager().registerEvents(damageManager, this);
        Bukkit.getPluginManager().registerEvents(new DamageReduction(), this);
        Bukkit.getPluginManager().registerEvents(attackEffects = new AttackEffects(), this);
        Bukkit.getPluginManager().registerEvents(new CustomProjectileDamage(), this);
        Bukkit.getPluginManager().registerEvents(mitigationMechanics = new MitigationMechanics(), this);
        Bukkit.getPluginManager().registerEvents(new AttackEventListener(), this);
        Bukkit.getPluginManager().registerEvents(new MythicCraftingManager(), this);
        Bukkit.getPluginManager().registerEvents(new SkillTriggers(), this);
        Bukkit.getPluginManager().registerEvents(new ElementalDamage(), this);
        Bukkit.getPluginManager().registerEvents(new PvpListener(), this);
        ArmorEquipEvent.registerListener(this);

        if (getConfig().getBoolean("vanilla-damage-modifiers.enabled"))
            Bukkit.getPluginManager().registerEvents(new VanillaDamageModifiers(getConfig().getConfigurationSection("vanilla-damage-modifiers")), this);

        if (getConfig().getBoolean("health-scale.enabled"))
            Bukkit.getPluginManager().registerEvents(new HealthScale(getConfig().getDouble("health-scale.scale"), getConfig().getInt("health-scale.delay", 0)), this);

        if (getConfig().getBoolean("fix-movement-speed"))
            Bukkit.getPluginManager().registerEvents(new FixMovementSpeed(), this);

        if (getConfig().getBoolean("fix_reset_attribute_modifiers.enabled")) try {
            new FixAttributeModifiers(getConfig().getConfigurationSection("fix_reset_attribute_modifiers"));
        } catch (Throwable exception) {
            getLogger().log(Level.WARNING, "Could not enable fix_reset_attribute_modifiers: " + exception.getMessage());
        }

        // Hologram provider
        try {
            final HologramFactoryList found = HologramFactoryList.valueOf(UtilityMethods.enumName(getConfig().getString("hologram-provider")));
            hologramFactory = found.provide();
            Bukkit.getServicesManager().register(HologramFactory.class, hologramFactory, this, ServicePriority.Normal); // Backwards compatibility
            getLogger().log(Level.INFO, "Hooked onto " + found.getName() + " (holograms)");
        } catch (Throwable throwable) {
            hologramFactory = HologramFactoryList.LEGACY_ARMOR_STANDS.provide();
            getLogger().log(Level.WARNING, "Could not hook onto hologram provider " + getConfig().getString("hologram-provider") + ", using default: " + throwable.getMessage());
        }

        if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
            damageManager.registerHandler(new MythicMobsAttackHandler());
            Bukkit.getPluginManager().registerEvents(new MythicMobsHook(), this);
            MythicItemUIFilter.register();
            getLogger().log(Level.INFO, "Hooked onto MythicMobs");
        }

        if (Bukkit.getPluginManager().getPlugin("Residence") != null) {
            flagHandler.registerPlugin(new ResidenceFlags());
            getLogger().log(Level.INFO, "Hooked onto Residence");
        }

        if (Bukkit.getPluginManager().getPlugin("Spartan") != null) {
            antiCheatSupport = new SpartanPlugin();
            getLogger().log(Level.INFO, "Hooked onto Spartan");
        }

        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
            if (getConfig().getBoolean("damage-particles-cap.enabled"))
                new DamageParticleCap(getConfig().getInt("damage-particles-cap.max-per-tick"));
            getLogger().log(Level.INFO, "Hooked onto ProtocolLib");
        }

        if (Bukkit.getPluginManager().getPlugin("mcMMO") != null) {
            new McMMOModule();
            getLogger().log(Level.INFO, "Hooked onto mcMMO");
        }

        if (Bukkit.getPluginManager().getPlugin("Fabled") != null) {
            new FabledModule();
            getLogger().log(Level.INFO, "Hooked onto Fabled");
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            MythicPlaceholders.registerPlaceholder(new MythicPlaceholderAPIHook());
            new PlaceholderAPIHook().register();
            placeholderParser = new PlaceholderAPIParser();
            getLogger().log(Level.INFO, "Hooked onto PlaceholderAPI");
        } else placeholderParser = new DefaultPlaceholderParser();

        if (Bukkit.getPluginManager().getPlugin("RealDualWield") != null) {
            Bukkit.getPluginManager().registerEvents(new RealDualWieldHook(), this);
            getLogger().log(Level.INFO, "Hooked onto RealDualWield");
        }

        if (Bukkit.getPluginManager().getPlugin("DualWield") != null) {
            Bukkit.getPluginManager().registerEvents(new DualWieldHook(), this);
            getLogger().log(Level.INFO, "Hooked onto DualWield");
        }

        // Look for plugin dependency cycles
        final Stack<DependencyNode> dependencyCycle = new DependencyCycleCheck().checkCycle();
        if (dependencyCycle != null) {
            getLogger().log(Level.WARNING, "Found a dependency cycle! Please make sure that the plugins involved load with no errors.");
            getLogger().log(Level.WARNING, "Plugin dependency cycle: " + dependencyCycle);
        }

        // Regen and damage indicators
        this.indicatorManager.load(getConfig());

//		if (Bukkit.getPluginManager().getPlugin("ShopKeepers") != null)
//			entityManager.registerHandler(new ShopKeepersEntityHandler());

        // Glowing module
        if (glowModule == null) {
            glowModule = new MythicGlowModule();
            glowModule.enable();
        }

        // Command executors
        getCommand("mmotempstat").setExecutor(new MMOTempStatCommand());
        getCommand("healthscale").setExecutor(new HealthScaleCommand());

        // Super workbench
        getCommand("superworkbench").setExecutor(SuperWorkbenchMapping.SWB);
        Bukkit.getPluginManager().registerEvents(SuperWorkbenchMapping.SWB, this);
        getCommand("megaworkbench").setExecutor(MegaWorkbenchMapping.MWB);
        Bukkit.getPluginManager().registerEvents(MegaWorkbenchMapping.MWB, this);

        damageManager.enable();

        // Loads commands
        commandManager.initialize(false);

        // Load local skills
        skillManager.initialize(false);

        // Load elements
        elementManager.reload(false);

        // Load player data of online players
        Bukkit.getOnlinePlayers().forEach(MMOPlayerData::setup);

        // Loop for flushing temporary player data
        Bukkit.getScheduler().runTaskTimer(this, MMOPlayerData::flushOfflinePlayerData, 20 * 60 * 60, 20 * 60 * 60);

        configManager.enable();
        statManager.initialize(false);
    }

    public void reload() {
        reloadConfig();
        statManager.initialize(true);
        attackEffects.reload();
        mitigationMechanics.reload();
        skillManager.initialize(true);
        configManager.reload();
        elementManager.reload(true);
        this.indicatorManager.reload(getConfig());

        // Flush outdated data
        for (MMOPlayerData online : MMOPlayerData.getLoaded())
            online.getStatMap().flushCache();
    }

    @Override
    public void onDisable() {
        //this.configuration.unload();
        UtilityMethods.closeOpenViewsOfType(PluginInventory.class);

        glowModule.disable();
    }

    public static MythicLib inst() {
        return plugin;
    }

    public Gson getGson() {
        return gson;
    }

    public ServerVersion getVersion() {
        return version;
    }

    @Deprecated
    public JsonManager getJson() {
        return jsonManager;
    }

    public FakeEventManager getFakeEvents() {
        return fakeEventManager;
    }

    @Deprecated
    public MythicLibCommandManager getCommand() {
        return getCommands();
    }

    public MythicLibCommandManager getCommands() {
        return commandManager;
    }

    public DamageManager getDamage() {
        return damageManager;
    }

    public EntityManager getEntities() {
        return entityManager;
    }

    public SkillManager getSkills() {
        return skillManager;
    }

    public ModifierManager getModifiers() {
        return modifierManager;
    }

    public ElementManager getElements() {
        return elementManager;
    }

    public StatManager getStats() {
        return statManager;
    }

    public ConfigManager getMMOConfig() {
        return configManager;
    }

    public FlagHandler getFlags() {
        return flagHandler;
    }

    public PlaceholderParser getPlaceholderParser() {
        return placeholderParser;
    }

    public AttackEffects getAttackEffects() {
        return attackEffects;
    }

    public AntiCheatSupport getAntiCheat() {
        return antiCheatSupport;
    }

    @Deprecated
    public FormulaParser getFormulaParser() {
        return FormulaParser.getInstance();
    }

    @Nullable
    public GlowModule getGlowing() {
        return glowModule;
    }

    /**
     * Enables support for legacy (spigot-based) MMOProfiles.
     */
    public void useLegacyProfiles() {
        Validate.isTrue(profileMode == null, "Profiles have already been enabled/disabled");
        profileMode = ProfileMode.LEGACY;

        Bukkit.getPluginManager().registerEvents(new LegacyProfiles(), this);
        getLogger().log(Level.INFO, "Hooked onto spigot-based ProfileAPI");
    }

    /**
     * Enables support for proxy-based MMOProfiles
     */
    public void useProxyProfiles() {
        Validate.isTrue(profileMode == null, "Profiles have already been enabled/disabled");
        profileMode = ProfileMode.PROXY;

        getLogger().log(Level.INFO, "Hooked onto proxy-based ProfileAPI");
    }

    public boolean hasProfiles() {
        return profileMode != null;
    }

    @Nullable
    public ProfileMode getProfileMode() {
        return profileMode;
    }

    @Deprecated
    public void handleFlags(FlagPlugin flagPlugin) {
        getFlags().registerPlugin(flagPlugin);
    }

    public boolean hasAntiCheat() {
        return antiCheatSupport != null;
    }

    /**
     * @param format The string to format
     * @return String with parsed (hex) color codes
     */
    public String parseColors(String format) {
        return adventureParser.parse(format);
    }

    public List<String> parseColors(String... format) {
        return parseColors(Arrays.asList(format));
    }

    public List<String> parseColors(List<String> format) {
        return new ArrayList<>(adventureParser.parse(format));
    }

    public AdventureParser getAdventureParser() {
        return adventureParser;
    }

    public File getJarFile() {
        return plugin.getFile();
    }

    @NotNull
    public List<MMOPlugin> getMMOPlugins() {
        return new ArrayList<>(mmoPlugins);
    }

    @NotNull
    public HologramFactory getHologramFactory() {
        return hologramFactory;
    }
}
