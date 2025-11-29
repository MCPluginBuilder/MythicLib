package io.lumine.mythic.lib.listener;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.event.AttackEvent;
import io.lumine.mythic.lib.api.stat.provider.StatProvider;
import io.lumine.mythic.lib.module.Module;
import io.lumine.mythic.lib.module.ModuleInfo;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

@ModuleInfo(key = "mitigation")
public class MitigationMechanics  {
    /*
    private static final Random RANDOM = new Random();
    private static final List<EntityDamageEvent.DamageCause> MITIGATION_CAUSES = Arrays.asList(EntityDamageEvent.DamageCause.PROJECTILE, EntityDamageEvent.DamageCause.ENTITY_ATTACK, EntityDamageEvent.DamageCause.ENTITY_EXPLOSION, EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK);

    // Mitigation configs
    private double dodgeKnockback, parryKnockback, parryDefaultCooldown, blockDefaultCooldown, dodgeDefaultCooldown;

    // Mitigation chat messages
    private PlayerMessage parryMessage, blockMessage, dodgeMessage;

    public MitigationMechanics(MMOPluginImpl plugin) {
        super(plugin);
    }

    @Override
    public void onEnable() {
        ConfigurationSection config = MythicLib.plugin.getConfig().getConfigurationSection("mitigation");

        dodgeKnockback = config.getDouble("dodge.knockback");
        parryKnockback = config.getDouble("parry.knockback");

        parryDefaultCooldown = config.getDouble("parry.cooldown");
        blockDefaultCooldown = config.getDouble("block.cooldown");
        dodgeDefaultCooldown = config.getDouble("dodge.cooldown");

        parryMessage = PlayerMessage.fromConfig(config.get("parry.message"));
        dodgeMessage = PlayerMessage.fromConfig(config.get("dodge.message"));
        blockMessage = PlayerMessage.fromConfig(config.get("block.message"));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void applyMitigation(AttackEvent event) {
        if (!MITIGATION_CAUSES.contains(event.toBukkit().getCause())) return;

        final MMOPlayerData playerData = MMOPlayerData.getOrNull(event.getEntity());
        if (playerData == null) return;

        final Player player = (Player) event.getEntity();
        final StatMap stats = playerData.getStatMap();

        // Dodging
        double dodgeRating = stats.getStat("DODGE_RATING") / 100;
        if (RANDOM.nextDouble() < dodgeRating && !playerData.getCooldownMap().isOnCooldown(CooldownType.DODGE)) {

            PlayerDodgeEvent mitigationEvent = new PlayerDodgeEvent(playerData, event.toBukkit());
            Bukkit.getPluginManager().callEvent(mitigationEvent);
            if (mitigationEvent.isCancelled())
                return;

            this.dodgeMessage.send(playerData, "damage", MythicLib.plugin.getMMOConfig().decimal.format(event.getDamage().getDamage()));
            playerData.getCooldownMap().applyCooldown(CooldownType.DODGE, calculateCooldown(dodgeDefaultCooldown, stats.getStat("DODGE_COOLDOWN_REDUCTION")));
            event.setCancelled(true);
            player.setNoDamageTicks(10);
            player.getWorld().playSound(player.getLocation(), Sounds.ENTITY_ENDER_DRAGON_FLAP, 2, 1);
            player.getWorld().spawnParticle(VParticle.EXPLOSION.get(), player.getLocation(), 16, 0, 0, 0, .06);
            if (dodgeKnockback > 0)
                player.setVelocity(getVector(player, event).multiply(-.85 * dodgeKnockback).setY(.3));
            return;
        }

        // Parrying
        double parryRating = stats.getStat("PARRY_RATING") / 100;
        if (RANDOM.nextDouble() < parryRating && !playerData.getCooldownMap().isOnCooldown(CooldownType.PARRY)) {

            PlayerParryEvent mitigationEvent = new PlayerParryEvent(playerData, event.toBukkit());
            Bukkit.getPluginManager().callEvent(mitigationEvent);
            if (mitigationEvent.isCancelled())
                return;

            playerData.getCooldownMap().applyCooldown(CooldownType.PARRY, calculateCooldown(parryDefaultCooldown, stats.getStat("PARRY_COOLDOWN_REDUCTION")));
            event.setCancelled(true);
            player.setNoDamageTicks(10);
            this.parryMessage.send(playerData, "damage", MythicLib.plugin.getMMOConfig().decimal.format(event.getDamage().getDamage()));
            player.getWorld().playSound(player.getLocation(), Sounds.ENTITY_ENDER_DRAGON_FLAP, 2, 1);
            player.getWorld().spawnParticle(VParticle.EXPLOSION.get(), player.getLocation(), 16, 0, 0, 0, .06);
            if (parryKnockback > 0 && event.toBukkit() instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) event.toBukkit()).getDamager() instanceof LivingEntity) {
                LivingEntity attacker = (LivingEntity) ((EntityDamageByEntityEvent) event.toBukkit()).getDamager();
                attacker.setVelocity(UtilityMethods.safeNormalize(attacker.getLocation().toVector().subtract(player.getLocation().toVector())).setY(.35).multiply(parryKnockback));
            }
            return;
        }
    }*/

    /**
     * @param cooldown      Default cooldown
     * @param reductionStat Mitigation cooldown reduction
     * @return The actual player cooldown
     */
    private double calculateCooldown(double cooldown, double reductionStat) {
        return cooldown * (1 - reductionStat / 100);
    }

    /**
     * @param victim Entity being hit
     * @return If there is a damager, returns a vector pointing towards damager.
     *         Otherwise, just returns the victim's eye location.
     */
    @NotNull
    private Vector getVector(LivingEntity victim, AttackEvent event) {
        final StatProvider attacker = event.getAttack().getAttacker();

        // Backwards compatibility
        if (attacker == null) {
            final Entity damager = event.toBukkit() instanceof EntityDamageByEntityEvent ? ((EntityDamageByEntityEvent) event.toBukkit()).getDamager() : null;
            return damager == null ? UtilityMethods.safeNormalize(damager.getLocation().subtract(victim.getLocation()).toVector()) : victim.getEyeLocation().getDirection();
        }

        return UtilityMethods.safeNormalize(attacker.getEntity().getLocation().subtract(victim.getLocation()).toVector());
    }

    private double getYaw(Entity player, Vector vec) {
        return new Location(player.getWorld(), vec.getX(), vec.getY(), vec.getZ()).setDirection(vec).getYaw();
    }
}

