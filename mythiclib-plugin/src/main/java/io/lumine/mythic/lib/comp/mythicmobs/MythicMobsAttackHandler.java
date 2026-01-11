package io.lumine.mythic.lib.comp.mythicmobs;

import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.stat.provider.StatProvider;
import io.lumine.mythic.lib.comp.mythicmobs.mechanic.SetMMODamageSplits;
import io.lumine.mythic.lib.damage.AttackHandler;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * This class is the interface between MythicMobs -> MMO damage systems.
 * <p>
 * The user is strongly advised to use the mmodamage mechanic instead:
 * - No player stat caching (fine most of the time)
 * - Bare bones damage type/element support
 *
 * @author jules
 */
public class MythicMobsAttackHandler implements AttackHandler {

    @Override
    @Nullable
    public AttackMetadata getAttack(EntityDamageEvent event) {

        // Retrieve damage info
        final var mythicEntity = BukkitAdapter.adapt(event.getEntity());
        final var mythicMetaOpt = mythicEntity.getMetadata("skill-damage");
        if (mythicMetaOpt.isEmpty()) return fallbackDefault(event);

        final var mythic = (io.lumine.mythic.api.skills.damage.DamageMetadata) mythicMetaOpt.get();

        // Find damager
        final Entity damagerBukkit = mythic.getDamager().getEntity().getBukkitEntity();
        final @Nullable StatProvider damager = damagerBukkit instanceof LivingEntity ? StatProvider.get((LivingEntity) damagerBukkit, EquipmentSlot.MAIN_HAND, true) : null;

        final var damageTypes = MythicLib.plugin.getDamage().getVanillaDamageTypes(mythic.getDamageCause());
        final DamageMetadata damageMeta = new DamageMetadata(mythic.getAmount(), damageTypes);

        // Apply element
        if (mythic.getElement() != null) {
            // TODO fix mythicmobs element correspondence ?! maybe using an explicit config option
            final var element = MythicLib.plugin.getElements().getOrNull(mythic.getElement().toUpperCase());
            if (element != null) damageMeta.getInitialPacket().setElement(element);
        }

        // TODO change to event.getFinalDamage() ?
        return new AttackMetadata(damageMeta, (LivingEntity) event.getEntity(), damager);
    }

    /**
     * Looks inside the entity metadata for a damage split introduced by
     * the MMO mechanic
     *
     * @see SetMMODamageSplits
     */
    @SuppressWarnings("unchecked")
    @Nullable
    private AttackMetadata fallbackDefault(EntityDamageEvent event) {

        if (!(event instanceof EntityDamageByEntityEvent)) return null;

        final var bukkitDamager = ((EntityDamageByEntityEvent) event).getDamager();
        final var mythicDamager = BukkitAdapter.adapt(bukkitDamager);
        final var damager = bukkitDamager instanceof LivingEntity ? StatProvider.get((LivingEntity) bukkitDamager, EquipmentSlot.MAIN_HAND, true) : null;

        // Use default damage types if existing
        final var defaultDamageTypesOpt = mythicDamager.getMetadata(SetMMODamageSplits.METADATA_KEY);
        if (defaultDamageTypesOpt.isEmpty()) return null;

        final var damageSplits = (List<SetMMODamageSplits.Entry>) defaultDamageTypesOpt.get();
        // TODO change to event#getFinalDamage() ?
        final var damageMeta = asDamageMetadata(event.getDamage(), damageSplits);
        return new AttackMetadata(damageMeta, (LivingEntity) event.getEntity(), damager);
    }

    @NotNull
    private DamageMetadata asDamageMetadata(double effectiveDamage, List<SetMMODamageSplits.Entry> entries) {
        DamageMetadata damageMeta = null;

        for (var entry : entries) {
            final var dmg = effectiveDamage * entry.percent;
            if (damageMeta == null) damageMeta = new DamageMetadata(dmg, entry.element, entry.types);
            else damageMeta.add(dmg, entry.element, entry.types);
        }

        return Objects.requireNonNull(damageMeta, "At least one split is required");
    }
}
