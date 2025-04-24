package io.lumine.mythic.lib.script;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.script.condition.Condition;
import io.lumine.mythic.lib.script.mechanic.Mechanic;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.PostLoadAction;
import io.lumine.mythic.lib.util.PreloadedObject;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.configobject.ConfigSectionObject;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * Custom script made using the MMOLib scripting language. MythicLib
 * modular approach lead scripts to dissociate from skills. A script
 * can be wrapped into a skill, and reciprocally a script can cast
 * any skill.
 *
 * @author jules
 */
public class Script implements PreloadedObject {
    private final String id;
    private final boolean publik;
    private final List<Condition> conditions = new ArrayList<>();
    private final List<Mechanic> mechanics = new ArrayList<>();

    private final PostLoadAction postLoadAction = new PostLoadAction(config -> {

        // Load conditions
        if (config.isConfigurationSection("conditions"))
            for (String str : config.getConfigurationSection("conditions").getKeys(false))
                registerCondition(str, () -> new ConfigSectionObject(config.getConfigurationSection("conditions." + str)));
        else if (config.isList("conditions")) for (Object obj : config.getList("conditions"))
            registerCondition(String.valueOf(obj), () -> new MMOLineConfig(String.valueOf(obj)));

        // Load mechanics
        if (config.isConfigurationSection("mechanics"))
            for (String str : config.getConfigurationSection("mechanics").getKeys(false))
                registerMechanic(str, () -> new ConfigSectionObject(config.getConfigurationSection("mechanics." + str)));
        else if (config.isList("mechanics")) for (Object obj : config.getList("mechanics"))
            registerMechanic(String.valueOf(obj), () -> new MMOLineConfig(String.valueOf(obj)));
    });

    public Script(@NotNull String key, @NotNull List<String> mechanics) {

        // Syntax adapter
        YamlConfiguration newConfig = new YamlConfiguration();
        newConfig.set(key + ".mechanics", mechanics);

        postLoadAction.cacheConfig(newConfig.getConfigurationSection(key));

        this.id = key;
        this.publik = false;
    }

    public Script(@NotNull ConfigurationSection config) {
        postLoadAction.cacheConfig(config);

        this.id = config.getName();
        this.publik = config.getBoolean("public", false);
    }

    public Script(String id, boolean publik) {
        this.id = id;
        this.publik = publik;
    }

    @NotNull
    @Override
    public PostLoadAction getPostLoadAction() {
        return postLoadAction;
    }

    private void registerCondition(String key, Supplier<ConfigObject> config) {
        try {
            conditions.add(MythicLib.plugin.getSkills().loadCondition(config.get()));
        } catch (RuntimeException exception) {
            MythicLib.plugin.getLogger().log(Level.WARNING, "Could not load condition '" + key + "' from script '" + id + "': " + exception.getMessage());
        }
    }

    private void registerMechanic(String key, Supplier<ConfigObject> config) {
        try {
            mechanics.add(MythicLib.plugin.getSkills().loadMechanic(config.get()));
        } catch (RuntimeException exception) {
            MythicLib.plugin.getLogger().log(Level.WARNING, "Could not load mechanic '" + key + "' from script '" + id + "': " + exception.getMessage());
        }
    }

    @NotNull
    public String getId() {
        return id;
    }

    /**
     * Should the skill be public i.e should a skill handler register for
     * that custom skill. If not, server admins won't be able to access
     * this skill when it is registered in the script manager.
     * <p>
     * Private scripts can be used within the scripting language scope,
     * public scripts are basically turned into skills to be used in
     * other MMO plugins.
     *
     * @return If the skill should be accessible to other plugins
     */
    public boolean isPublic() {
        return publik;
    }

    @NotNull
    public List<Mechanic> getMechanics() {
        return mechanics;
    }

    @NotNull
    public List<Condition> getConditions() {
        return conditions;
    }

    /**
     * Cast this skill
     *
     * @param meta Skill cast information
     * @return If conditions are met ie skill is cast
     */
    public boolean cast(SkillMetadata meta) {

        int conditionCounter = 0;
        for (Condition condition : conditions)
            try {
                conditionCounter += 1;
                if (!condition.checkIfMet(meta)) return false;
            } catch (RuntimeException exception) {
                MythicLib.plugin.getLogger().log(Level.WARNING, "Could not check condition n" + conditionCounter + " from script '" + id + "': " + exception.getMessage());
                return false;
            }

        new MechanicQueue(meta, this).next();
        return true;
    }
}
