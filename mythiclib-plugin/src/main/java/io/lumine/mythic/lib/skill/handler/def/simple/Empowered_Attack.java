package io.lumine.mythic.lib.skill.handler.def.simple;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.BuiltinSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.util.SmallParticleEffect;
import io.lumine.mythic.lib.util.TemporaryHandler;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@BuiltinSkillHandler(mods = {"radius", "ratio", "extra"})
public class Empowered_Attack extends SkillHandler<SimpleSkillResult> {
    private final List<DamageType> damageTypes, triggerDamageTypes;

    private static final double PARTICLES_PER_METER = 5;

    public Empowered_Attack(ConfigurationSection config) {
        super(config);

        damageTypes = DamageType.listFromConfig(List.of(DamageType.SKILL, DamageType.PHYSICAL), config.get("damage_types"));
        triggerDamageTypes = DamageType.listFromConfig(List.of(DamageType.WEAPON), config.get("trigger_damage_types"));
    }

    @Override
    public @NotNull SimpleSkillResult getResult(SkillMetadata meta) {
        return new SimpleSkillResult();
    }

    @Override
    public void whenCast(SimpleSkillResult result, SkillMetadata skillMeta) {
        Player caster = skillMeta.getCaster().getPlayer();
        caster.playSound(caster.getLocation(), Sounds.BLOCK_END_PORTAL_FRAME_FILL, 1, 1);
        new EmpoweredAttack(skillMeta.getCaster(), skillMeta.getParameter("extra"), skillMeta.getParameter("ratio"), skillMeta.getParameter("radius"));
    }

    class EmpoweredAttack extends TemporaryHandler {
        private final PlayerMetadata caster;
        private final double c, r, rad;

        public EmpoweredAttack(PlayerMetadata caster, double extra, double ratio, double radius) {
            super(caster.getData());

            this.caster = caster;
            this.c = 1 + extra / 100;
            this.r = ratio / 100;
            this.rad = radius;

            if (caster.getData().isOnline())
                new SmallParticleEffect(caster.getPlayer(), VParticle.FIREWORK.get());

            closeAfter(4 * 20);
        }

        @EventHandler
        public void a(PlayerAttackEvent event) {
            if (!caster.getData().isOnline()) return;
            if (event.getAttacker().getPlayer().equals(caster.getPlayer())
                    && event.getAttack().getDamage().hasAnyType(triggerDamageTypes)) {
                close();

                Entity target = event.getEntity();

                // Play lightning effect
                final Location loc = target.getLocation().add(0, target.getHeight() / 2, 0);
                for (int j = 0; j < 3; j++) {
                    Location clone = loc.clone();
                    double a = Math.random() * Math.PI * 2;
                    loc.add(Math.cos(a), 5, Math.sin(a));
                    drawVector(clone, loc.clone().subtract(clone).toVector());
                }

                target.getWorld().playSound(target.getLocation(), Sounds.ENTITY_FIREWORK_ROCKET_BLAST, 2, .5f);
                target.getWorld().spawnParticle(VParticle.FIREWORK.get(), target.getLocation().add(0, target.getHeight() / 2, 0), 32, 0, 0, 0, .2);

                double sweep = event.getAttack().getDamage().getDamage() * r;
                Location src = target.getLocation().add(0, target.getHeight() / 2, 0);

                for (Entity entity : target.getNearbyEntities(rad, rad, rad))
                    if (UtilityMethods.canTarget(caster.getPlayer(), entity)) {
                        drawVector(src, entity.getLocation().add(0, entity.getHeight() / 2, 0).subtract(src).toVector());
                        event.getAttacker().attack((LivingEntity) entity, sweep, damageTypes);
                    }

                /*
                 * Apply damage afterwards otherwise the damage dealt to nearby
                 * entities scale with the extra ability damage.
                 */
                event.getAttack().getDamage().multiplicativeModifier(c, triggerDamageTypes);
            }
        }
    }

    private static void drawVector(Location loc, Vector vec) {

        double steps = vec.length() * PARTICLES_PER_METER;
        Vector v = vec.clone().normalize().multiply((double) 1 / PARTICLES_PER_METER);

        for (int j = 0; j < Math.min(steps, 124); j++)
            loc.getWorld().spawnParticle(VParticle.FIREWORK.get(), loc.add(v), 0);
    }
}
