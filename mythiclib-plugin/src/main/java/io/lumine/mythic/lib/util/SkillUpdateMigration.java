package io.lumine.mythic.lib.util;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.util.annotation.BackwardsCompatibility;
import io.lumine.mythic.lib.util.config.YamlFile;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;

@BackwardsCompatibility(version = "1.7.1-SNAPSHOT")
public class SkillUpdateMigration {

    // all mythiclib skills do not use the ID provided by the config section header
    // instead, they all use
    // we want to change that because it is a terrible design decision, so we need
    // to populate a backward map, that maps from internal script name to (file, config key)
    //
    // at the end of this script, we change all keys. to make sure it is only performed
    // once, we perform the migration when transfering MMOCore skills
    // could have been MMOItems, it is completely arbitrary
    private final Map<String, Pair<File, String>> backwardMap = new HashMap<>();
    private final Set<String> backwardDueToScripts = new HashSet<>();
    private final Set<String> builtinSkills;

    private boolean updateRequired;

    public SkillUpdateMigration(Set<String> builtinSkills) {
        this.builtinSkills = builtinSkills;
    }

    public void apply() {

        // convert ML skills and populate backward maps
        convertMythicLibSkills();

        // transfer skills from mmocore/mmoitems folders to mythiclib
        transferLegacySkills("MMOCore", "skills",
                this::mmocoreSkillPreprocessor,
                this::mmocoreSkillSimplifier,
                this::mmocoreMerger);
        transferLegacySkills("MMOItems", "skill",
                this::mmoitemSkillPreprocessor,
                this::mmoitemSkillSimplifier,
                this::mmoitemMerger);

        // fix skill handler ids......
        // only fix if at least one skill is transferred from mmoitems/mmocore
        if (updateRequired) {
            updateLegacySkillHandlerIds();
            updateLegacySources();
        }
    }

    private void convertMythicLibSkills() {

        var counter = new AtomicInteger(0);

        // get all files inside /skill folder
        FileUtils.exploreFolderRecursively(new File(MythicLib.plugin.getDataFolder() + "/skill"), file -> {
            final var config0 = YamlConfiguration.loadConfiguration(file);
            var save = false;

            // if file contains one of these keys then it should be converted to keyed config
            // this makes sure MythicLib can use loadObjectsFromFolder from now on
            // TODO there should be more keys than that but this should cover 99.9% skills
            if (config0.contains("modifiers") ||
                    config0.contains("mythiclib-skill-id") || config0.contains("mythicmobs-skill-id")) {
                // Infer skill ID and use as config key
                final var inferredSkillId = file.getName().substring(0, file.getName().length() - 4).toUpperCase().replace(" ", "_").replace("-", "_");
                // Clear config first
                for (var key : config0.getKeys(false)) config0.set(key, null);
                // Clone content to avoid concurrent modification
                config0.set(inferredSkillId, YamlConfiguration.loadConfiguration(file));
                save = true;
            }

            // transform "modifiers" string list into 'parameters' config section
            for (var key : config0.getKeys(false)) {
                final var skillConfig = config0.getConfigurationSection(key);
                if (skillConfig == null) {
                    MythicLib.plugin.getLogger().log(Level.WARNING, "Skill '" + key + "' in file '" + file.getName() + "' is not a config section, skipping (1)");
                    continue;
                }

                if (skillConfig.contains("modifiers")) {
                    // transform list into section
                    for (var mod : skillConfig.getStringList("modifiers"))
                        skillConfig.createSection("parameters." + mod);
                    skillConfig.set("modifiers", null); // remove list
                    save = true;
                }
            }

            // for each key, populate backward map
            for (var key : config0.getKeys(false)) {
                final var subconfig = config0.getConfigurationSection(key);
                if (subconfig == null) {
                    MythicLib.plugin.getLogger().log(Level.WARNING, "X Skill '" + key + "' in file '" + file.getName() + "' is not a config section, skipping (4)");
                    continue;
                }

                final var legacySkillHandlerId = getLegacySkillHandlerId(subconfig);
                if (legacySkillHandlerId == null) {
                    // This log shows everytime a skill handler is found with no source
                    // MythicLib.plugin.getLogger().log(Level.WARNING, "X Skill '" + key + "' in file '" + file.getName() + "' has no skill type, skipping (5)");
                    continue;
                }

                backwardMap.put(legacySkillHandlerId, Pair.of(file, key));
            }

            if (save) try {
                config0.save(file);
                counter.incrementAndGet();
            } catch (Exception e) {
                MythicLib.plugin.getLogger().log(Level.WARNING, "X Could not convert MythicLib skill file '" + file.getName() + "': " + e.getMessage());
                e.printStackTrace();
            }
        });

        // Explore all scripts and populate backward map
        FileUtils.exploreFolderRecursively(new File(MythicLib.plugin.getDataFolder() + "/script"), file -> {
            final var config0 = YamlConfiguration.loadConfiguration(file);

            // key = script id
            // !!!! scripts are case sensitive. no case manipulation or anything compared to skills !!!!
            for (var key : config0.getKeys(false)) {
                final var subconfig = config0.getConfigurationSection(key);
                if (subconfig == null) {
                    MythicLib.plugin.getLogger().log(Level.WARNING, "Script '" + key + "' in file '" + file.getName() + "' is not a config section, skipping (2)");
                    continue;
                }

                // Script not published as a skill
                if (!subconfig.getBoolean("public")) continue;

                final var legacySkillHandlerId = UtilityMethods.enumName(key);
                backwardMap.put(legacySkillHandlerId, Pair.of(file, key));
                backwardDueToScripts.add(legacySkillHandlerId);
            }
        });

        if (counter.get() > 0) {
            MythicLib.plugin.getLogger().log(Level.INFO, "Updated " + counter + " legacy MythicLib skills");
        }
    }

    private static final Map<String, String> INTERNAL_ID_PATHS = Map.of(
            "mythiclib-skill-id", "mythiclib",
            "fabled-skill-id", "fabled",
            "skillapi-skill-id", "fabled",
            "coretools-script-id", "coretools",
            "builtin_skill", "default",
            "mythicmobs-skill-id", "mythicmobs"
    );

    @Nullable
    private String getLegacySkillHandlerId(ConfigurationSection skillRootConfig) {
        for (var internalScriptNamePath : INTERNAL_ID_PATHS.keySet()) {
            final var key = skillRootConfig.getString(internalScriptNamePath);
            if (key != null && !key.isEmpty()) return UtilityMethods.enumName(key);
        }

        final var source = skillRootConfig.get("source");
        if (source != null && source instanceof String) {
            final var asString = (String) source;
            if (!asString.contains(":")) return asString;
            return UtilityMethods.enumName(asString.split(":", 2)[1]);
        }

        return null;
    }

    private void mmoitemSkillPreprocessor(ConfigurationSection sourceYamlFile) {
        // nothing
    }

    private void mmoitemSkillSimplifier(ConfigurationSection sourceYamlFile, String legacySkillHandlerId) {
        // nothing
    }

    private void mmoitemMerger(ConfigurationSection sourceYamlFile, FileConfiguration targetYamlFile, String configKey) {

        final var targetSection = targetYamlFile.contains(configKey)
                ? targetYamlFile.getConfigurationSection(configKey)
                : targetYamlFile.createSection(configKey);

        // merge name
        final var sourceName = sourceYamlFile.getString("name");
        if (sourceName != null && targetSection.get("name") == null)
            targetSection.set("name", sourceName);

        // Merge modifiers
        if (sourceYamlFile.isConfigurationSection("modifier"))
            for (var key : sourceYamlFile.getConfigurationSection("modifier").getKeys(false)) {
                targetSection.set("parameters." + key + ".name", sourceYamlFile.get("modifier." + key + ".name"));
                targetSection.set("parameters." + key + ".item", sourceYamlFile.get("modifier." + key + ".default-value"));
            }
    }

    private void mmocoreSkillPreprocessor(ConfigurationSection sourceYamlFile) {

        // replace "passive-type" and "material" keys
        replaceKey(sourceYamlFile, "material", "icon");
        replaceKey(sourceYamlFile, "passive-type", "trigger");

        // if does not contain "parameters", create and move modifiers inside
        if (!sourceYamlFile.contains("parameters")) {
            sourceYamlFile.createSection("parameters");
            // transfer all keys to inside "parameters"
            for (var key : sourceYamlFile.getKeys(false)) {
                if (key.equals("parameters")
                        || key.equals("lore")
                        || key.equals("trigger")
                        || INTERNAL_ID_PATHS.containsKey(key)
                        || key.equals("icon")
                        || key.equals("name"))
                    continue;
                sourceYamlFile.set("parameters." + key, sourceYamlFile.get(key));
                sourceYamlFile.set(key, null);
            }
        }

        // put all modifiers inside "player"
        for (var key : sourceYamlFile.getConfigurationSection("parameters").getKeys(false)) {
            var value = sourceYamlFile.get("parameters." + key);
            sourceYamlFile.set("parameters." + key, null);
            sourceYamlFile.set("parameters." + key + ".player", value);

            sourceYamlFile.set("parameters." + key + ".format", sourceYamlFile.get("parameters." + key + ".player.decimal-format"));
            sourceYamlFile.set("parameters." + key + ".player.decimal-format", null);
        }

    }

    private void mmocoreSkillSimplifier(ConfigurationSection sourceYamlFile, String legacySkillHandlerId) {
        // if script, to avoid config clutter, remove superfluous shit
        if (backwardDueToScripts.contains(legacySkillHandlerId)) {

            // empty name
            if (sourceYamlFile.getString("name", "").equals(UtilityMethods.caseOnWords(legacySkillHandlerId.replace("_", " ").toLowerCase())))
                sourceYamlFile.set("name", null);
            // empty lore
            var lore = sourceYamlFile.getStringList("lore");
            if (!lore.isEmpty() && lore.get(0).equals("This is the default skill description"))
                sourceYamlFile.set("lore", null);
            // empty material
            if (sourceYamlFile.getString("icon", "").equals("BOOK"))
                sourceYamlFile.set("icon", null);
            // empty parameters
            for (var key : sourceYamlFile.getConfigurationSection("parameters").getKeys(false))
                if (isSkillParamUseless(sourceYamlFile, key)) sourceYamlFile.set("parameters." + key, null);
            if (sourceYamlFile.getConfigurationSection("parameters").getKeys(false).isEmpty())
                sourceYamlFile.set("parameters", null);
        }
    }

    private void mmocoreMerger(ConfigurationSection sourceYamlFile, FileConfiguration targetYamlFile, String configKey) {
        // Merge config sections
        for (var key : sourceYamlFile.getKeys(false))
            if (!key.equals("parameters"))
                targetYamlFile.set(configKey + "." + key, sourceYamlFile.get(key));

        // Merge "parameters" section
        final var sourceParamsConfig = sourceYamlFile.getConfigurationSection("parameters");
        if (sourceParamsConfig != null) {
            final var targetParamsPath = configKey + ".parameters";
            final var targetParamsConfig = targetYamlFile.contains(targetParamsPath)
                    ? targetYamlFile.getConfigurationSection(targetParamsPath)
                    : targetYamlFile.createSection(targetParamsPath);
            for (var paramKey : sourceParamsConfig.getKeys(false))
                targetParamsConfig.set(paramKey, sourceParamsConfig.get(paramKey));
        }
    }

    private void transferLegacySkills(String pluginName,
                                      String skillFolderName,
                                      Consumer<ConfigurationSection> preprocessor,
                                      BiConsumer<ConfigurationSection, String> simplifier,
                                      TriConsumer<ConfigurationSection, FileConfiguration, String> merger) {

        final var pluginSkillFolder = new File(MythicLib.plugin.getDataFolder().getParentFile() + "/" + pluginName + "/" + skillFolderName);
        if (!pluginSkillFolder.exists()) return;

        ///////////////////////////////
        // run transfer !!
        ///////////////////////////////
        updateRequired = true;
        MythicLib.plugin.getLogger().log(Level.INFO, "Converting legacy " + pluginName + " skills to MythicLib format, might take a while...");

        // this file will contain all default skill handlers
        // (ambers, fireball........)
        final var defaultSkills = new YamlFile(MythicLib.plugin, "skill", "default_skills");
        var saveDefault = new AtomicBoolean(false);

        // this file is used to store any skills that fail to be mapped
        // maybe the skill no longer exists?
        final var fallbackSkills = new YamlFile(MythicLib.plugin, "skill", "error_skills");
        var saveFallback = new AtomicBoolean(false);

        // Technically users were not allowed to have subfolders
        // but some of them will have them anyways, just to avoid
        // log spams, take this into consideration and convert all
        // files on a best-effort basis
        FileUtils.exploreFolderRecursively(pluginSkillFolder, file -> {
            final var sourceYamlFile = YamlConfiguration.loadConfiguration(file);
            // ID of skill handler
            var legacySkillHandlerId = file.getName().substring(0, file.getName().length() - 4).toUpperCase().replace(" ", "_").replace("-", "_");

            ///////////////////////////////
            // preprocess source file
            ///////////////////////////////

            preprocessor.accept(sourceYamlFile);

            ///////////////////////////////////
            // trace back to skill handler source file!!
            ///////////////////////////////////
            final FileConfiguration targetYamlFile;
            final File targetFile;
            final String configKey;
            final boolean saveFile;

            final Pair<File, String> backwardSkill;
            if (builtinSkills.contains(legacySkillHandlerId)) {
                // default skill
                sourceYamlFile.set("source", "default:" + legacySkillHandlerId);
                targetYamlFile = defaultSkills.getContent();
                configKey = legacySkillHandlerId;
                targetFile = defaultSkills.getFile();
                saveDefault.set(true);
                saveFile = false;
                MythicLib.plugin.getLogger().log(Level.INFO, "- Found builtin skill " + legacySkillHandlerId + " -> " + targetFile.getPath());
            } else if ((backwardSkill = backwardMap.get(legacySkillHandlerId)) != null) {

                simplifier.accept(sourceYamlFile, legacySkillHandlerId); // simplify config to reduce clutter

                targetYamlFile = YamlConfiguration.loadConfiguration(backwardSkill.getLeft());
                configKey = backwardSkill.getRight();
                targetFile = backwardSkill.getLeft();
                saveFile = true;
                MythicLib.plugin.getLogger().log(Level.INFO, "- Found skill " + legacySkillHandlerId + " -> " + targetFile.getPath());
            } else {
                // fallback. some skills might not exist anymore
                targetYamlFile = fallbackSkills.getContent();
                configKey = legacySkillHandlerId;
                targetFile = fallbackSkills.getFile();
                saveFallback.set(true);
                saveFile = false;
                MythicLib.plugin.getLogger().log(Level.WARNING, "- Found " + pluginName + " skill '" + legacySkillHandlerId + "' with no MythicLib counterpart -> " + targetFile.getPath());
            }

            ///////////////////////////////////
            // merge source with target
            ///////////////////////////////////

            merger.accept(sourceYamlFile, targetYamlFile, configKey);

            // Finally, save file. THAT was a journey ffs
            if (saveFile) try {
                targetYamlFile.save(targetFile);
            } catch (Exception e) {
                MythicLib.plugin.getLogger().log(Level.WARNING, "X Could not transfer " + pluginName + " skill '" + legacySkillHandlerId + "': " + e.getMessage());
                e.printStackTrace();
            }
            file.delete();
        });

        if (saveFallback.get()) fallbackSkills.save();
        if (saveDefault.get()) defaultSkills.save();

        try {
            // Delete plugin skill folder if empty
            // [[[comment out this line for testing]]]
            recursiveEmptyDirectoryRemove(pluginSkillFolder);
        } catch (Exception exception) {
            MythicLib.plugin.getLogger().log(Level.WARNING, "X Could not delete " + pluginName + " skill folder: " + exception.getMessage());
        }
    }

    private void recursiveEmptyDirectoryRemove(File folder) {
        // Only delete the folder if it is empty
        // It's okay if it contains subfolders as long as
        // they are empty too
        Validate.isTrue(folder.isDirectory(), "Not a folder: " + folder.getPath());
        for (var file : Objects.requireNonNull(folder.listFiles())) recursiveEmptyDirectoryRemove(file);
        if (!folder.delete()) throw new RuntimeException("Could not delete folder " + folder.getPath());
    }

    private boolean isSkillParamUseless(ConfigurationSection config, String paramKey) {
        for (var subkey : config.getConfigurationSection("parameters." + paramKey + ".player").getKeys(false)) {
            var ob = config.get("parameters." + paramKey + ".player." + subkey);
            var useless = ob instanceof Number && Math.abs(((Number) ob).doubleValue()) < 1e-5;
            if (!useless) return false;
        }
        return true;
    }

    private void replaceKey(ConfigurationSection config, String oldKey, String newKey) {
        if (config.contains(oldKey)) {
            config.set(newKey, config.get(oldKey));
            config.set(oldKey, null);
        }
    }

    private void updateLegacySkillHandlerIds() {

        MythicLib.plugin.getLogger().log(Level.INFO, "Updating legacy skill IDs...");

        FileUtils.exploreFolderRecursively(new File(MythicLib.plugin.getDataFolder() + "/skill"), file -> {

            final var config0 = YamlConfiguration.loadConfiguration(file);
            var save = false;

            for (var key : config0.getKeys(false)) {
                final var subconfig = config0.getConfigurationSection(key);
                if (subconfig == null) {
                    MythicLib.plugin.getLogger().log(Level.WARNING, "X Skill '" + key + "' in file '" + file.getName() + "' is not a config section, skipping (3)");
                    continue;
                }

                var legacySkillHandlerId = getLegacySkillHandlerId(subconfig);
                if (legacySkillHandlerId == null) {
                    MythicLib.plugin.getLogger().log(Level.WARNING, "X Skill '" + key + "' in file '" + file.getName() + "' has no skill type, skipping (6)");
                    continue;
                }

                legacySkillHandlerId = UtilityMethods.enumName(legacySkillHandlerId); // uppercase expected
                if (backwardMap.containsKey(legacySkillHandlerId) && !key.equals(legacySkillHandlerId)) {
                    // rename key
                    final var skillConfig = config0.getConfigurationSection(key);
                    config0.set(legacySkillHandlerId, skillConfig);
                    config0.set(key, null);
                    save = true;
                    MythicLib.plugin.getLogger().log(Level.INFO, "- Updated skill ID from '" + key + "' to '" + legacySkillHandlerId + "' in file " + file.getName());
                }
            }

            if (save) try {
                config0.save(file);
            } catch (Exception e) {
                MythicLib.plugin.getLogger().log(Level.INFO, "X Could not update skill IDs in file " + file.getName());
                e.printStackTrace();
            }
        });
    }

    private void updateLegacySources() {

        MythicLib.plugin.getLogger().log(Level.INFO, "Updating legacy skill sources...");

        FileUtils.exploreFolderRecursively(new File(MythicLib.plugin.getDataFolder() + "/skill"), file -> {

            final var config0 = YamlConfiguration.loadConfiguration(file);
            var save = false;

            skillLoop:
            for (var key : config0.getKeys(false)) {
                final var subconfig = config0.getConfigurationSection(key);
                if (subconfig == null) {
                    MythicLib.plugin.getLogger().log(Level.WARNING, "X Skill '" + key + "' in file '" + file.getName() + "' is not a config section, skipping (3)");
                    continue;
                }

                for (var internalScriptNamePath : INTERNAL_ID_PATHS.keySet())
                    if (subconfig.contains(internalScriptNamePath)) {
                        final var newSource = INTERNAL_ID_PATHS.get(internalScriptNamePath);
                        subconfig.set("source", newSource + ":" + subconfig.getString(internalScriptNamePath));
                        subconfig.set(internalScriptNamePath, null);
                        save = true;
                        MythicLib.plugin.getLogger().log(Level.INFO, "- Updated skill source for '" + key + "' in file " + file.getName());
                        continue skillLoop;
                    }
            }

            if (save) try {
                config0.save(file);
            } catch (Exception e) {
                MythicLib.plugin.getLogger().log(Level.INFO, "X Could not update skill IDs in file " + file.getName());
                e.printStackTrace();
            }
        });
    }
}
