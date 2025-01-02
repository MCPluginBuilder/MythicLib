package io.lumine.mythic.lib.skill.handler;

import io.lumine.mythic.api.config.MythicConfig;
import io.lumine.mythic.api.skills.Skill;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.skills.MetaSkill;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.comp.anticheat.CheatType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.MythicMobsSkillResult;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

public class MythicMobsSkillHandler extends SkillHandler<MythicMobsSkillResult> {
    private final Skill skill;

    /**
     * Maps the amount of ticks during which the anticheat
     * must stop checking for hacks; for every cheat type
     */
    private final Map<CheatType, Integer> antiCheat = new HashMap<>();

    public MythicMobsSkillHandler(ConfigurationSection config) {
        super(config, config.getName().isEmpty() ? config.getString("mythicmobs-skill-id") : config.getName());

        final SkillExecutor skillManager = MythicBukkit.inst().getSkillManager();

        // Register extra skills first
        if (config.contains("extra-skills")) {

           /* // Find FileConfiguration again
            final long steps = config.getCurrentPath().chars().filter(ch -> ch == 'e').count();
            ConfigurationSection explored = config;
            for (int i = 0; i < steps + 1; i++)
                explored = explored.getParent();
            Validate.isTrue(explored instanceof FileConfiguration, "An error occured when attempting to roll back to config file");
            final FileConfiguration configFile = (FileConfiguration) explored;*/

            for (String key : config.getConfigurationSection("extra-skills").getKeys(false))
                try {
                    MythicConfig mythicConfig = new MythicConfigImpl("extra-skills." + key, config);
                    MetaSkill metaSkill = new MetaSkill(skillManager, null, null, key, mythicConfig);
                    skillManager.registerSkill(key, metaSkill);
                } catch (RuntimeException exception) {
                    MythicLib.plugin.getLogger().log(Level.WARNING, "Could not register MythicMob extra skill '" + key + "' for custom skill handler '" + getId() + "': " + exception.getMessage());
                }
        }

        String skillName = config.getString("mythicmobs-skill-id");
        Optional<Skill> opt = skillManager.getSkill(skillName);
        Validate.isTrue(opt.isPresent(), "Could not find MM skill with name '" + skillName + "'");
        skill = opt.get();

        if (config.isConfigurationSection("disable-anti-cheat"))
            for (String key : config.getConfigurationSection("disable-anti-cheat").getKeys(false)) {
                CheatType cheatType = CheatType.valueOf(key.toUpperCase().replace(" ", "_").replace("-", "_"));
                this.antiCheat.put(cheatType, config.getInt("disable-anti-cheat." + key));
            }
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
    public MythicMobsSkillResult getResult(SkillMetadata meta) {
        return new MythicMobsSkillResult(meta, this);
    }

    @Override
    public void whenCast(MythicMobsSkillResult result, SkillMetadata skillMeta) {

        // Disable anticheat
        if (MythicLib.plugin.hasAntiCheat())
            MythicLib.plugin.getAntiCheat().disableAntiCheat(skillMeta.getCaster().getPlayer(), antiCheat);

        skill.execute(result.getMythicMobsSkillMetadata());
    }
}
