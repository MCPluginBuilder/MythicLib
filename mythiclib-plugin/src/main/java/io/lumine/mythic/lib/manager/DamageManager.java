package io.lumine.mythic.lib.manager;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.event.AttackUnregisteredEvent;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.stat.provider.StatProvider;
import io.lumine.mythic.lib.damage.*;
import io.lumine.mythic.lib.entity.ProjectileMetadata;
import io.lumine.mythic.lib.module.MMOPlugin;
import io.lumine.mythic.lib.module.Module;
import io.lumine.mythic.lib.module.ModuleInfo;
import io.lumine.mythic.lib.module.ModuleListener;
import io.lumine.mythic.lib.util.lang3.Validate;
import io.lumine.mythic.lib.version.Attributes;
import io.lumine.mythic.lib.version.VersionUtils;
import io.lumine.mythic.lib.version.wrapper.VersionWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;

/**
 * Central piece of the MythicLib damage system.
 *
 * @author jules
 */
@ModuleInfo(key = "damage")
public class DamageManager extends Module {

    /**
     * External attack handlers
     */
    private final List<AttackHandler> handlers = new ArrayList<>();

    /**
     * There is an issue with metadata not being garbage-collected on mobs.
     * It looks like persistent data containers do also suffer from that issue.
     * <p>
     * I switched back to using a weak hash map to save the current attack
     * metadata for a mob. Weak hash maps are great for garbage collection.
     */
    private final Map<UUID, AttackMetadata> attackMetadatas = new WeakHashMap<>();

    @ModuleListener
    @SuppressWarnings("unused")
    final Listener mainListener = new InternalListener();

    private AttributeModifier noKnockbackModifier;

    public DamageManager(MMOPlugin plugin) {
        super(plugin);
    }

    @Override
    protected void onStartup() {
        // Not on constructor, server version is not available yet
        noKnockbackModifier = VersionUtils.attrMod(new NamespacedKey(MythicLib.plugin, "no_knockback"), 100, AttributeModifier.Operation.ADD_NUMBER);
    }

    /**
     * Attack handlers are used by MythicLib to keep track of details of every
     * attack so that it can apply damage based stats like PvE damage, Magic
     * Damage...
     *
     * @param handler Damage handler being registered
     */
    public void registerHandler(AttackHandler handler) {
        Validate.notNull(handler, "Damage handler cannot be null");

        handlers.add(handler);
    }

    @NotNull
    public List<AttackHandler> getHandlers() {
        return handlers;
    }

    /**
     * Forces a player to damage an entity with knockback
     *
     * @param metadata The class containing all info about the current attack
     * @return If the attack was successful
     */
    public boolean registerAttack(@NotNull AttackMetadata metadata) {
        return registerAttack(metadata, true, false);
    }

    /**
     * Forces a player to damage an entity with (no) knockback
     *
     * @param metadata  The class containing all info about the current attack
     * @param knockback If the attack should deal knockback
     * @return If the attack was successful
     */
    public boolean registerAttack(@NotNull AttackMetadata metadata, boolean knockback) {
        return registerAttack(metadata, knockback, false);
    }

    /**
     * Deals damage to an entity. Does not do anything if the
     * damage is negative or null.
     *
     * @param attack         The class containing all info about the current attack
     * @param knockback      If the attack should deal knockback
     * @param ignoreImmunity The attack will not produce immunity frames.
     * @return If the attack was successful
     */
    public boolean registerAttack(@NotNull AttackMetadata attack, boolean knockback, boolean ignoreImmunity) {
        Validate.notNull(attack.getTarget(), "Target cannot be null"); // BW compatibility check

        markAsMetadata(attack);
        var result = applyDamage(attack.getDamage().getDamage(), attack.getTarget(), attack.getAttacker() != null ? attack.getAttacker().getEntity() : null, knockback, ignoreImmunity);
        unmarkAsMetadata(attack.getTarget());
        return result;
    }

    private boolean applyDamage(double damage, @NotNull LivingEntity target, @Nullable LivingEntity damager, boolean knockback, boolean ignoreImmunity) {
        Validate.isTrue(damage > 0, "Damage must be strictly positive");

        final var kbInstance = !knockback ? target.getAttribute(Attributes.KNOCKBACK_RESISTANCE) : null;
        final var noDamageTicks = ignoreImmunity ? target.getNoDamageTicks() : 0;

        // Disable knockback/iframes
        if (kbInstance != null) kbInstance.addModifier(noKnockbackModifier);
        if (ignoreImmunity) target.setNoDamageTicks(0);

        // Apply damage
        // Try/finally to make sure entity attributes are reset in case of fail.
        boolean damageResult = false;
        try {
            damageResult = VersionWrapper.get().damage(target, damage, damager);
        } catch (Exception anyError) {
            MythicLib.plugin.getLogger().log(Level.SEVERE, "Exception caught while damaging entity '" + target.getUniqueId() + "':");
            anyError.printStackTrace();
        }

        // Restore knockback/iframes
        finally {
            if (kbInstance != null) kbInstance.removeModifier(noKnockbackModifier);
            if (ignoreImmunity) target.setNoDamageTicks(noDamageTicks);
        }

        return damageResult;
    }

    /**
     * Very important method. Looks for a RegisteredAttack that would have been registered
     * by other plugins ie MMOItems abilities, or MMOCore skills, or any other plugin.
     * <p>
     * If it can't find any plugin that has registered an attack, it checks if it is simply
     * not just a vanilla attack: projectile or melee attacks. If SO it registers this new
     * attack meta within MythicLib to make sure the same attackMeta is provided later on.
     *
     * @param event The attack event
     * @return Null if MythicLib cannot find the attack source, some attack meta otherwise.
     */
    @NotNull
    public AttackMetadata findAttack(EntityDamageEvent event) {
        Validate.isTrue(event.getEntity() instanceof LivingEntity, "Target entity is not living");
        final LivingEntity entity = (LivingEntity) event.getEntity();

        // MythicLib attack registry
        @Nullable AttackMetadata found = getRegisteredAttackMetadata(entity);
        if (found != null) return found;

        // Attack registries from other plugins
        for (AttackHandler handler : handlers) {
            found = handler.getAttack(event);
            if (found != null) {
                markAsMetadata(found);
                return found;
            }
        }

        // TODO support for Spigot new DamageSource's

        // Attacks with a damager
        if (event instanceof EntityDamageByEntityEvent) {

            /*
             * Handles melee attacks. This is used everytime a player left clicks an entity.
             *
             * The attack damage type can vary depending on the context: if it is a bare-firsts
             * attack, final attack has no WEAPON damage type. If the player is holding any
             * other item, it is considered a WEAPON attack.
             *
             * If MythicLib reaches this portion of the code this means that the attack is with
             * the right hand. Left-hand attacks are handled by specific listeners.
             */
            final Entity damager = ((EntityDamageByEntityEvent) event).getDamager();
            if (damager instanceof LivingEntity) {
                final StatProvider attacker = StatProvider.get((LivingEntity) damager, EquipmentSlot.MAIN_HAND, true);
                final AttackMetadata attackMeta = new MeleeAttackMetadata(new DamageMetadata(event.getDamage(), getVanillaDamageTypes((EntityDamageByEntityEvent) event, EquipmentSlot.MAIN_HAND)), entity, attacker);
                markAsMetadata(attackMeta);
                return attackMeta;
            }

            /*
             * Handles projectile attacks; used everytime when a player shoots a trident,
             * a bow, a crossbow or even eggs and snowballs.
             *
             * Notice this is always the same damage type: WEAPON, PHYSICAL, PROJECTILE
             * which means that if MMOCore has a skill which makes players shoot multiple
             * arrows, MythicLib will use the following lines to monitor the attacks
             * and the skill will apply WEAPON damage.
             *
             * Make sure to check the shooter is not the damaged entity. We don't want
             * players to backstab themselves using projectiles.
             */
            else if (damager instanceof Projectile) {

                // First tries to find original CustomProjectile
                final Projectile projectile = (Projectile) damager;
                final @Nullable ProjectileMetadata projectileData = ProjectileMetadata.get(projectile);
                if (projectileData != null) {
                    final AttackMetadata attackMeta = new ProjectileAttackMetadata(new DamageMetadata(event.getDamage(), projectileData.getDamageTypes()), (LivingEntity) event.getEntity(), projectileData.getShooter(), projectile, projectileData);
                    markAsMetadata(attackMeta);
                    return attackMeta;
                }

                // Try to trace back the player source
                final ProjectileSource source = projectile.getShooter();
                if (source != null && !source.equals(event.getEntity()) && source instanceof LivingEntity) {
                    final StatProvider attacker = StatProvider.get((LivingEntity) source, EquipmentSlot.MAIN_HAND, true);
                    final AttackMetadata attackMeta = new ProjectileAttackMetadata(new DamageMetadata(event.getDamage(), MythicLib.plugin.getMMOConfig().bowAttackTypes), (LivingEntity) event.getEntity(), attacker, projectile);
                    markAsMetadata(attackMeta);
                    return attackMeta;
                }
            }
        }

        // Attacks with NO damager
        final @NotNull AttackMetadata vanillaAttack = new AttackMetadata(new DamageMetadata(event.getDamage(), getVanillaDamageTypes(event.getCause())), entity, null);
        markAsMetadata(vanillaAttack);
        return vanillaAttack;
    }

    /**
     * Registers the attackMetadata inside the entity metadata.
     * This does NOT apply any damage to the target entity.
     *
     * @param attackMeta Attack metadata being registered
     * @return Attack metadata already present on the player, if it's not
     *         the case it's an internal error.
     */
    @Nullable
    public AttackMetadata markAsMetadata(@NotNull AttackMetadata attackMeta) {
        final @Nullable AttackMetadata found = attackMetadatas.put(attackMeta.getTarget().getUniqueId(), attackMeta);
        if (found != null)
            MythicLib.plugin.getLogger().log(Level.WARNING, "Please report this issue to the developer: persistent attack metadata was found.");
        return found;
    }

    /**
     * @param target Target of current attack
     * @return Attack metadata that was found, if any
     */
    @Nullable
    public AttackMetadata unmarkAsMetadata(@NotNull Entity target) {
        return attackMetadatas.remove(target.getUniqueId());
    }

    /**
     * @param cause Cause of the attack
     * @return The damage types of a vanilla attack
     */
    @NotNull
    public List<DamageType> getVanillaDamageTypes(EntityDamageEvent.DamageCause cause) {
        final var mappings = MythicLib.plugin.getMMOConfig().damageCauseMap;
        return mappings.getOrDefault(cause, List.of());
    }

    /**
     * @param event Attack event
     * @param hand  Hand used to perform the attack
     * @return The damage types of a vanilla melee entity attack
     */
    @NotNull
    public List<DamageType> getVanillaDamageTypes(EntityDamageByEntityEvent event, EquipmentSlot hand) {
        Validate.isTrue(event.getDamager() instanceof LivingEntity, "Not an entity attack");
        return getVanillaDamageTypes((LivingEntity) event.getDamager(), event.getCause(), hand);
    }

    /**
     * @param damager Entity attacking
     * @param cause   Cause of the attack
     * @param hand    Hand used to perform the attack
     * @return The damage types of a vanilla melee entity attack
     */
    @NotNull
    public List<DamageType> getVanillaDamageTypes(@NotNull LivingEntity damager, @NotNull EntityDamageEvent.DamageCause cause, @NotNull EquipmentSlot hand) {

        // Not an entity attack
        if (cause != EntityDamageEvent.DamageCause.ENTITY_ATTACK && cause != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)
            return MythicLib.plugin.getMMOConfig().meleeRandomAttackTypes;

        // Physical attack with bare fists
        // Unarmed attack, allows for interesting gameplay mechanics
        final @Nullable var handItem = UtilityMethods.getHandItem(damager, hand);
        if (isAir(handItem))
            return MythicLib.plugin.getMMOConfig().meleeUnarmedAttackTypes;

        // Weapon attack, default attack damage types
        // Replaced later on by MMOItems on PlayerAttackEvent
        if (isWeapon(handItem.getType()))
            return MythicLib.plugin.getMMOConfig().meleeWeaponAttackTypes;

        // Hitting with a random item
        return MythicLib.plugin.getMMOConfig().meleeRandomAttackTypes;
    }

    @Nullable
    public AttackMetadata getRegisteredAttackMetadata(Entity entity) {
        return attackMetadatas.get(entity.getUniqueId());
    }

    private class InternalListener implements Listener {

        /**
         * This method is used to unregister MythicLib custom damage after everything
         * was calculated, hence MONITOR priority. As a safe practice, it does NOT
         * ignore cancelled damage events. It does however ignore fake events as they
         * are sometimes called for checking interaction rules after the metadata
         * has been set, but before effects are being applied which can screw things up.
         * <p>
         * This method is ABSOLUTELY NECESSARY. While MythicLib does clean up the
         * entity metadata as soon as damage is dealt, vanilla attacks and extra
         * plugins just don't.
         */
        @EventHandler(priority = EventPriority.MONITOR)
        public void unregisterCustomAttacks(EntityDamageEvent event) {
            if (UtilityMethods.isFake(event)) return;

            if (event.getEntity() instanceof LivingEntity) {
                final @Nullable AttackMetadata attack = unmarkAsMetadata(event.getEntity());
                if (attack != null && !event.isCancelled() && event.getFinalDamage() > 0)
                    Bukkit.getPluginManager().callEvent(new AttackUnregisteredEvent(event, attack));
            }
        }
    }

    /**
     * Purely arbitrary but works decently
     */
    private boolean isWeapon(@NotNull Material mat) {
        return mat.getMaxDurability() > 0;
    }

    private boolean isAir(@Nullable ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }

    //region Deprecated

    @Deprecated
    public DamageType[] getVanillaDamageTypes(EntityDamageEvent event) {
        return getVanillaDamageTypes(event.getCause()).toArray(new DamageType[0]);
    }

    @Deprecated
    public void damage(@NotNull AttackMetadata metadata, @NotNull LivingEntity target) {
        damage(metadata, target, true);
    }

    @Deprecated
    public void damage(@NotNull AttackMetadata metadata, @NotNull LivingEntity target, boolean knockback) {
        damage(metadata, target, knockback, false);
    }

    @Deprecated
    public void damage(@NotNull AttackMetadata metadata, @NotNull LivingEntity target, boolean knockback, boolean ignoreImmunity) {
        registerAttack(new AttackMetadata(metadata.getDamage(), target, metadata.getAttacker()), knockback, ignoreImmunity);
    }

    /**
     * This method draws an interface between MythicLib damage mitigation system
     * and Bukkit damage events.
     * <p>
     * In the worst case (unknown damage cause/damage not logged by any plugin) scenario,
     * it just returns a damage metadata with no damage type which is completely fine.
     *
     * @param event The damage event
     * @return The corresponding MythicLib damage metadata.
     * @deprecated Use {@link #findAttack(EntityDamageEvent)} which provides more information
     */
    @NotNull
    @Deprecated
    public DamageMetadata findDamage(EntityDamageEvent event) {
        return findAttack(event).getDamage();
    }

    @Deprecated
    public void unmarkAsMetadata(@NotNull AttackMetadata attackMetadata) {
        unmarkAsMetadata(attackMetadata.getTarget());
    }

    //endregion
}
