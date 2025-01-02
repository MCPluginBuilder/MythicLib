package io.lumine.mythic.lib.element;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.skill.SimpleSkill;
import io.lumine.mythic.lib.skill.Skill;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

public class Element {
    private final String id, name, loreIcon, color;
    private final Material icon;
    private final SkillHandler<?> criticalStrike, regularAttack;

    private static final String DEFAULT_LORE_ICON = "❖";
    private static final String DEFAULT_MATERIAL = "DIRT";
    private static final String DEFAULT_COLOR = "&f";

    public Element(ConfigurationSection config) {
        Validate.notNull(config, "Config cannot be null");

        this.id = UtilityMethods.enumName(config.getName());
        this.icon = Material.valueOf(UtilityMethods.enumName(config.getString("icon", DEFAULT_MATERIAL)));
        this.name = Objects.requireNonNull(config.getString("name"), "Please specify an element name");
        this.loreIcon = config.getString("lore-icon", DEFAULT_LORE_ICON);
        this.color = config.getString("color", DEFAULT_COLOR);
        this.regularAttack = MythicLib.plugin.getSkills().loadSkillHandler(Objects.requireNonNull(config.get("regular-attack"), "Could not find skill for regular attacks"));
        this.criticalStrike = config.contains("crit-strike") ? MythicLib.plugin.getSkills().loadSkillHandler(config.get("crit-strike")) : null;
    }

    public String getId() {
        return UtilityMethods.enumName(id);
    }

    public String getName() {
        return name;
    }

    // Useful for MMOItems
    public Material getIcon() {
        return icon;
    }

    public String getLoreIcon() {
        return loreIcon;
    }

    public String getColor() {
        return color;
    }

    @NotNull
    public Skill getSkill(boolean criticalStrike) {
        final SkillHandler<?> handler = criticalStrike && this.criticalStrike != null ? this.criticalStrike : regularAttack;
        return new SimpleSkill(TriggerType.API, handler);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Element element = (Element) o;
        return id.equals(element.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static Collection<Element> values() {
        return MythicLib.plugin.getElements().getAll();
    }

    /**
     * @param id Element identifier
     * @return Element with given identifier
     * @throws NullPointerException If no element was found with given ID
     */
    @NotNull
    public static Element valueOf(String id) {
        return MythicLib.plugin.getElements().get(id);
    }
}
