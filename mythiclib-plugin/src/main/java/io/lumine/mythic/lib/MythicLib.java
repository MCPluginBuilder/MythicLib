package io.lumine.mythic.lib;

import com.google.gson.Gson;
import io.lumine.mythic.lib.api.crafting.recipes.MythicCraftingManager;
import io.lumine.mythic.lib.api.crafting.recipes.vmp.MegaWorkbenchMapping;
import io.lumine.mythic.lib.api.crafting.recipes.vmp.SuperWorkbenchMapping;
import io.lumine.mythic.lib.api.crafting.uifilters.MythicItemUIFilter;
import io.lumine.mythic.lib.api.event.armorequip.ArmorEquipEvent;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.command.mythiclib.HealthScaleCommand;
import io.lumine.mythic.lib.command.mythiclib.MMOTempStatCommand;
import io.lumine.mythic.lib.command.mythiclib.MythicLibCommand;
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
import io.lumine.mythic.lib.comp.placeholder.DefaultPlaceholderParser;
import io.lumine.mythic.lib.comp.placeholder.PlaceholderAPIHook;
import io.lumine.mythic.lib.comp.placeholder.PlaceholderAPIParser;
import io.lumine.mythic.lib.comp.placeholder.PlaceholderParser;
import io.lumine.mythic.lib.comp.profile.ProfileMode;
import io.lumine.mythic.lib.comp.protocollib.DamageParticleCap;
import io.lumine.mythic.lib.damage.indicator.DamageIndicators;
import io.lumine.mythic.lib.damage.mitigation.MitigationModule;
import io.lumine.mythic.lib.damage.onhit.OnHitModule;
import io.lumine.mythic.lib.data.SynchronizedDataManager;
import io.lumine.mythic.lib.glow.GlowModule;
import io.lumine.mythic.lib.glow.provided.MythicGlowModule;
import io.lumine.mythic.lib.gui.PluginInventory;
import io.lumine.mythic.lib.hologram.HologramFactory;
import io.lumine.mythic.lib.hologram.HologramFactoryList;
import io.lumine.mythic.lib.listener.*;
import io.lumine.mythic.lib.listener.event.AttackEventListener;
import io.lumine.mythic.lib.listener.option.*;
import io.lumine.mythic.lib.manager.*;
import io.lumine.mythic.lib.module.MMOPlugin;
import io.lumine.mythic.lib.profile.handler.ProfileHandler;
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
import java.util.*;
import java.util.logging.Level;

public class MythicLib extends MMOPlugin {
    public static MythicLib plugin;

    private final DamageManager damageManager = new DamageManager(this);
    private final EntityManager entityManager = new EntityManager(this);
    private final StatManager statManager = new StatManager(this);
    private final ConfigManager configManager = new ConfigManager(this);
    private final ElementManager elementManager = new ElementManager(this);
    private final SkillManager skillManager = new SkillManager(this);
    private final FlagHandler flagHandler = new FlagHandler();
    private final DamageIndicators damageIndicators = new DamageIndicators(this);
    private final RegenIndicators regenIndicators = new RegenIndicators(this);
    private final MitigationModule mitigationModule = new MitigationModule(this);
    private final OnHitModule onHitModule = new OnHitModule(this);
    private final FakeEventManager fakeEventManager = new FakeEventManager();
    private final List<MMOPlugin> mmoPlugins = new ArrayList<>();
    private Gson gson;
    private AntiCheatSupport antiCheatSupport;
    private ServerVersion version;
    private HologramFactory hologramFactory;
    private AdventureParser adventureParser;
    private PlaceholderParser placeholderParser;
    private GlowModule glowModule;
    private @Nullable ProfileMode profileMode;
    private @Nullable ProfileHandler profileHandler;

    @Override
    public void onLoad() {
        plugin = this;
        getLogger().log(Level.INFO, "Plugin file is called '" + getFile().getName() + "'");

        try {
            version = new ServerVersion();
            version.validateMappings(); // After field is initialized
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

        // Fixes left clicks
        new MythicPacketSniffer(this, version);

        // Detect MMO plugins
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins())
            if (plugin instanceof MMOPlugin) mmoPlugins.add((MMOPlugin) plugin);

        // Register listeners
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        Bukkit.getPluginManager().registerEvents(new DamageReduction(), this);
        Bukkit.getPluginManager().registerEvents(new LegacyAttackEffects(), this);
        Bukkit.getPluginManager().registerEvents(new CustomProjectileDamage(), this);
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

        if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null) try {
            damageManager.registerHandler(new MythicMobsAttackHandler());
            Bukkit.getPluginManager().registerEvents(new MythicMobsHook(), this);
            MythicItemUIFilter.register();
            getLogger().log(Level.INFO, "Hooked onto MythicMobs");
        } catch (Throwable throwable) {
            getLogger().log(Level.INFO, "Could not hook onto MythicMobs: " + throwable.getMessage());
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
            //MythicPlaceholders.registerPlaceholder(new MythicPlaceholderAPIHook());
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

        // Initialize profile handler
        initializeProfiles();

        // Look for plugin dependency cycles
        final Stack<DependencyNode> dependencyCycle = new DependencyCycleCheck().checkCycle();
        if (dependencyCycle != null) {
            getLogger().log(Level.WARNING, "Found a dependency cycle! Please make sure that the plugins involved load with no errors.");
            getLogger().log(Level.WARNING, "Plugin dependency cycle: " + dependencyCycle);
        }

//		if (Bukkit.getPluginManager().getPlugin("ShopKeepers") != null)
//			entityManager.registerHandler(new ShopKeepersEntityHandler());

        // Glowing module
        if (glowModule == null) {
            glowModule = new MythicGlowModule();
            glowModule.enable();
        }

        // Main command
        final var mythicLibCommand = new MythicLibCommand();
        getCommand("mythiclib").setExecutor(mythicLibCommand);
        getCommand("mythiclib").setTabCompleter(mythicLibCommand);

        // Other commands
        getCommand("mmotempstat").setExecutor(new MMOTempStatCommand());
        getCommand("healthscale").setExecutor(new HealthScaleCommand());

        // Super workbench
        getCommand("superworkbench").setExecutor(SuperWorkbenchMapping.SWB);
        Bukkit.getPluginManager().registerEvents(SuperWorkbenchMapping.SWB, this);
        getCommand("megaworkbench").setExecutor(MegaWorkbenchMapping.MWB);
        Bukkit.getPluginManager().registerEvents(MegaWorkbenchMapping.MWB, this);

        damageManager.reload();
        skillManager.reload(); // Before elements are loaded
        elementManager.reload(); // Before stats are loaded
        mitigationModule.reload(); // After scripts
        onHitModule.reload(); // After scripts
        damageIndicators.reload();
        regenIndicators.reload();
        configManager.reload(); // TODO why so late?
        statManager.reload(); // TODO why so late?

        // Load player data of online players
        Bukkit.getOnlinePlayers().forEach(MMOPlayerData::setup);

        // Periodically flush temporary player data (1 hour)
        Bukkit.getScheduler().runTaskTimer(this, MMOPlayerData::flushOfflinePlayerData, 20 * 60 * 60, 20 * 60 * 60);

        // Loop for applying permanent potion effects
        Bukkit.getScheduler().runTaskTimer(plugin, () -> MMOPlayerData.forEachPlaying(MMOPlayerData::tickOnline), 100, 20);
    }

    public void reload() {
        reloadConfig();
        statManager.reload();
        skillManager.reload();
        configManager.reload();
        elementManager.reload();
        mitigationModule.reload();
        onHitModule.reload();
        damageIndicators.reload();
        regenIndicators.reload();

        // Flush outdated data
        for (var online : MMOPlayerData.getLoaded()) online.getStatMap().flushCache();
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

    public DamageIndicators getDamageIndicators() {
        return damageIndicators;
    }

    public RegenIndicators getRegenIndicators() {
        return regenIndicators;
    }

    @Deprecated
    public JsonManager getJson() {
        return new JsonManager();
    }

    @Deprecated
    public SkillModifierManager getModifiers() {
        return new SkillModifierManager();
    }

    public FakeEventManager getFakeEvents() {
        return fakeEventManager;
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

    @NotNull
    public MitigationModule getMitigation() {
        return mitigationModule;
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

    //region Profile mode

    private void validateNoProfileMode() {
        Validate.isTrue(profileMode == null, "Profiles have already been enabled/disabled");
    }

    /**
     * Enables support for legacy (spigot-based) MMOProfiles.
     */
    public void useLegacyProfiles() {
        validateNoProfileMode();

        this.profileMode = ProfileMode.LEGACY;
        getLogger().log(Level.INFO, "Hooked onto classic ProfileAPI");
    }

    public void useNoProfiles() {
        validateNoProfileMode();

        this.profileMode = ProfileMode.NONE;
        // No console log if no profile plugin installed
    }

    /**
     * Enables support for proxy-based MMOProfiles
     */
    public void useProxyProfiles() {
        validateNoProfileMode();

        this.profileMode = ProfileMode.PROXY;
        getLogger().log(Level.INFO, "Hooked onto proxy-based ProfileAPI");
    }

    private void initializeProfiles() {

        // Disable profiles altogether
        if (profileMode == null) useNoProfiles();

        Validate.notNull(profileMode, "Internal error with profile mode");
        Bukkit.getPluginManager().registerEvents(this.profileHandler = this.profileMode.newProfileHandler(), this);
    }

    public boolean hasProfiles() {
        return getProfileMode() != ProfileMode.NONE;
    }

    @NotNull
    public ProfileMode getProfileMode() {
        return Objects.requireNonNull(profileMode, "No profile mode");
    }

    @NotNull
    public ProfileHandler getProfileHandler() {
        return Objects.requireNonNull(profileHandler, "No profile handler");
    }

    //endregion

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

    @Override
    public @NotNull SynchronizedDataManager<?, ?> getRawPlayerDataManager() {
        throw new IllegalArgumentException("No player data manager for MythicLib");
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
