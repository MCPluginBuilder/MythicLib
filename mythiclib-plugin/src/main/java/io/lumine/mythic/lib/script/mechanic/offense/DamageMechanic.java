package io.lumine.mythic.lib.script.mechanic.offense;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.element.Element;
import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.script.mechanic.type.TargetMechanic;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.DoubleFormula;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@MechanicMetadata
public class DamageMechanic extends TargetMechanic {
    private final DoubleFormula amount;
    private final boolean knockback, ignoreImmunity;
    private final DamageType[] types;

    /**
     * Cannot save the Element object reference since skills
     * load BEFORE elements. This also permits the elements to
     * be modified without having to reload skills which reduces
     * MythicLib module load inter-dependency.
     */
    @Nullable
    private final String elementName;

    public DamageMechanic(ConfigObject config) {
        super(config);

        config.validateKeys("amount");

        amount = new DoubleFormula(config.getString("amount"));
        knockback = config.getBoolean("knockback", true);
        ignoreImmunity = config.getBoolean("ignore_immunity", false);

        // Look for damage type
        if (config.contains("damage_type")) {
            final String[] split = config.getString("damage_type").split("\\,");
            types = new DamageType[split.length];
            for (int i = 0; i < split.length; i++)
                types[i] = DamageType.valueOf(UtilityMethods.enumName(split[i]));
        }

        // By default, magical-skill damage
        else
            types = new DamageType[]{DamageType.MAGIC, DamageType.SKILL};

        // Elemental attack?
        elementName = config.contains("element") ? UtilityMethods.enumName(config.getString("element")) : null;
    }

    @Override
    public void cast(SkillMetadata meta, @NotNull Entity target) {
        Validate.isTrue(target instanceof LivingEntity, "Cannot damage a non living entity");
        final Element element = elementName == null ? null : MythicLib.plugin.getElements().get(elementName);

        // Look for attackMetadata
        final @Nullable AttackMetadata opt = MythicLib.plugin.getDamage().getRegisteredAttackMetadata(target);
        if (opt != null) {
            opt.getDamage().add(amount.evaluate(meta), element, types);
            return;
        }

        final DamageMetadata damageMetadata = element == null ? new DamageMetadata(amount.evaluate(meta), types) : new DamageMetadata(amount.evaluate(meta), element, types);
        final AttackMetadata attackMetadata = new AttackMetadata(damageMetadata, (LivingEntity) target, meta.getCaster());
        MythicLib.plugin.getDamage().registerAttack(attackMetadata, knockback, ignoreImmunity);
    }
}
