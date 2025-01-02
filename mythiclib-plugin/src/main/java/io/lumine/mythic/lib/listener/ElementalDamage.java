package io.lumine.mythic.lib.listener;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.event.AttackEvent;
import io.lumine.mythic.lib.api.stat.provider.StatProvider;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.element.Element;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.skill.Skill;
import io.lumine.mythic.lib.skill.trigger.TriggerMetadata;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import io.lumine.mythic.lib.util.DefenseFormula;
import io.lumine.mythic.lib.version.wrapper.VersionWrapper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Random;

/**
 * Class which implements the elemental damage calculation
 * AND application. This also applies elemental critical
 * strikes.
 *
 * @author indyuce
 */
public class ElementalDamage implements Listener {
    private static final Random RANDOM = new Random();

    @EventHandler(ignoreCancelled = true)
    public void applyElementalDamage(AttackEvent event) {

        // Make sure ML can identify the attacker
        if (event.getAttack().getAttacker() == null) return;

        // Apply on-hit elemental damage
        final StatProvider attacker = event.getAttack().getAttacker();
        final double critChanceCoef = attacker.getStat("CRITICAL_STRIKE_CHANCE") / 100;
        if (event.getDamage().hasType(DamageType.WEAPON)) {
            final double attackCharge = attacker instanceof PlayerMetadata ? VersionWrapper.get().getAttackCooldown(((PlayerMetadata) attacker).getPlayer()) : 1;
            for (Element element : MythicLib.plugin.getElements().getAll()) {
                final double damage = attacker.getStat(element.getId() + "_DAMAGE") * attackCharge;
                if (damage == 0) continue;

                if (MythicLib.plugin.getMMOConfig().skipElementalDamageApplication) {
                    ((PlayerMetadata) attacker).setStat(element.getId() + "_DAMAGE", damage); // Update for placeholders
                    applyElementalScripts(event, element, attacker, critChanceCoef); // Apply scripts first?
                } else event.getDamage().add(damage, element); // Otherwise just set damage
            }
        }

        // Apply elemental damage modifiers
        for (Element element : MythicLib.plugin.getElements().getAll()) {
            if (!event.getDamage().hasElement(element)) continue;

            // Apply percent-based damage buff
            event.getDamage().multiplicativeModifier(1 + Math.max(-1, attacker.getStat(element.getId() + "_DAMAGE_PERCENT") / 100), element);

            // Apply elemental weakness
            final StatProvider opponent = StatProvider.get(event.getEntity(), null, false);
            event.getDamage().multiplicativeModifier(1 + Math.max(-1, opponent.getStat(element.getId() + "_WEAKNESS") / 100), element);

            // Apply elemental defense
            final double defense = opponent.getStat(element.getId() + "_DEFENSE") * (1 + Math.max(-1, opponent.getStat(element.getId() + "_DEFENSE_PERCENT") / 100));
            final double initialDamage = event.getDamage().getDamage(element);
            if (initialDamage == 0) continue;
            final double finalDamage = DefenseFormula.calculateDamage(true, defense, initialDamage);
            event.getDamage().multiplicativeModifier(finalDamage / initialDamage, element);

            // Apply scripts last?
            if (!MythicLib.plugin.getMMOConfig().skipElementalDamageApplication)
                applyElementalScripts(event, element, attacker, critChanceCoef);
        }
    }

    private void applyElementalScripts(AttackEvent event, Element element, StatProvider attacker, double critChanceCoef) {
        final boolean crit = RANDOM.nextDouble() < critChanceCoef;
        final Skill skill = element.getSkill(crit);
        if (skill != null && attacker instanceof PlayerMetadata)
            skill.cast(new TriggerMetadata((PlayerMetadata) attacker, TriggerType.API, event.getEntity(), event.getAttack()));
        if (crit) event.getDamage().registerElementalCriticalStrike(element);
    }
}
