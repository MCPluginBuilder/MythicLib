package io.lumine.mythic.lib.comp.mythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.core.logging.MythicLogger;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.damage.DamagingMechanic;
import io.lumine.mythic.core.utils.annotations.MythicMechanic;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.stat.provider.StatProvider;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.element.Element;
import io.lumine.mythic.lib.skill.result.MythicMobsSkillResult;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@MythicMechanic(
        author = "Indyuce",
        name = "mmodamage",
        aliases = {"mmod", "mmodmg"},
        description = "Deals damage to the target (compatible with MMO plugins)"
)
public class MMODamageMechanic extends DamagingMechanic implements ITargetedEntitySkill {
    protected final PlaceholderDouble amount;
    protected final boolean ignoreMMOAttack;

    @Nullable
    private final PlaceholderString damageTypes, elementName;

    public MMODamageMechanic(SkillExecutor manager, File file, String line, MythicLineConfig mlc) {
        super(manager, file, line, mlc);

        this.amount = PlaceholderDouble.of(mlc.getString(new String[]{"amount", "a"}, "1"));
        //this.elementAmount = PlaceholderDouble.of(mlc.getString(new String[]{"amount", "a"}, "1"));
        this.ignoreMMOAttack = mlc.getBoolean(new String[]{"ignoreMMOAttack", "immo", "immoa"}, false);
        this.damageTypes = placeholders(mlc.getString(new String[]{"type", "t", "types"}, null));
        this.elementName = placeholders(mlc.getString(new String[]{"element", "el", "e"}, null));
    }

    @Nullable
    private PlaceholderString placeholders(@Nullable String input) {
        return input == null ? null : PlaceholderString.of(input);
    }

    @NotNull
    private List<DamageType> findDamageTypes(SkillCaster caster) {
        final String format = this.damageTypes.get(caster);
        if (format.isEmpty() || format.equals("NONE")) return List.of();

        final var split = format.split("\\,");
        final var array = new ArrayList<DamageType>(split.length);
        for (var s : split) array.add(DamageType.valueOf(UtilityMethods.enumName(s)));

        return array;
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {

        if (target.isDead() || !(target.getBukkitEntity() instanceof LivingEntity) || data.getCaster().isUsingDamageSkill() || target.getHealth() <= 0)
            return SkillResult.INVALID_TARGET;

        // Find damageMeta
        final double damage = amount.get(data, target) * data.getPower();
        final Element element = elementName == null ? null : MythicLib.plugin.getElements().get(UtilityMethods.enumName(elementName.get(data.getCaster())));
        final var damageTypes = this.damageTypes == null ? List.<DamageType>of() : findDamageTypes(data.getCaster());

        final AttackMetadata currentAttack;
        if (!ignoreMMOAttack && (currentAttack = MythicLib.plugin.getDamage().getRegisteredAttackMetadata(target.getBukkitEntity())) != null) {
            if (element == null) currentAttack.getDamage().add(damage, damageTypes);
            else currentAttack.getDamage().add(damage, element, damageTypes);
            return SkillResult.SUCCESS;
        }

        // Find attacker
        final Entity attackerBukkit = data.getCaster().getEntity().getBukkitEntity();
        @Nullable final StatProvider attacker = data.getVariables().has(MythicMobsSkillResult.MMO_SKILLMETADATA_TAG) ?
                ((io.lumine.mythic.lib.skill.SkillMetadata) data.getVariables().get(MythicMobsSkillResult.MMO_SKILLMETADATA_TAG).get()).getCaster()
                : attackerBukkit instanceof LivingEntity ? StatProvider.get((LivingEntity) attackerBukkit, EquipmentSlot.MAIN_HAND, true) : null;

        // Find damage
        final DamageMetadata damageMeta = element == null ? new DamageMetadata(damage, damageTypes) : new DamageMetadata(damage, element, damageTypes);
        final AttackMetadata attackMeta = new AttackMetadata(damageMeta, (LivingEntity) target.getBukkitEntity(), attacker);

        // Register damage in ML and apply damage
        MythicLib.plugin.getDamage().markAsMetadata(attackMeta);
        try {
            doDamage(data, target, damage);
            MythicLogger.debug(MythicLogger.DebugLevel.MECHANIC, "+ MMODamageMechanic fired for {0} with {1} power", new Object[]{damage, data.getPower()});
        } catch (Exception exception) {
            MythicLib.plugin.getLogger().log(Level.SEVERE, "Caught an exception (4) while damaging entity '" + target.getUniqueId() + "':");
            exception.printStackTrace();
        } finally {
            MythicLib.plugin.getDamage().unmarkAsMetadata(target.getBukkitEntity());
        }

        return SkillResult.SUCCESS;
    }
}
