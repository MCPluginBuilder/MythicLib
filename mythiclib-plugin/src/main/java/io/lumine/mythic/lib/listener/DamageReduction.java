package io.lumine.mythic.lib.listener;

import io.lumine.mythic.lib.api.event.AttackEvent;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.StatMap;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.element.Element;
import io.lumine.mythic.lib.util.DefenseFormula;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

public class DamageReduction implements Listener {

    private static final double MIN_DEFENSE = 1e-6;

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void damageMitigation(AttackEvent event) {

        // Skip invalid players
        final MMOPlayerData data = MMOPlayerData.getOrNull(event.getEntity());
        if (data == null) return;

        // Applies specific damage reduction
        for (SpecificDamageReductionType type : SpecificDamageReductionType.values())
            type.applyReduction(data.getStatMap(), event.getDamage(), event.toBukkit());

        // Applies damage reduction for existing damage types
        for (DamageType damageType : DamageType.values())
            event.getDamage().multiplicativeModifier(Math.max(0, 1 - data.getStatMap().getStat(damageType + "_DAMAGE_REDUCTION") / 100), damageType);

        // Applies the Defense stat to neutral damage
        final double defense = data.getStatMap().getStat("DEFENSE"), neutralDamage;
        if (Math.abs(defense) > MIN_DEFENSE && (neutralDamage = event.getDamage().getDamage((Element) null)) > 0) {
            final double ratio = DefenseFormula.calculateDamage(false, defense, neutralDamage) / neutralDamage;
            event.getDamage().multiplicativeModifier(ratio, (Element) null);
        }
    }

    private static final Set<EntityDamageEvent.DamageCause> FIRE_DAMAGE_CAUSES
            = new HashSet<>(Arrays.asList(EntityDamageEvent.DamageCause.FIRE, EntityDamageEvent.DamageCause.FIRE_TICK, EntityDamageEvent.DamageCause.LAVA, EntityDamageEvent.DamageCause.MELTING));

    /**
     * Damage reduction types which do NOT depend on an
     * existing MythicLib damage type
     */
    public enum SpecificDamageReductionType {

        // Damage reduction, always applies
        ENVIRONMENTAL("DAMAGE_REDUCTION", event -> true),

        // Vanilla damage types
        PVP(event -> event instanceof EntityDamageByEntityEvent && getDamager((EntityDamageByEntityEvent) event) instanceof Player),
        PVE(event -> event instanceof EntityDamageByEntityEvent && !(getDamager((EntityDamageByEntityEvent) event) instanceof Player)),
        FIRE(event -> FIRE_DAMAGE_CAUSES.contains(event.getCause())),
        FALL(event -> event.getCause() == EntityDamageEvent.DamageCause.FALL);

        /**
         * The corresponding item stat that will be used to
         * apply damage reduction. For instance, ENVIRONMENTAL calls
         * DAMAGE_REDUCTION and MAGIC calls MAGIC_DAMAGE_REDUCTION
         */
        @NotNull
        private final String stat;

        /**
         * When this field is not null, if it does return true, it will reduce
         * all the damage from every damage packet. This is used for vanilla
         * damage types, like {@link #FALL} or {@link #FIRE} or even {@link #ENVIRONMENTAL}
         */
        @NotNull
        private final Predicate<EntityDamageEvent> apply;

        SpecificDamageReductionType(String stat, Predicate<EntityDamageEvent> apply) {
            this.stat = stat;
            this.apply = Objects.requireNonNull(apply);
        }

        SpecificDamageReductionType(Predicate<EntityDamageEvent> apply) {
            this.stat = name() + "_DAMAGE_REDUCTION";
            this.apply = Objects.requireNonNull(apply);
        }

        public void applyReduction(StatMap statMap, DamageMetadata damageMeta, EntityDamageEvent event) {
            if (apply.test(event))
                damageMeta.multiplicativeModifier(1 - Math.min(statMap.getStat(stat) / 100, 1));
        }
    }

    /**
     * Tries to find the entity who dealt the damage in some attack event. Main issue is that
     * if it is a ranged attack like a trident or an arrow, we have to find back the shooter.
     */
    private static LivingEntity getDamager(EntityDamageByEntityEvent event) {

        // Check direct damager
        if (event.getDamager() instanceof LivingEntity)
            return (LivingEntity) event.getDamager();

        /*
         * Checks projectile and add damage type, which supports every vanilla
         * projectile like snowballs, tridents and arrows
         */
        if (event.getDamager() instanceof Projectile) {
            Projectile proj = (Projectile) event.getDamager();
            if (proj.getShooter() instanceof LivingEntity)
                return (LivingEntity) proj.getShooter();
        }

        return null;
    }
}
