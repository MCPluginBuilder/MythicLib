package io.lumine.mythic.lib.rpg;

import io.lumine.mythic.lib.rpg.provided.DummyModule;
import io.lumine.mythic.lib.rpg.provided.NativeManaModule;
import io.lumine.mythic.lib.rpg.provided.PlaceholderClassModule;
import io.lumine.mythic.lib.rpg.provided.PlaceholderLevelModule;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.function.Supplier;

public enum RPGPluginEnum {
    NONE(DummyModule.class),
    MYTHICLIB(NativeManaModule.class),
    PLACEHOLDER_LEVEL(PlaceholderLevelModule.class),
    PLACEHOLDER_CLASS(PlaceholderClassModule.class),

    MMOCORE("MMOCore", "net.Indyuce.mmocore.comp.MMOCoreModule"),
    HEROES("Heroes", "io.lumine.mythic.lib.rpg.compat.HeroesHook"),
    FABLED("Fabled", "io.lumine.mythic.lib.rpg.compat.FabledHook"),
    RPGPLAYERLEVELING("RPGPlayerLeveling", "io.lumine.mythic.lib.rpg.compat.RPGPlayerLevelingHook"),
    RACESANDCLASSES("RacesAndClasses", "io.lumine.mythic.lib.rpg.compat.RacesAndClassesHook"),
    BATTLELEVELS("BattleLevels", "io.lumine.mythic.lib.rpg.compat.BattleLevelsHook"),
    MCMMO("mcMMO", "io.lumine.mythic.lib.rpg.compat.McMMOHook"),
    MCRPG("McRPG", "io.lumine.mythic.lib.rpg.compat.McRPGHook"),
    AURELIUMSKILLS("AureliumSkills", "io.lumine.mythic.lib.rpg.compat.AureliumSkillsHook"),
    AURASKILLS("AuraSkills", "io.lumine.mythic.lib.rpg.compat.AuraSkillsHook"),
    SKILLS("Skills", "io.lumine.mythic.lib.rpg.compat.SkillsHook"),
    SKILLSPRO("SkillsPro", "io.lumine.mythic.lib.rpg.compat.SkillsProHook"),

    ;

    private final Supplier<Class<?>> pluginClass;
    private final String pluginName;

    RPGPluginEnum(Class<?> pluginClass) {
        this.pluginClass = () -> pluginClass;
        this.pluginName = null;
    }

    RPGPluginEnum(String pluginName, String className) {
        this.pluginClass = () -> locateClass(className);
        this.pluginName = pluginName;
    }

    private static Class<?> locateClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException exception) {
            throw new Error("Class not found", exception);
        }
    }

    @NotNull
    public Object instantiateHook() throws LinkageError, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (pluginName != null) Objects.requireNonNull(Bukkit.getPluginManager().getPlugin(pluginName), "Plugin " + pluginName + " is not installed");
        return pluginClass.get().getDeclaredConstructor().newInstance();
    }
}
