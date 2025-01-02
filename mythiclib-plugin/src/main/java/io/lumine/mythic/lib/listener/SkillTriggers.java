package io.lumine.mythic.lib.listener;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.event.AttackEvent;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.api.event.PlayerKillEntityEvent;
import io.lumine.mythic.lib.api.event.armorequip.ArmorEquipEvent;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.comp.profile.ProfileMode;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.ProjectileAttackMetadata;
import io.lumine.mythic.lib.entity.ProjectileMetadata;
import io.lumine.mythic.lib.entity.ProjectileType;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.player.skill.PassiveSkill;
import io.lumine.mythic.lib.skill.trigger.TriggerMetadata;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class SkillTriggers implements Listener {
    public SkillTriggers() {
        Bukkit.getScheduler().runTaskTimer(MythicLib.plugin, () -> MMOPlayerData.forEachOnline(online -> online.getPassiveSkillMap().tickTimerSkills()), 0, 1);
    }

    @EventHandler
    public void killEntity(PlayerKillEntityEvent event) {
        final PlayerMetadata killer = (PlayerMetadata) event.getAttack().getAttacker();
        TriggerMetadata triggerMetadata = new TriggerMetadata(killer, TriggerType.KILL_ENTITY, event.getTarget(), null);
        final Collection<PassiveSkill> isolatedSkills = getSkills(triggerMetadata, killer, event.getAttack());
        event.getData().triggerSkills(triggerMetadata, isolatedSkills);
        if (event.getTarget() instanceof Player) {
            triggerMetadata = new TriggerMetadata(killer, TriggerType.KILL_PLAYER, event.getTarget(), null);
            event.getData().triggerSkills(triggerMetadata, isolatedSkills);
        }
    }

    @NotNull
    private Collection<PassiveSkill> getSkills(@NotNull TriggerMetadata triggerMeta,
                                               @NotNull PlayerMetadata damager,
                                               @NotNull AttackMetadata attack) {

        // Skills for projectile attacks
        if (attack instanceof ProjectileAttackMetadata
                && ((ProjectileAttackMetadata) attack).getProjectileMetadata() != null)
            return ((ProjectileAttackMetadata) attack).getProjectileMetadata().getEffectiveSkills();

        // Skills for melee attacks
        return damager.getData().isolateSkills(triggerMeta);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void attack(PlayerAttackEvent event) {
        final TriggerMetadata triggerMetadata = new TriggerMetadata(event, TriggerType.ATTACK);
        final Collection<PassiveSkill> skills = getSkills(triggerMetadata, event.getAttacker(), event.getAttack());
        event.getAttacker().getData().triggerSkills(triggerMetadata, skills);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void attack(AttackEvent event) {
        final MMOPlayerData caster;
        if (!(event.getEntity() instanceof Player) || (caster = MMOPlayerData.online((Player) event.getEntity())) == null)
            return;

        TriggerMetadata triggerMetadata = new TriggerMetadata(caster, TriggerType.DAMAGED, EquipmentSlot.MAIN_HAND, null, null, null, event.getAttack(), null);
        caster.triggerSkills(triggerMetadata);
        if (event.getAttack().hasAttacker()) {
            triggerMetadata = new TriggerMetadata(caster, TriggerType.DAMAGED_BY_ENTITY, EquipmentSlot.MAIN_HAND, null, event.getAttack().getAttacker().getEntity(), null, event.getAttack(), null);
            caster.triggerSkills(triggerMetadata);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void death(PlayerDeathEvent event) {
        final MMOPlayerData caster;
        if ((caster = MMOPlayerData.online(event.getEntity())) != null)
            // Check if caster is online as DeluxeCombat calls this event while the player has already logged off
            caster.triggerSkills(new TriggerMetadata(caster, TriggerType.DEATH));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void login(PlayerJoinEvent event) {
        // Most likely useless as data is loaded async after join event.
        if (MythicLib.plugin.getProfileMode() == ProfileMode.LEGACY) return;

        final MMOPlayerData caster = MMOPlayerData.get(event.getPlayer());
        caster.triggerSkills(new TriggerMetadata(caster, TriggerType.LOGIN));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void shootBow(EntityShootBowEvent event) {
        final MMOPlayerData caster;
        if (event.getEntity() instanceof Player && (caster = MMOPlayerData.online((Player) event.getEntity())) != null) {
            final EquipmentSlot actionHand = getShootHand(((Player) event.getEntity()).getInventory());

            // Register a runnable to trigger projectile skills
            final ProjectileMetadata proj = ProjectileMetadata.create(caster, actionHand, ProjectileType.ARROW, event.getProjectile());

            // Cast on-shoot skills
            caster.triggerSkills(new TriggerMetadata(caster, TriggerType.SHOOT_BOW, actionHand, null, event.getProjectile(), null, null, proj.getShooter()));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void shootTrident(ProjectileLaunchEvent event) {
        final MMOPlayerData caster;
        if (event.getEntity() instanceof Trident && event.getEntity().getShooter() instanceof Player && (caster = MMOPlayerData.online((Player) event.getEntity().getShooter())) != null) {
            final Player shooter = (Player) event.getEntity().getShooter();
            final EquipmentSlot actionHand = getShootHand(shooter.getInventory());

            // Register a runnable to trigger projectile skills
            final ProjectileMetadata proj = ProjectileMetadata.create(caster, actionHand, ProjectileType.TRIDENT, event.getEntity());

            // Cast on-shoot skills
            caster.triggerSkills(new TriggerMetadata(caster, TriggerType.SHOOT_TRIDENT, actionHand, null, event.getEntity(), null, null, proj.getShooter()));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void sneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) return;

        final MMOPlayerData caster = MMOPlayerData.get(event.getPlayer());
        caster.triggerSkills(new TriggerMetadata(caster, TriggerType.SNEAK));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void placeBlock(BlockPlaceEvent event) {
        final MMOPlayerData caster = MMOPlayerData.get(event.getPlayer());
        caster.triggerSkills(new TriggerMetadata(caster, TriggerType.PLACE_BLOCK, event.getBlock().getLocation().add(.5, .5, .5)));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void breakBlock(BlockBreakEvent event) {
        final MMOPlayerData caster = MMOPlayerData.get(event.getPlayer());
        caster.triggerSkills(new TriggerMetadata(caster, TriggerType.BREAK_BLOCK, event.getBlock().getLocation().add(.5, .5, .5)));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void dropItem(PlayerDropItemEvent event) {
        final MMOPlayerData caster = MMOPlayerData.get(event.getPlayer());
        final boolean sneaking = event.getPlayer().isSneaking() && !MythicLib.plugin.getMMOConfig().ignoreShiftTriggers;
        caster.triggerSkills(new TriggerMetadata(caster, sneaking ? TriggerType.SHIFT_DROP_ITEM : TriggerType.DROP_ITEM, event.getItemDrop()));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void teleport(PlayerTeleportEvent event) {
        final MMOPlayerData caster = MMOPlayerData.online(event.getPlayer());
        if (caster == null) return;
        caster.triggerSkills(new TriggerMetadata(caster, TriggerType.TELEPORT, event.getFrom(), event.getTo()));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void swapItems(PlayerSwapHandItemsEvent event) {
        final MMOPlayerData caster = MMOPlayerData.get(event.getPlayer());
        final boolean sneaking = event.getPlayer().isSneaking() && !MythicLib.plugin.getMMOConfig().ignoreShiftTriggers;
        caster.triggerSkills(new TriggerMetadata(caster, sneaking ? TriggerType.SHIFT_SWAP_ITEMS : TriggerType.SWAP_ITEMS));
    }

    /**
     * Uses {@link MMOPlayerData#online(Player)} to support combat log plugins
     * un-equipping items after the player has logged out.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void equipArmor(ArmorEquipEvent event) {
        final MMOPlayerData caster = MMOPlayerData.online(event.getPlayer());
        if (caster == null) return;
        final boolean unequip = UtilityMethods.isAir(event.getNewArmorPiece());
        caster.triggerSkills(new TriggerMetadata(caster, unequip ? TriggerType.UNEQUIP_ARMOR : TriggerType.EQUIP_ARMOR));
    }

    /**
     * @implNote {@link Cancellable#isCancelled()} does not work with PlayerInteractEvent
     * because there are now two possible ways to cancel the event, either
     * by canceling the item interaction, either by canceling the block interaction.
     * <p>
     * Checking if the event is cancelled points towards the block interaction
     * and not the item interaction which is NOT what MythicLib is interested in
     * @implNote Scrap this, it's 100% useless to check if the event is cancelled.
     * It makes sense to trigger skills even if the item or block interactions are canceled
     * @implNote Event priority set to {@link EventPriority#LOW} because MI consumes consumables on
     * priority NORMAL and item abilities require the held item not to be null in hand
     */
    @EventHandler(priority = EventPriority.LOW)
    public void click(PlayerInteractEvent event) {
        // || event.useItemInHand() == Event.Result.DENY
        if (event.getAction() == Action.PHYSICAL) return;
        if (MythicLib.plugin.getMMOConfig().ignoreOffhandClickTriggers && event.getHand() == org.bukkit.inventory.EquipmentSlot.OFF_HAND)
            return;

        final MMOPlayerData caster = MMOPlayerData.get(event.getPlayer());
        final boolean left = event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK;
        final boolean sneaking = event.getPlayer().isSneaking() && !MythicLib.plugin.getMMOConfig().ignoreShiftTriggers;
        final TriggerType triggerType = sneaking ? (left ? TriggerType.SHIFT_LEFT_CLICK : TriggerType.SHIFT_RIGHT_CLICK) : (left ? TriggerType.LEFT_CLICK : TriggerType.RIGHT_CLICK);
        final TriggerMetadata triggerMetadata = new TriggerMetadata(caster, triggerType, EquipmentSlot.fromBukkit(event.getHand()), null, null, null, null, null);
        caster.triggerSkills(triggerMetadata);
    }

    /**
     * @return Hand used to shoot a projectile (arrow/trident) based on
     * what items the player is holding in his two hands
     */
    @NotNull
    private EquipmentSlot getShootHand(@NotNull PlayerInventory inv) {
        final ItemStack main = inv.getItemInMainHand();
        return main != null && isShootable(main.getType()) ? EquipmentSlot.MAIN_HAND : EquipmentSlot.OFF_HAND;
    }

    private boolean isShootable(@NotNull Material material) {
        return material == Material.BOW || material == Material.CROSSBOW || material == Material.TRIDENT;
    }
}
