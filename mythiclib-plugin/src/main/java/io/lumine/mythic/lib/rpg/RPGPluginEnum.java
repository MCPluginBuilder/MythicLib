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
    HEROES("Heroes"),
    FABLED("Fabled"),
    RPGPLAYERLEVELING("RPGPlayerLeveling"),
    RACESANDCLASSES("RacesAndClasses"),
    BATTLELEVELS("BattleLevels"),
    MCMMO("mcMMO"),
    MCRPG("McRPG"),
    AURELIUMSKILLS("AureliumSkills"),
    AURASKILLS("AuraSkills"),
    SKILLS("Skills"),
    SKILLSPRO("SkillsPro"),

    ;

    private final Supplier<Class<?>> pluginClass;
    private final String pluginName;

    RPGPluginEnum(Class<?> pluginClass) {
        this.pluginClass = () -> pluginClass;
        this.pluginName = null;
    }

    RPGPluginEnum(String pluginName) {
        this(pluginName, "io.lumine.mythic.lib.rpg.compat." + pluginName);
    }

    RPGPluginEnum(String pluginName, String classPath) {
        this.pluginClass = () -> locateClass(classPath);
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
