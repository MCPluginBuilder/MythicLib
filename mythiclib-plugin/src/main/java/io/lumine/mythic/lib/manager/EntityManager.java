package io.lumine.mythic.lib.manager;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.event.fake.DamageCheckEvent;
import io.lumine.mythic.lib.comp.interaction.InteractionType;
import io.lumine.mythic.lib.comp.interaction.TargetRestriction;
import io.lumine.mythic.lib.comp.interaction.relation.Relationship;
import io.lumine.mythic.lib.comp.interaction.relation.RelationshipHandler;
import io.lumine.mythic.lib.entity.ProjectileMetadata;
import io.lumine.mythic.lib.module.GeneralManager;
import io.lumine.mythic.lib.module.ModuleInfo;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

@ModuleInfo(key = "entities", load = false)
public class EntityManager extends GeneralManager {
    private final Set<TargetRestriction> restrictions = new HashSet<>();
    private final Set<RelationshipHandler> relHandlers = new HashSet<>();

    public EntityManager() {
        super(MythicLib.plugin);
    }

    /**
     * See {@link TargetRestriction} for more information. This should be
     * called as soon as MythicLib enables by plugins implementing player sets
     * like parties, friends, factions.... any set that could support friendly fire.
     * <p>
     * This is also helpful to prevent players from interacting with
     * specific invulnerable entities like NPCs
     *
     * @param restriction New restriction for entities
     */
    public void registerRestriction(TargetRestriction restriction) {
        restrictions.add(restriction);
    }

    /**
     * Plugins which create player groups create relations between
     * players. Depending on the type of relationship between players,
     * two players may or may not be able to pvp/cast spells onto each other.
     *
     * @param relationHandler New handler for player relations
     * @see {@link RelationshipHandler}
     */
    public void registerRelationHandler(RelationshipHandler relationHandler) {
        relHandlers.add(relationHandler);
    }

    public Set<RelationshipHandler> getRelationHandlers() {
        return relHandlers;
    }

    /**
     * @see {@link ProjectileMetadata#get(Entity)}
     * @deprecated
     */
    @Nullable
    @Deprecated
    public ProjectileMetadata getCustomProjectile(Entity entity) {
        return ProjectileMetadata.get(entity);
    }

    /**
     * Registers a custom projectile. This is used for bows, crossbows and tridents.
     *
     * @deprecated Automatically registers on class instanciation
     */
    @Deprecated
    public void registerCustomProjectile(Entity entity, ProjectileMetadata projectileData) {
    }

    @Deprecated
    public boolean canTarget(@NotNull Player source, @NotNull Entity target, @NotNull InteractionType interactionType) {
        return canInteract(source, target, interactionType);
    }

    /**
     * Called whenever a player tries to damage OR buff an entity.
     * <p>
     * This should be used by:
     * - plugins which implement friendly fire player sets like parties, guilds, nations, factions....
     * - plugins which implement custom invulnerable entities like NPCs, sentinels....
     *
     * @param source          Player targeting another entity
     * @param target          Entity being targeted
     * @param interactionType Type of interaction, whether it's positive (buff, heal) or negative (offense skill, attack)
     * @return If false, any interaction should be cancelled!
     */
    public boolean canInteract(@NotNull Player source, @NotNull Entity target, @NotNull InteractionType interactionType) {

        // Simple checks
        if (source.equals(target) || target.isDead() || !(target instanceof LivingEntity) || target instanceof ArmorStand)
            return false;

        // Specific plugin checks (Not used anymore)
        final LivingEntity livingTarget = (LivingEntity) target;
        for (TargetRestriction restriction : restrictions)
            if (!restriction.canTarget(source, livingTarget, interactionType))
                return false;

        // Pvp Interaction Rules
        if (target instanceof Player) {

            // PvP value check
            boolean pvpEnabled = target.getWorld().getPVP();
            if (pvpEnabled) {
                final DamageCheckEvent damageCheckEvent = new DamageCheckEvent(source, target, interactionType);
                Bukkit.getPluginManager().callEvent(damageCheckEvent);
                pvpEnabled = !damageCheckEvent.isCancelled();
            }

            // If offense, just cancel if PvP is disabled
            if (interactionType.isOffense() && !pvpEnabled)
                return false;

            // Otherwise check rules
            if (!checkPvpInteractionRules(source, (Player) target, interactionType, pvpEnabled))
                return false;
        }

        // PvE Interaction Rules
        else if (!interactionType.isOffense() && !MythicLib.plugin.getMMOConfig().interactionRules.supportSkillsOnMobs)
            return false;

        return true;
    }

    /**
     * If a player can interact with another player
     *
     * @param source          First player
     * @param target          Target player
     * @param interactionType Type of interaction. What matters here is if it's an offense or friendly action.
     * @param pvpEnabled      Is PvP enabled between the two players. It should be computed in advance
     * @return If the two players can interact
     */
    public boolean checkPvpInteractionRules(@NotNull Player source, @NotNull Player target, @NotNull InteractionType interactionType, @NotNull boolean pvpEnabled) {

        // Self harm
        if (source.equals(target))
            return MythicLib.plugin.getMMOConfig().interactionRules.isEnabled(pvpEnabled, interactionType, Relationship.SELF);

        // Relations from other plugins
        for (RelationshipHandler relHandler : relHandlers)
            if (!MythicLib.plugin.getMMOConfig().interactionRules.isEnabled(pvpEnabled, interactionType, relHandler.getRelationship(source, target)))
                return false;

        return true;
    }

    @Deprecated
    public void unregisterCustomProjectile(Projectile projectile) {
        projectile.removeMetadata(ProjectileMetadata.METADATA_KEY, MythicLib.plugin);
    }
}
