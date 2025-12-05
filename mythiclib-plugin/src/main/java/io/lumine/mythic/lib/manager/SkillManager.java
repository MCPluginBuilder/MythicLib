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
import io.lumine.mythic.lib.script.targeter.location.LookingAtTargeter;
import io.lumine.mythic.lib.script.targeter.location.*;
import io.lumine.mythic.lib.skill.handler.*;
import io.lumine.mythic.lib.util.FileUtils;
import io.lumine.mythic.lib.util.PostLoadException;
import io.lumine.mythic.lib.util.SkillUpdateMigration;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
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
    private final Map<Predicate<ConfigurationSection>, Function<ConfigurationSection, SkillHandler<?>>> skillHandlerTypes = new HashMap<>();

    private boolean registration = true;

    public SkillManager(MMOPlugin plugin) {
        super(plugin);

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

        registerSkillHandlerType(config -> config.contains("builtin_skill"), this::loadBuiltinSkillHandler);
        registerSkillHandlerType(config -> config.contains("mythiclib-skill-id"), MythicLibSkillHandler::new);
    }

    @NotNull
    private SkillHandler<?> loadBuiltinSkillHandler(@NotNull ConfigurationSection config) {
        final var builtinId = UtilityMethods.enumName(config.getString("builtin_skill"));
        final var mapping = builtInSkillHandlerTypes.get(builtinId);
        Validate.notNull(mapping, "Could not find builtin skill with ID '" + builtinId + "'");
        try {
            return mapping.getDeclaredConstructor(ConfigurationSection.class).newInstance(config);
        } catch (Exception exception) {
            throw new RuntimeException("Could not instantiate builtin skill handler '" + builtinId + "'", exception);
        }
    }

    /**
     * @param matcher  If a certain skill config redirects to the skill handler
     *                 Example: a config which the following key should be handled
     *                 by {@link io.lumine.mythic.lib.skill.handler.MythicMobsSkillHandler}
     *                 <code>mythic-mobs-skill-id: WarriorStrike</code>
     * @param provider Function that provides the skill handler given the previous config,
     *                 if the config matches
     */
    public void registerSkillHandlerType(@NotNull Predicate<ConfigurationSection> matcher, @NotNull Function<ConfigurationSection, SkillHandler<?>> provider) {
        Validate.notNull(matcher);
        Validate.notNull(provider);

        skillHandlerTypes.put(matcher, provider);
    }

    public void registerBuiltinSkillHandlerType(@NotNull Class<? extends SkillHandler<?>> clazz) {
        Validate.notNull(clazz, "Skill class cannot be null");

        final var annot = clazz.getAnnotation(BuiltinSkillHandler.class);
        Validate.notNull(annot, "No BuiltinSkillHandler annotation on class " + clazz.getName());

        final var key = UtilityMethods.enumName(clazz.getSimpleName());
        builtInSkillHandlerTypes.put(key, clazz);
    }

    @NotNull
    public SkillHandler<?> loadSkillHandler(Object obj) throws IllegalArgumentException, IllegalStateException {

        // By handler name
        if (obj instanceof String) return getHandlerOrThrow(UtilityMethods.enumName((String) obj));

        // By type of configuration section
        if (obj instanceof ConfigurationSection) {
            ConfigurationSection config = (ConfigurationSection) obj;
            for (var type : skillHandlerTypes.entrySet())
                if (type.getKey().test(config)) return type.getValue().apply(config);

            throw new IllegalArgumentException("Could not match skill type to config");
        }

        // TODO support lists

        throw new IllegalArgumentException("Provide either a string or configuration section instead of " + obj.getClass().getSimpleName());
    }

    public void registerSkillHandler(SkillHandler<?> handler) {
        Validate.isTrue(handlers.putIfAbsent(handler.getId(), handler) == null, "A skill with the same name already exists");

        if (!registration && handler instanceof Listener)
            Bukkit.getPluginManager().registerEvents((Listener) handler, MythicLib.plugin);
    }

    @NotNull
    public SkillHandler<?> getHandlerOrThrow(String id) {
        return Objects.requireNonNull(handlers.get(id), "Could not find skill with ID '" + id + "'");
    }

    /**
     * @return Currently registered skill handlers.
     */
    public Collection<SkillHandler<?>> getHandlers() {
        return handlers.values();
    }

    @Nullable
    public SkillHandler<?> getHandler(String handlerId) {
        return handlers.get(handlerId);
    }

    public void registerScript(@NotNull Script script) {
        Validate.isTrue(!scripts.containsKey(script.getId()), "A script with the same name already exists");
        scripts.put(script.getId(), script);
    }

    @NotNull
    public Script getScriptOrThrow(String name) {
        return Objects.requireNonNull(scripts.get(name), "Could not find script with name '" + name + "'");
    }

    @NotNull
    public Script loadScript(Object obj) {
        // Arbitrary default script name
        return loadScript("UnidentifiedScript", obj);
    }

    @NotNull
    public Script loadScript(@NotNull String key, @NotNull Object genericInput) {
        Validate.notNull(genericInput, "Object cannot be null");

        if (genericInput instanceof String) return getScriptOrThrow(genericInput.toString());

        if (genericInput instanceof ConfigurationSection) {
            Script skill = new Script((ConfigurationSection) genericInput);
            skill.getPostLoadAction().performAction();
            return skill;
        }

        // Adapt a list to a config section
        if (genericInput instanceof List) {
            Validate.notNull(key, "Key cannot be null");
            Script skill = new Script(key, (List<String>) genericInput);
            skill.getPostLoadAction().performAction();
            return skill;
        }

        throw new IllegalArgumentException("Expected a string, config section or list");
    }

    @NotNull
    public Collection<Script> getScripts() {
        return scripts.values();
    }

    @NotNull
    private String findEffectiveObjectType(String objectType, ConfigObject config) {
        if (config.contains("type")) return config.getString("type");
        else if (config.hasKey()) return config.getKey();
        else throw new IllegalArgumentException("Could not find " + objectType + " type");
    }

    public void registerCondition(String name, Function<ConfigObject, Condition> condition, String... aliases) {
        Validate.isTrue(registration, "Condition registration is disabled");
        Validate.isTrue(!conditions.containsKey(name), "A condition with the same name already exists");
        Validate.notNull(condition, "Function cannot be null");

        conditions.put(name, condition);

        for (String alias : aliases)
            registerCondition(alias, condition);
    }

    @NotNull
    public Condition loadCondition(ConfigObject config) {
        final String key = findEffectiveObjectType("condition", config);
        final Function<ConfigObject, Condition> supplier = conditions.get(key);
        Validate.notNull(supplier, "Could not match condition to '" + key + "'");
        return supplier.apply(config);
    }

    public void registerMechanic(@NotNull String name, @NotNull Function<ConfigObject, Mechanic> mechanic, String... aliases) {
        Validate.isTrue(registration, "Mechanic registration is disabled");
        Validate.isTrue(!mechanics.containsKey(name), "A mechanic with the name '" + name + "' already exists");
        Validate.notNull(mechanic, "Function cannot be null");

        mechanics.put(name, mechanic);

        for (String alias : aliases)
            registerMechanic(alias, mechanic);
    }

    @NotNull
    public Mechanic loadMechanic(ConfigObject config) {
        final String key = findEffectiveObjectType("mechanic", config);
        final Function<ConfigObject, Mechanic> supplier = mechanics.get(key);
        Validate.notNull(supplier, "Could not match mechanic to '" + key + "'");
        return supplier.apply(config);
    }

    public void registerEntityTargeter(String name, Function<ConfigObject, EntityTargeter> entityTarget) {
        Validate.isTrue(registration, "Targeter registration is disabled");
        Validate.isTrue(!entityTargets.containsKey(name), "A targeter with the same name already exists");
        Validate.notNull(entityTarget, "Function cannot be null");

        entityTargets.put(name, entityTarget);
    }

    @NotNull
    public EntityTargeter loadEntityTargeter(ConfigObject config) {
        final String key = findEffectiveObjectType("targeter", config);
        final Function<ConfigObject, EntityTargeter> supplier = entityTargets.get(key);
        Validate.notNull(supplier, "Could not match targeter to '" + key + "'");
        return supplier.apply(config);
    }

    public void registerLocationTargeter(String name, Function<ConfigObject, LocationTargeter> locationTarget) {
        Validate.isTrue(registration, "Targeter registration is disabled");
        Validate.isTrue(!locationTargets.containsKey(name), "A targeter with the same name already exists");
        Validate.notNull(locationTarget, "Function cannot be null");

        locationTargets.put(name, locationTarget);
    }

    @NotNull
    public LocationTargeter loadLocationTargeter(ConfigObject config) {
        final String key = findEffectiveObjectType("targeter", config);
        final Function<ConfigObject, LocationTargeter> supplier = locationTargets.get(key);
        Validate.notNull(supplier, "Could not match targeter to '" + key + "'");
        return supplier.apply(config);
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
                    registerBuiltinSkillHandlerType((Class<? extends SkillHandler<?>>) clazz);
                }
            }
            file.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Override
    protected void onReset() {
        for (var handler : handlers.values())
            if (handler instanceof Listener) HandlerList.unregisterAll((Listener) handler);

        handlers.clear();
        scripts.clear();

        registration = true;
    }

    @Override
    protected void onStartup() {

        // Load built-in skill handler types
        loadBuiltinSkillHandlerTypes();

        // MythicMobs skill handler type
        if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null)
            registerSkillHandlerType(config -> config.contains("mythicmobs-skill-id"), MythicMobsSkillHandler::new);

        // Fabled skill handler type
        if (Bukkit.getPluginManager().getPlugin("Fabled") != null)
            registerSkillHandlerType(config -> config.contains("fabled-skill-id") || config.contains("skillapi-skill-id"), FabledSkillHandler::new);

        // CoreTools skill handler type
        if (Bukkit.getPluginManager().getPlugin("CoreTools") != null)
            registerSkillHandlerType(config -> config.contains("coretools-script-id"), CoreToolsSkillHandler::new);
    }

    @Override
    protected void onReload() {
        registration = false;

        // mkdir skill folder
        File skillsFolder = new File(MythicLib.plugin.getDataFolder() + "/skill");
        if (!skillsFolder.exists()) skillsFolder.mkdir();

        // mkdir script folder
        File scriptFolder = new File(MythicLib.plugin.getDataFolder() + "/script");
        if (!scriptFolder.exists()) {
            FileUtils.copyDefaultFile(MythicLib.plugin, "script/elemental_attacks.yml");
            FileUtils.copyDefaultFile(MythicLib.plugin, "script/mmoitems_scripts.yml");
            FileUtils.copyDefaultFile(MythicLib.plugin, "script/mmocore_scripts.yml");
            FileUtils.copyDefaultFile(MythicLib.plugin, "script/example_skills.yml");
            FileUtils.copyDefaultFile(MythicLib.plugin, "script/mitigation_types.yml");
            FileUtils.copyDefaultFile(MythicLib.plugin, "script/on_hit_effects.yml");
        }

        // Initialize custom scripts
        FileUtils.loadObjectsFromFolder(MythicLib.plugin, "script", false, (key, config) -> {
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
        FileUtils.loadObjectsFromFolder(MythicLib.plugin, "skill", false, (key, config) -> {
            registerSkillHandler(loadSkillHandler(config));
        }, "Could not load skill '%s' from file '%s': %s");
    }

    //region Deprecated

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
