package io.lumine.mythic.lib.skill.handler;

import io.lumine.mythic.api.config.MythicConfig;
import io.lumine.mythic.api.skills.Skill;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.config.MythicConfigImpl;
import io.lumine.mythic.core.skills.MetaSkill;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.comp.anticheat.CheatType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.MythicMobsSkillResult;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class MythicMobsSkillHandler extends SkillHandler<MythicMobsSkillResult> {
    private final Skill skill;

    /**
     * Maps the amount of ticks during which the anticheat
     * must stop checking for hacks; for every cheat type
     */
    private final Map<CheatType, Integer> antiCheat;

    /**
     * [Optimization] Timings show that with ~100 players connected, servers
     * struggle with on-timer skills, which happen to have no entity targets,
     * which makes MythicLib to cast a ray trace to provide a default target
     * entity to the MythicMobs SkillMetadata object.
     * <p>
     * Note that this ray trace was implemented to stay consistent with the
     * behavior of the /mm cast command to avoid confusion for new plugin users.
     * <p>
     * Since these raytraces are not used in most on-timer skills, they are not
     * necessary. When toggled on, this option skips the raytrace altogether.
     */
    private final boolean skipRayTrace;

    public MythicMobsSkillHandler(@NotNull ConfigurationSection config, @NotNull String skillName) {
        super(config);

        final var skillManager = MythicBukkit.inst().getSkillManager();

        // Register extra skills first
        if (config.contains("extra-skills")) {
            MythicConfig mythicConfig = findParentMythicConfig(config, "extra-skills");

            for (String key : config.getConfigurationSection("extra-skills").getKeys(false))
                try {
                    MetaSkill metaSkill = new MetaSkill(skillManager, null, null, key, mythicConfig.getNestedConfig(key));
                    skillManager.registerSkill(key, metaSkill);
                } catch (RuntimeException exception) {
                    MythicLib.plugin.getLogger().log(Level.WARNING, "Could not register MythicMob extra skill '" + key + "' for custom skill handler '" + getId() + "': " + exception.getMessage());
                }
        }

        // Find corresponding MM skill
        final var mmSkillOpt = skillManager.getSkill(skillName);
        Validate.isTrue(mmSkillOpt.isPresent(), "Could not find MythicMobs skill with name '" + skillName + "'");
        skill = mmSkillOpt.get();

        skipRayTrace = config.getBoolean("skip_raytrace");

        // Disable anticheat feature?
        if (config.isConfigurationSection("disable-anti-cheat") && MythicLib.plugin.hasAntiCheat()) {
            antiCheat = new HashMap<>();
            for (String key : config.getConfigurationSection("disable-anti-cheat").getKeys(false)) {
                CheatType cheatType = CheatType.valueOf(key.toUpperCase().replace(" ", "_").replace("-", "_"));
                this.antiCheat.put(cheatType, config.getInt("disable-anti-cheat." + key));
            }
        } else antiCheat = Map.of();
    }

    private MythicConfig findParentMythicConfig(ConfigurationSection section, String extraConfigPath) {
        ConfigurationSection parent;
        StringBuilder fullPath = new StringBuilder();

        while ((parent = section.getParent()) != null) {
            fullPath.insert(0, '.').insert(0, section.getName());
            section = parent;
        }

        fullPath.append(extraConfigPath);
        return new MythicConfigImpl(fullPath.toString(), section); // Inshallah
    }

    public String getInternalName() {
        return skill.getInternalName();
    }

    public Skill getSkill() {
        return skill;
    }

    public Map<CheatType, Integer> getAntiCheat() {
        return antiCheat;
    }

    @Override
    public @NotNull MythicMobsSkillResult getResult(SkillMetadata meta) {
        return new MythicMobsSkillResult(meta, this, this.skipRayTrace);
    }

    @Override
    public void whenCast(MythicMobsSkillResult result, SkillMetadata skillMeta) {

        // Disable anticheat
        if (MythicLib.plugin.hasAntiCheat())
            MythicLib.plugin.getAntiCheat().disableAntiCheat(skillMeta.getCaster().getPlayer(), antiCheat);

        skill.execute(result.getMythicMobsSkillMetadata());
    }
}
