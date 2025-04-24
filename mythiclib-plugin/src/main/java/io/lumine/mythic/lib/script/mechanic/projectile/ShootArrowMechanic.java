package io.lumine.mythic.lib.script.mechanic.projectile;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.entity.ProjectileMetadata;
import io.lumine.mythic.lib.entity.ProjectileType;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.skill.PassiveSkill;
import io.lumine.mythic.lib.script.Script;
import io.lumine.mythic.lib.script.mechanic.type.DirectionMechanic;
import io.lumine.mythic.lib.skill.SimpleSkill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import io.lumine.mythic.lib.util.DoubleFormula;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ShootArrowMechanic extends DirectionMechanic {
    private final boolean fromItem, playerAttackDamage;
    private final DoubleFormula velocity;

    @Nullable
    private final Script onHit, onLand, onTick;

    public ShootArrowMechanic(ConfigObject config) {
        super(config);

        fromItem = config.getBoolean("from_item", false);
        playerAttackDamage = config.getBoolean("player_attack_damage", false);
        onHit = config.getScriptOrNull("hit");
        onLand = config.getScriptOrNull("land");
        onTick = config.getScriptOrNull("tick");
        velocity = config.getDoubleFormula("velocity", DoubleFormula.constant(1));
    }

    @Override
    public void cast(SkillMetadata meta, Location source, Vector dir) {
        final Arrow arrow = meta.getCaster().getPlayer().launchProjectile(Arrow.class);
        arrow.setVelocity(dir.multiply(velocity.evaluate(meta)));

        // Trigger on-shoot abilities
        meta.getCaster().triggerSkills(TriggerType.SHOOT_BOW, arrow, null);

        final ProjectileMetadata proj = ProjectileMetadata.create(meta.getCaster(), ProjectileType.ARROW, arrow);
        if (fromItem)
            proj.setSourceItem(NBTItem.get(meta.getCaster().getPlayer().getInventory().getItem(meta.getCaster().getActionHand().toBukkit())));
        if (playerAttackDamage) proj.setCustomDamage(true);

        // Register skills
        if (onHit != null) proj.getEffectiveSkills().add(skill(onHit, TriggerType.ARROW_HIT));
        if (onLand != null) proj.getEffectiveSkills().add(skill(onLand, TriggerType.ARROW_LAND));
        if (onTick != null) proj.getEffectiveSkills().add(skill(onTick, TriggerType.ARROW_TICK));
    }

    private static final String PASSIVE_SKILL_KEY = "api";

    @NotNull
    private PassiveSkill skill(Script script, TriggerType triggerType) {
        return new PassiveSkill(PASSIVE_SKILL_KEY, triggerType, new SimpleSkill(script), EquipmentSlot.OTHER, ModifierSource.OTHER);
    }
}
