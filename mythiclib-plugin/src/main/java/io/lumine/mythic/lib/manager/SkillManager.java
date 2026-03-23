package io.lumine.mythic.lib.manager;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.module.MMOPlugin;
import io.lumine.mythic.lib.module.Module;
import io.lumine.mythic.lib.module.ModuleInfo;
import io.lumine.mythic.lib.script.Script;
import io.lumine.mythic.lib.script.condition.Condition;
import io.lumine.mythic.lib.script.condition.generic.*;
import io.lumine.mythic.lib.script.condition.location.BiomeCondition;
import io.lumine.mythic.lib.script.condition.location.CuboidCondition;
import io.lumine.mythic.lib.script.condition.location.DistanceCondition;
import io.lumine.mythic.lib.script.condition.location.WorldCondition;
import io.lumine.mythic.lib.script.condition.misc.*;
import io.lumine.mythic.lib.script.mechanic.Mechanic;
import io.lumine.mythic.lib.script.mechanic.buff.FeedMechanic;
import io.lumine.mythic.lib.script.mechanic.buff.HealMechanic;
import io.lumine.mythic.lib.script.mechanic.buff.ReduceCooldownMechanic;
import io.lumine.mythic.lib.script.mechanic.buff.SaturateMechanic;
import io.lumine.mythic.lib.script.mechanic.buff.stat.AddStatModifierMechanic;
import io.lumine.mythic.lib.script.mechanic.buff.stat.RemoveStatModifierMechanic;
import io.lumine.mythic.lib.script.mechanic.gui.CloseInventoryMechanic;
import io.lumine.mythic.lib.script.mechanic.gui.GoBackMechanic;
import io.lumine.mythic.lib.script.mechanic.misc.*;
import io.lumine.mythic.lib.script.mechanic.movement.TeleportMechanic;
import io.lumine.mythic.lib.script.mechanic.movement.VelocityMechanic;
import io.lumine.mythic.lib.script.mechanic.offense.*;
import io.lumine.mythic.lib.script.mechanic.player.GiveItemMechanic;
import io.lumine.mythic.lib.script.mechanic.player.KickMechanic;
import io.lumine.mythic.lib.script.mechanic.player.SudoMechanic;
import io.lumine.mythic.lib.script.mechanic.projectile.ShootArrowMechanic;
import io.lumine.mythic.lib.script.mechanic.projectile.ShulkerBulletMechanic;
import io.lumine.mythic.lib.script.mechanic.shaped.*;
import io.lumine.mythic.lib.script.mechanic.variable.*;
import io.lumine.mythic.lib.script.mechanic.variable.vector.*;
import io.lumine.mythic.lib.script.mechanic.visual.*;
import io.lumine.mythic.lib.script.targeter.EntityTargeter;
import io.lumine.mythic.lib.script.targeter.LocationTargeter;
import io.lumine.mythic.lib.script.targeter.entity.*;
import io.lumine.mythic.lib.script.targeter.location.*;
import io.lumine.mythic.lib.script.targeter.location.LookingAtTargeter;
import io.lumine.mythic.lib.skill.handler.*;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import io.lumine.mythic.lib.util.FileUtils;
import io.lumine.mythic.lib.util.PostLoadException;
import io.lumine.mythic.lib.util.SkillUpdateMigration;
import io.lumine.mythic.lib.util.annotation.BackwardsCompatibility;
import io.lumine.mythic.lib.util.config.YamlFile;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.jar.JarFile;
import java.util.logging.Level;

/**
 * The next step for MMO/Mythic abilities is to merge all the
 * different abilities of MMOItems and MMOCore. This will allow
 * us not to implement twice the same skill in the two plugins
 * which will be a gain of time.
 * <p>
 * The second thing is to make MythicLib a database combining:
 * - default MMOItems/MMOCore skills
 * - custom skills made using MythicMobs
 * - custom skills made using Fabled
 * - custom skills made using MythicLib
 * <p>
 * Then users can "register" any of these base skills inside MMOItems
 * or MMOCore by adding one specific YAML to the "/skill" folder.
 *
 * @author jules
 */
@ModuleInfo(key = "skills")
public class SkillManager extends Module {
    private final Map<String, Function<ConfigObject, Mechanic>> mechanics = new HashMap<>();
    private final Map<String, Function<ConfigObject, Condition>> conditions = new HashMap<>();
    private final Map<String, Function<ConfigObject, EntityTargeter>> entityTargets = new HashMap<>();
    private final Map<String, Function<ConfigObject, LocationTargeter>> locationTargets = new HashMap<>();

    /**
     * Registered custom scripts. In fact they have as much information
     * as a skill handler but they are not yet a skill handler
     */
    private final Map<String, Script> scripts = new HashMap<>();

    /**
     * All registered skill handlers accessible by any external plugins. This uncludes:
     * - default skill handlers from both MI and MMOCore (found in /skill/handler/def)
     * - custom MM skill handlers
     * - custom Fabled skill handlers
     * - custom ML skill handlers
     */
    private final Map<String, SkillHandler<?>> handlers = new HashMap<>();

    private final Map<String, Class<? extends SkillHandler<?>>> builtInSkillHandlerTypes = new HashMap<>();
    private final Map<String, SkillHandlerSource> skillHandlerSources = new HashMap<>();

    private boolean registration = true;

    public SkillManager(MMOPlugin plugin) {
        super(plugin);

        loadBuiltinObjects();
    }

    public void registerSkillHandlerSource(@NotNull SkillHandlerSource skillHandlerSource) {
        Validate.notNull(skillHandlerSource, "Skill handler type cannot be null");

        Validate.isTrue(skillHandlerSources.put(skillHandlerSource.getKey(), skillHandlerSource) == null, "A skill source with the same ID already exists");
    }

    //region Loading from config files

    @NotNull
    private SkillHandler<?> loadBuiltinSkillHandler(@NotNull ConfigurationSection config, @NotNull String builtinId) {
        final var mapping = builtInSkillHandlerTypes.get(UtilityMethods.enumName(builtinId));
        Validate.notNull(mapping, "Could not find builtin skill with ID '" + builtinId + "'");
        try {
            return mapping.getDeclaredConstructor(ConfigurationSection.class).newInstance(config);
        } catch (Exception exception) {
            throw new RuntimeException("Could not instantiate builtin skill handler '" + builtinId + "': " + exception.getMessage(), exception);
        }
    }

    private static final String UNIDENTIFIED_SCRIPT_ID = "UnidentifiedScript";

    @NotNull
    public SkillHandler<?> loadSkillHandler(Object configObject) {
        return loadSkillHandler(UNIDENTIFIED_SCRIPT_ID, configObject);
    }

    @NotNull
    public SkillHandler<?> loadSkillHandler(@Nullable String fallbackSkillHandlerId, @NotNull Object configObject) {
        Validate.notNull(configObject, "Input cannot be null");

        // Handler name
        if (configObject instanceof String) {
            final var asString = (String) configObject;

            // Support format 'source:InternalScriptName'
            if (asString.contains(":")) {
                final var asSplit = asString.split(":", 2);
                final var handlerSourceKey = asSplit[0];
                final var skillHandlerSource = this.skillHandlerSources.get(handlerSourceKey);
                Validate.notNull(skillHandlerSource, "Could not find skill source '" + handlerSourceKey + "'");

                final var emulatedConfigParent = new YamlConfiguration();
                final var config = emulatedConfigParent.createSection(UNIDENTIFIED_SCRIPT_ID);
                config.set("source", asSplit);
                return skillHandlerSource.getConstructor().apply(config, asSplit[1]);
            }

            return getHandlerOrThrow(UtilityMethods.enumName((String) configObject));
        }

        // Configuration Section
        if (configObject instanceof ConfigurationSection) {
            final var config = (ConfigurationSection) configObject;
            final var source = config.getString("source");

            // [Backwards compatibility]
            if (source == null) {
                final var legacySkillHandler = findLegacySkillSource(config);
                if (legacySkillHandler != null) return legacySkillHandler;
                throw new IllegalArgumentException("Could not find skill source");
            }

            Validate.isTrue(source.contains(":"), "Source must be in the format 'source:InternalSkillName'");
            final var asSplit = source.split(":", 2);
            final var handlerSourceKey = asSplit[0];
            final var skillHandlerSource = this.skillHandlerSources.get(handlerSourceKey);
            Validate.notNull(skillHandlerSource, "Could not find skill source '" + handlerSourceKey + "'");
            return skillHandlerSource.getConstructor().apply(config, asSplit[1]);
        }

        // List of mechanics => load MythicLib script
        if (configObject instanceof List) {
            return new MythicLibSkillHandler(loadScript(fallbackSkillHandlerId, configObject));
        }

        throw new IllegalArgumentException("Provide either a string or configuration section instead of " + configObject.getClass().getSimpleName());
    }

    @Nullable
    @BackwardsCompatibility(version = "1.7.1-SNAPSHOT")
    private SkillHandler<?> findLegacySkillSource(@NotNull ConfigurationSection config) {
        for (var skillSource : this.skillHandlerSources.values())
            for (var legacyPath : skillSource.getLegacyInternalSkillPaths()) {
                final var skillId = config.getString(legacyPath);
                if (skillId != null) return skillSource.getConstructor().apply(config, skillId);
            }
        return null;
    }

    @NotNull
    public SkillHandler<?> getHandlerOrThrow(@NotNull String handlerId) {
        return Objects.requireNonNull(handlers.get(handlerId), "Could not find skill with ID '" + handlerId + "'");
    }

    @Nullable
    public SkillHandler<?> getHandler(@NotNull String handlerId) {
        return handlers.get(handlerId);
    }

    @NotNull
    public Script loadScript(Object genericInput) {
        return loadScript(UNIDENTIFIED_SCRIPT_ID, genericInput);
    }

    @NotNull
    public Script loadScript(@Nullable String fallbackScriptId, @NotNull Object genericInput) {
        Validate.notNull(genericInput, "Input cannot be null");

        if (genericInput instanceof String) return getScriptOrThrow(genericInput.toString());

        if (genericInput instanceof ConfigurationSection) {
            Script skill = new Script((ConfigurationSection) genericInput);
            skill.getPostLoadAction().performAction();
            return skill;
        }

        // Adapt a list to a config section
        if (genericInput instanceof List) {
            Validate.notNull(fallbackScriptId, "Cannot use unidentified script here");
            @SuppressWarnings("unchecked") final var skill = new Script(fallbackScriptId, (List<String>) genericInput);
            skill.getPostLoadAction().performAction();
            return skill;
        }

        throw new IllegalArgumentException("Expected a string, config section or list");
    }

    @NotNull
    public Script getScriptOrThrow(@NotNull String scriptName) {
        return Objects.requireNonNull(scripts.get(scriptName), "Could not find script with name '" + scriptName + "'");
    }

    @Nullable
    public Script getScript(@NotNull String scriptName) {
        return scripts.get(scriptName);
    }

    //region Script objects

    @NotNull
    private String findEffectiveObjectType(String objectType, ConfigObject config) {
        if (config.contains("type")) return config.getString("type");
        else if (config.hasKey()) return config.getKey();
        else throw new IllegalArgumentException("Could not find " + objectType + " type");
    }

    @NotNull
    public Condition loadCondition(ConfigObject config) {
        final String key = findEffectiveObjectType("condition", config);
        final Function<ConfigObject, Condition> supplier = conditions.get(key);
        Validate.notNull(supplier, "Could not match condition to '" + key + "'");
        return supplier.apply(config);
    }

    @NotNull
    public Mechanic loadMechanic(ConfigObject config) {
        final String key = findEffectiveObjectType("mechanic", config);
        final Function<ConfigObject, Mechanic> supplier = mechanics.get(key);
        Validate.notNull(supplier, "Could not match mechanic to '" + key + "'");
        return supplier.apply(config);
    }

    @NotNull
    public LocationTargeter loadLocationTargeter(ConfigObject config) {
        final String key = findEffectiveObjectType("targeter", config);
        final Function<ConfigObject, LocationTargeter> supplier = locationTargets.get(key);
        Validate.notNull(supplier, "Could not match targeter to '" + key + "'");
        return supplier.apply(config);
    }

    @NotNull
    public EntityTargeter loadEntityTargeter(ConfigObject config) {
        final String key = findEffectiveObjectType("targeter", config);
        final Function<ConfigObject, EntityTargeter> supplier = entityTargets.get(key);
        Validate.notNull(supplier, "Could not match targeter to '" + key + "'");
        return supplier.apply(config);
    }

    //endregion

    //endregion

    public void registerSkillHandler(SkillHandler<?> handler) {
        Validate.isTrue(handlers.putIfAbsent(handler.getId(), handler) == null, "A skill with the same name already exists");

        if (!registration && handler instanceof Listener)
            Bukkit.getPluginManager().registerEvents((Listener) handler, MythicLib.plugin);
    }

    /**
     * @return Currently registered skill handlers.
     */
    public Collection<SkillHandler<?>> getHandlers() {
        return handlers.values();
    }

    public void registerScript(@NotNull Script script) {
        Validate.isTrue(!scripts.containsKey(script.getId()), "A script with the same name already exists");
        scripts.put(script.getId(), script);
    }

    @NotNull
    public Collection<Script> getScripts() {
        return scripts.values();
    }

    public void registerCondition(String name, Function<ConfigObject, Condition> condition, String... aliases) {
        Validate.isTrue(registration, "Condition registration is disabled");
        Validate.isTrue(!conditions.containsKey(name), "A condition with the same name already exists");
        Validate.notNull(condition, "Function cannot be null");

        conditions.put(name, condition);

        for (String alias : aliases)
            registerCondition(alias, condition);
    }

    public void registerMechanic(@NotNull String name, @NotNull Function<ConfigObject, Mechanic> mechanic, String... aliases) {
        Validate.isTrue(registration, "Mechanic registration is disabled");
        Validate.isTrue(!mechanics.containsKey(name), "A mechanic with the name '" + name + "' already exists");
        Validate.notNull(mechanic, "Function cannot be null");

        mechanics.put(name, mechanic);

        for (String alias : aliases)
            registerMechanic(alias, mechanic);
    }

    public void registerEntityTargeter(String name, Function<ConfigObject, EntityTargeter> entityTarget) {
        Validate.isTrue(registration, "Targeter registration is disabled");
        Validate.isTrue(!entityTargets.containsKey(name), "A targeter with the same name already exists");
        Validate.notNull(entityTarget, "Function cannot be null");

        entityTargets.put(name, entityTarget);
    }

    public void registerLocationTargeter(String name, Function<ConfigObject, LocationTargeter> locationTarget) {
        Validate.isTrue(registration, "Targeter registration is disabled");
        Validate.isTrue(!locationTargets.containsKey(name), "A targeter with the same name already exists");
        Validate.notNull(locationTarget, "Function cannot be null");

        locationTargets.put(name, locationTarget);
    }

    //region Built-In Objects

    private void loadBuiltinObjects() {

        //////////////////////////////////
        // Mechanics
        //////////////////////////////////

        // Buffs
        registerMechanic("add_stat", AddStatModifierMechanic::new);
        registerMechanic("remove_stat", RemoveStatModifierMechanic::new);
        registerMechanic("feed", FeedMechanic::new);
        registerMechanic("heal", HealMechanic::new);
        registerMechanic("reduce_cooldown", ReduceCooldownMechanic::new, "reduce_cd", "decrease_cooldown", "decrease_cd");
        registerMechanic("saturate", SaturateMechanic::new);

        // Misc
        registerMechanic("apply_cooldown", ApplyCooldownMechanic::new, "apply_cd");
        registerMechanic("call_trigger", CallTriggerMechanic::new);
        registerMechanic("cancel_event", CancelEventMechanic::new, "cancelevent");
        registerMechanic("consume_ammo", ConsumeAmmoMechanic::new, "take_ammo");
        registerMechanic("delay", DelayMechanic::new);
        registerMechanic("dispatch_command", DispatchCommandMechanic::new, "c", "dispatch_cmd", "cmd", "command", "execute_command", "execute_cmd", "run_command", "run_cmd");
        registerMechanic("entity_effect", EntityEffectMechanic::new);
        registerMechanic("lightning", LightningStrikeMechanic::new);
        registerMechanic("script", ScriptMechanic::new, "skill", "cast");

        // Inventory
        registerMechanic("close_inventory", config -> new CloseInventoryMechanic(), "close_inv");
        registerMechanic("go_back", config -> new GoBackMechanic(), "goback");

        // Move
        registerMechanic("teleport", TeleportMechanic::new, "tp", "set_position", "set_pos", "setpos", "setposition", "set_location", "setlocation", "set_loc", "setloc", "move", "moveto", "move_to");
        registerMechanic("set_velocity", VelocityMechanic::new, "setvel", "set_vel", "setvelocity");

        // Offense
        registerMechanic("additive_damage_buff", AdditiveDamageBuffMechanic::new);
        registerMechanic("damage", DamageMechanic::new, "deal_damage", "dmg", "deal_dmg", "dealdamage", "dealdmg", "attack", "atk");
        registerMechanic("mark_crit", MarkCritMechanic::new, "mark_as_crit");
        registerMechanic("multiply_damage", MultiplyDamageMechanic::new);
        registerMechanic("potion", PotionMechanic::new, "peffect", "potion_effect", "p_effect", "apply_potion", "apply_potion_effect", "apply_peffect");
        registerMechanic("remove_potion", RemovePotionMechanic::new);
        registerMechanic("set_no_damage_ticks", SetNoDamageTicksMechanic::new, "no_damage_ticks", "set_no_damage", "setnodamage", "nodamageticks");
        registerMechanic("set_on_fire", SetOnFireMechanic::new);

        // Player
        registerMechanic("give_item", GiveItemMechanic::new);
        registerMechanic("kick_player", KickMechanic::new, "kick", "kickplayer");
        registerMechanic("sudo", SudoMechanic::new);

        // Projectile
        registerMechanic("shoot_arrow", ShootArrowMechanic::new, "fire_arrow", "bowshoot", "bow_shoot", "shoot_bow");
        registerMechanic("shulker_bullet", ShulkerBulletMechanic::new);

        // Shaped
        registerMechanic("draw_helix", HelixMechanic::new, "helix");
        registerMechanic("draw_line", LineMechanic::new, "line");
        registerMechanic("draw_parabola", ParabolaMechanic::new, "parabola", "spawn_parabola");
        registerMechanic("projectile", ProjectileMechanic::new);
        registerMechanic("raytrace_blocks", RayTraceBlocksMechanic::new);
        registerMechanic("raytrace_entities", RayTraceEntitiesMechanic::new);
        registerMechanic("ray_trace", RayTraceMechanic::new, "cast_ray", "raytrace", "ray_cast", "raycast");
        registerMechanic("slash", SlashMechanic::new);
        registerMechanic("draw_sphere", SphereMechanic::new, "sphere");

        // Variables
        registerMechanic("add_vector", AddVectorMechanic::new, "add_vec");
        registerMechanic("cross_product", CrossProductMechanic::new);
        registerMechanic("dot_product", DotProductMechanic::new);
        registerMechanic("hadamard_product", HadamardProductMechanic::new);
        registerMechanic("multiply_vector", MultiplyVectorMechanic::new);
        registerMechanic("normalize_vector", NormalizeVectorMechanic::new, "normalize");
        registerMechanic("orient_vector", OrientVectorMechanic::new, "orient_vec");
        registerMechanic("save_vector", CopyVectorMechanic::new, "save_vec", "copy_vec", "copy_vector");
        registerMechanic("set_x", SetXMechanic::new);
        registerMechanic("set_y", SetYMechanic::new);
        registerMechanic("set_z", SetZMechanic::new);
        registerMechanic("subtract_vector", SubtractVectorMechanic::new, "sub_vec", "sub_vector", "subvec");

        registerMechanic("increment", IncrementMechanic::new, "incr");
        registerMechanic("set_boolean", SetBooleanMechanic::new, "set_bool");
        registerMechanic("set_double", SetDoubleMechanic::new, "set_float");
        registerMechanic("set_integer", SetIntegerMechanic::new, "set_int");
        registerMechanic("set_string", SetStringMechanic::new, "set_str");
        registerMechanic("set_vector", SetVectorMechanic::new, "set_vec");

        // Visual
        registerMechanic("action_bar", ActionBarMechanic::new, "actionbar", "ab");
        registerMechanic("spawn_particle", ParticleMechanic::new, "particle", "par", "spawnparticle");
        registerMechanic("sound", SoundMechanic::new, "play_world_sound", "play_sound", "world_sound");
        registerMechanic("player_sound", PlayerSoundMechanic::new, "play_player_sound", "playersound");
        registerMechanic("send_message", TellMechanic::new, "message", "msg", "send", "tell", "send_msg");

        //////////////////////////////////
        // Targeters
        //////////////////////////////////

        registerEntityTargeter("caster", config -> new CasterTargeter());
        registerEntityTargeter("cone", ConeTargeter::new);
        registerEntityTargeter("nearby_entities", NearbyEntitiesTargeter::new);
        registerEntityTargeter("nearest_entity", NearestEntityTargeter::new);
        registerEntityTargeter("target", config -> new TargetTargeter());
        registerEntityTargeter("variable", VariableEntityTargeter::new);
        registerEntityTargeter("looking_at", io.lumine.mythic.lib.script.targeter.entity.LookingAtTargeter::new);

        registerLocationTargeter("caster", CasterLocationTargeter::new);
        registerLocationTargeter("circle", CircleLocationTargeter::new);
        registerLocationTargeter("custom", CustomLocationTargeter::new);
        registerLocationTargeter("looking_at", LookingAtTargeter::new);
        registerLocationTargeter("source_location", config -> new SourceLocationTargeter());
        registerLocationTargeter("target", TargetEntityLocationTargeter::new);
        registerLocationTargeter("target_location", config -> new TargetLocationTargeter());
        registerLocationTargeter("variable", VariableLocationTargeter::new);

        //////////////////////////////////
        // Conditions
        //////////////////////////////////

        registerCondition("boolean", BooleanCondition::new, "bool", "generic");
        registerCondition("compare", CompareCondition::new);
        registerCondition("has_variable", HasVariableCondition::new, "has_var", "variable_exists", "var_exists");
        registerCondition("in_between", InBetweenCondition::new);
        registerCondition("string_equals", StringEqualsCondition::new, "string_equal", "str_eq");
        registerCondition("string_contains", StringContainsCondition::new, "string_contain", "str_contain", "str_in", "string_in");

        registerCondition("biome", BiomeCondition::new);
        registerCondition("cuboid", CuboidCondition::new);
        registerCondition("distance", DistanceCondition::new);
        registerCondition("world", WorldCondition::new);

        registerCondition("can_target", CanTargetCondition::new, "can_tgt", "cantarget", "ctgt");
        registerCondition("cooldown", CooldownCondition::new);
        registerCondition("food", FoodCondition::new);
        registerCondition("ammo", HasAmmoCondition::new);
        registerCondition("has_damage_type", HasDamageTypeCondition::new);
        registerCondition("is_living", IsLivingCondition::new);
        registerCondition("on_fire", OnFireCondition::new);
        registerCondition("permission", PermissionCondition::new);
        registerCondition("random_chance", RandomChanceCondition::new, "roll_chance", "chance_roll", "randomchance", "chance", "rollchance");
        registerCondition("time", TimeCondition::new);

        //////////////////////////////////
        // Skill handler types
        //////////////////////////////////

        registerSkillHandlerSource(new SkillHandlerSource("default", this::loadBuiltinSkillHandler));
        registerSkillHandlerSource(new SkillHandlerSource("mythiclib", MythicLibSkillHandler::new, List.of("mythiclib-skill-id")));
    }

    @SuppressWarnings("unchecked")
    private void loadBuiltinSkillHandlerTypes() {

        try {
            final var file = new JarFile(MythicLib.plugin.getJarFile());
            for (var enu = file.entries(); enu.hasMoreElements(); ) {
                String name = enu.nextElement().getName().replace("/", ".");
                if (!name.contains("$") && name.endsWith(".class") && name.startsWith("io.lumine.mythic.lib.skill.handler.def.")) {
                    final var clazz = Class.forName(name.substring(0, name.length() - 6));

                    final var annot = clazz.getAnnotation(BuiltinSkillHandler.class);
                    Validate.notNull(annot, "No BuiltinSkillHandler annotation on class " + clazz.getName());
                    registerBuiltinSkillHandlerSource((Class<? extends SkillHandler<?>>) clazz);
                }
            }
            file.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Register a new built-in skill handler source. Such skill handlers
     * can be used by plugins to hard code skills instead of using a script
     * framework such as MythicMobs or MMOLib.
     */
    public void registerBuiltinSkillHandlerSource(@NotNull Class<? extends SkillHandler<?>> clazz) {
        Validate.notNull(clazz, "Skill class cannot be null");

        final var annot = clazz.getAnnotation(BuiltinSkillHandler.class);
        Validate.notNull(annot, "No BuiltinSkillHandler annotation on class " + clazz.getName());

        final var key = UtilityMethods.enumName(clazz.getSimpleName());
        builtInSkillHandlerTypes.put(key, clazz);
    }

    //endregion

    @Override
    protected void onReset() {
        for (var handler : handlers.values())
            if (handler instanceof Listener) HandlerList.unregisterAll((Listener) handler);

        handlers.clear();
        scripts.clear();
        TriggerType.removeCustom();

        registration = true;
    }

    @Override
    protected void onStartup() {

        // Load built-in skill handler types
        loadBuiltinSkillHandlerTypes();

        // MythicMobs skill handler type
        if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null)
            registerSkillHandlerSource(new SkillHandlerSource("mythicmobs", MythicMobsSkillHandler::new, List.of("mythicmobs-skill-id")));

        // Fabled skill handler type
        if (Bukkit.getPluginManager().getPlugin("Fabled") != null)
            registerSkillHandlerSource(new SkillHandlerSource("fabled", FabledSkillHandler::new, List.of("fabled-skill-id", "skillapi-skill-id")));

        // CoreTools skill handler type
        if (Bukkit.getPluginManager().getPlugin("CoreTools") != null)
            registerSkillHandlerSource(new SkillHandlerSource("coretools", CoreToolsSkillHandler::new, List.of("coretools-script-id", "coretools-skill-id")));
    }

    @Override
    protected void onReload() {
        registration = false;

        // mkdir skill folder
        final var skillsFolder = new File(MythicLib.plugin.getDataFolder() + "/skill");
        if (!skillsFolder.exists()) {
            FileUtils.copyDefaultFile(MythicLib.plugin, "skill/example_skills.yml");
            FileUtils.copyDefaultFile(MythicLib.plugin, "skill/default_skills.yml");
        }

        // mkdir script folder
        final var scriptFolder = new File(MythicLib.plugin.getDataFolder() + "/script");
        if (!scriptFolder.exists()) {
            FileUtils.copyDefaultFile(MythicLib.plugin, "script/elemental_attacks.yml");
            FileUtils.copyDefaultFile(MythicLib.plugin, "script/mmoitems_scripts.yml");
            FileUtils.copyDefaultFile(MythicLib.plugin, "script/mmocore_scripts.yml");
            FileUtils.copyDefaultFile(MythicLib.plugin, "script/example_skills.yml");
            FileUtils.copyDefaultFile(MythicLib.plugin, "script/mitigation_types.yml");
            FileUtils.copyDefaultFile(MythicLib.plugin, "script/on_hit_effects.yml");
        }

        FileUtils.copyDefaultFile(MythicLib.plugin, "triggers.yml");

        // Load custom triggers
        var customTriggers = new YamlFile(MythicLib.plugin, "triggers").getContent();
        for (var key : customTriggers.getKeys(false))
            try {
                var subconfig = Objects.requireNonNull(customTriggers.getConfigurationSection(key), "Config cannot be null");
                TriggerType.register(new TriggerType(subconfig));
            } catch (Exception exception) {
                MythicLib.plugin.getLogger().log(Level.WARNING, "Could not load trigger '" + key + "': " + exception.getMessage());
            }

        // Initialize custom scripts
        FileUtils.loadObjectsFromFolder(MythicLib.plugin, "script", (key, config) -> {
            registerScript(new Script(Objects.requireNonNull(config, "Config is null")));
        }, "Could not load script '%s' from file '%s': '%s'");

        // Post-load custom scripts, publish some if needed
        for (var script : scripts.values())
            try {
                final var skillConfig = script.getPostLoadAction().getCachedConfig();
                script.getPostLoadAction().performAction();
                if (script.isPublic()) registerSkillHandler(new MythicLibSkillHandler(skillConfig, script));
            } catch (PostLoadException exception) {
                // Trying to load an alias, ignore
            } catch (RuntimeException exception) {
                MythicLib.plugin.getLogger().log(Level.WARNING, "Could not post-load script '" + script.getId() + "': " + exception.getMessage());
                exception.printStackTrace();
            }

        // [skill refactor] /skill folder migration for ML 1.7.1
        new SkillUpdateMigration(this.builtInSkillHandlerTypes.keySet()).apply();

        // Load skills
        FileUtils.loadObjectsFromFolder(MythicLib.plugin, "skill", (key, config) -> {
            registerSkillHandler(loadSkillHandler(config));
        }, "Could not load skill '%s' from file '%s': %s");
    }

    //region Deprecated

    @Deprecated
    public void registerBuiltinSkillHandlerType(@NotNull Class<? extends SkillHandler<?>> clazz) {
        this.registerBuiltinSkillHandlerSource(clazz);
    }

    @Deprecated
    public void initialize(boolean clearFirst) {
        reload();
    }

    @Deprecated
    public Script loadScript(@NotNull ConfigurationSection config, @NotNull String key) {
        return loadScript(key, config.get(key));
    }

    //endregion
}
