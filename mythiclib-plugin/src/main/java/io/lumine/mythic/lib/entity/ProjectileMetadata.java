package io.lumine.mythic.lib.entity;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.util.TemporaryListener;
import io.lumine.mythic.lib.damage.ProjectileAttackMetadata;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.player.skill.PassiveSkill;
import io.lumine.mythic.lib.skill.trigger.TriggerMetadata;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The one thing about projectiles is that they create some type of
 * delay between the attack action (when shooting the trident or
 * the arrow) and the actual damage. This means the player stats
 * and abilities need to be cached on item use.
 * <p>
 * This class triggers the right skills when a projectile flies into
 * the air/hits the ground or an entity.
 * <p>
 * This is used for any type of projectile: arrows, tridents, eggs,
 * snowballs.
 *
 * @author indyuce
 */
public class ProjectileMetadata extends TemporaryListener {
    private final int entityId;
    private final ProjectileType projectileType;

    /**
     * Used to cache the caster skills. If the skills are not cached, the player skill
     * list can actually be edited (hand swapping or external plugins) while the projectile
     * is still in midair which will change the projectile behaviour. The very same
     * glitch is being fixed by {@link PlayerMetadata}
     */
    private final List<PassiveSkill> cachedSkills;
    private final PlayerMetadata shooter;

    @Nullable
    private NBTItem sourceItem;

    /**
     * When toggled on, this flag indicates that MythicLib should apply
     * attack damage amount read from the shooter metadata if the projectile
     * finds a target.
     */
    private boolean customDamage;

    /**
     * Can be modified by external plugins.
     */
    private double damageMultiplier = 1;

    public static final String METADATA_KEY = "MythicLibProjectileMetadata";
    private static final HandlerList[] HANDLER_LISTS = inferHandlerLists(ProjectileMetadata.class);

    /**
     * Used to keep track of custom MythicLib projectiles. This class handles:
     * - custom projectile damage (bows from MMOItems for instance)
     * - ability triggering (shoot, tick, hit, land)
     *
     * @param shooter        Player performing the shoot
     * @param projectileType Type of projectile being fired
     * @param projectile     Projectile being fired
     */
    private ProjectileMetadata(@NotNull PlayerMetadata shooter, @NotNull ProjectileType projectileType, @NotNull Entity projectile) {
        super(HANDLER_LISTS);

        this.entityId = projectile.getEntityId();
        this.projectileType = projectileType;

        // Cache important stuff
        this.shooter = shooter;
        this.cachedSkills = shooter.getData().getPassiveSkillMap().isolateModifiers(shooter.getActionHand());

        // Trigger skills
        registerRunnable(new BukkitRunnable() {
            final TriggerMetadata tickTriggerMetadata = new TriggerMetadata(shooter, projectileType.getTickTrigger(), projectile, null);

            @Override
            public void run() {
                shooter.getData().triggerSkills(tickTriggerMetadata, cachedSkills);
            }
        }, runnable -> runnable.runTaskTimer(MythicLib.plugin, 0, 1));

        // Register
        projectile.setMetadata(METADATA_KEY, new FixedMetadataValue(MythicLib.plugin, this));
    }

    @NotNull
    public PlayerMetadata getShooter() {
        return shooter;
    }

    @Nullable
    public NBTItem getSourceItem() {
        return sourceItem;
    }

    public void setSourceItem(@Nullable NBTItem sourceItem) {
        this.sourceItem = sourceItem;
    }

    @NotNull
    public List<PassiveSkill> getEffectiveSkills() {
        return cachedSkills;
    }

    public boolean isCustomDamage() {
        return customDamage;
    }

    public void setCustomDamage(boolean customDamage) {
        this.customDamage = customDamage;
    }

    public double getDamageMultiplier() {
        return damageMultiplier;
    }

    public void setDamageMultiplier(double damageMultiplier) {
        Validate.isTrue(damageMultiplier >= 0, "Damage multiplier must be positive");
        this.damageMultiplier = damageMultiplier;
    }

    /**
     * Will throw an error if it's not a custom bow
     *
     * @return Damage of custom bow
     */
    public double getDamage() {
        return shooter.getStat("ATTACK_DAMAGE") * damageMultiplier;
    }

    @EventHandler
    public void unregisterOnHit(ProjectileHitEvent event) {
        if (event.getEntity().getEntityId() == entityId)
            // Close with delay to make sure skills are triggered on hit/land
            Bukkit.getScheduler().runTask(MythicLib.plugin, () -> close());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void triggerHit(PlayerAttackEvent event) {
        if (event.getAttack() instanceof ProjectileAttackMetadata && ((ProjectileAttackMetadata) event.getAttack()).getProjectile().getEntityId() == entityId)
            shooter.getData().triggerSkills(new TriggerMetadata(shooter, projectileType.getHitTrigger(), event.getEntity(), null), cachedSkills);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void triggerLand(ProjectileHitEvent event) {

        // Make sure the projectile landed on a block
        if (event.getHitBlock() != null && event.getEntity().getEntityId() == entityId) {
            final TriggerMetadata meta = new TriggerMetadata(shooter, projectileType.getLandTrigger(), event.getEntity(), null);
            shooter.getData().triggerSkills(meta, cachedSkills);
        }
    }

    @EventHandler
    public void unregisterOnDeath(EntityDeathEvent event) {
        if (event.getEntity().getEntityId() == entityId) close();
    }

    @EventHandler
    public void unregisterOnLogout(PlayerQuitEvent event) {
        if (event.getPlayer().getUniqueId().equals(shooter.getData().getUniqueId())) close();
    }

    @Deprecated
    public static ProjectileMetadata getCustomData(Entity proj) {
        return get(proj);
    }

    @Nullable
    public static ProjectileMetadata get(@NotNull Entity projectile) {
        for (MetadataValue mv : projectile.getMetadata(METADATA_KEY))
            if (mv.getOwningPlugin().equals(MythicLib.plugin)) return (ProjectileMetadata) mv.value();
        return null;
    }

    @NotNull
    public static ProjectileMetadata create(@NotNull PlayerMetadata shooter, @NotNull ProjectileType projectileType, @NotNull Entity projectile) {
        final @Nullable ProjectileMetadata found = get(projectile);
        if (found != null) return found;
        return new ProjectileMetadata(shooter, projectileType, projectile);
    }

    @NotNull
    public static ProjectileMetadata create(@NotNull MMOPlayerData data, @NotNull EquipmentSlot actionHand, @NotNull ProjectileType projectileType, @NotNull Entity projectile) {
        final @Nullable ProjectileMetadata found = get(projectile);
        if (found != null) return found;
        return new ProjectileMetadata(data.getStatMap().cache(actionHand), projectileType, projectile);
    }
}
