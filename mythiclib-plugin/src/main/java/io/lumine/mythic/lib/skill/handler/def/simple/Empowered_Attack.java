package io.lumine.mythic.lib.skill.handler.def.simple;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.util.SmallParticleEffect;
import io.lumine.mythic.lib.version.VParticle;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

public class Empowered_Attack extends SkillHandler<SimpleSkillResult> {
    private static final double PARTICLES_PER_METER = 5;

    public Empowered_Attack() {
        super();

        registerModifiers("radius", "ratio", "extra");
    }

    @Override
    public SimpleSkillResult getResult(SkillMetadata meta) {
        return new SimpleSkillResult();
    }

    @Override
    public void whenCast(SimpleSkillResult result, SkillMetadata skillMeta) {
        Player caster = skillMeta.getCaster().getPlayer();
        caster.playSound(caster.getLocation(), Sounds.BLOCK_END_PORTAL_FRAME_FILL, 1, 1);
        new EmpoweredAttack(skillMeta.getCaster(), skillMeta.getParameter("extra"), skillMeta.getParameter("ratio"), skillMeta.getParameter("radius"));
    }

    private void drawVector(Location loc, Vector vec) {

        double steps = vec.length() * PARTICLES_PER_METER;
        Vector v = vec.clone().normalize().multiply((double) 1 / PARTICLES_PER_METER);

        for (int j = 0; j < Math.min(steps, 124); j++)
            loc.getWorld().spawnParticle(VParticle.FIREWORK.get(), loc.add(v), 0);
    }

    public class EmpoweredAttack implements Listener {
        private final PlayerMetadata caster;
        private final double c, r, rad;

        public EmpoweredAttack(PlayerMetadata caster, double extra, double ratio, double radius) {
            this.caster = caster;
            this.c = 1 + extra / 100;
            this.r = ratio / 100;
            this.rad = radius;

            if (caster.getData().isOnline())
                new SmallParticleEffect(caster.getPlayer(), VParticle.FIREWORK.get());

            Bukkit.getPluginManager().registerEvents(this, MythicLib.plugin);
            Bukkit.getScheduler().scheduleSyncDelayedTask(MythicLib.plugin, this::close, 80);
        }

        private void close() {
            PlayerAttackEvent.getHandlerList().unregister(this);
        }

        @EventHandler
        public void a(PlayerAttackEvent event) {
            if (!caster.getData().isOnline()) return;
            if (event.getAttacker().getPlayer().equals(caster.getPlayer()) && event.getAttack().getDamage().hasType(DamageType.WEAPON)) {
                close();

                Entity target = event.getEntity();

                // Play lightning effect
                final Location loc = target.getLocation().add(0, target.getHeight() / 2, 0);
                for (int j = 0; j < 3; j++) {
                    Location clone = loc.clone();
                    double a = RANDOM.nextDouble() * Math.PI * 2;
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
                        event.getAttacker().attack((LivingEntity) entity, sweep, DamageType.SKILL, DamageType.PHYSICAL);
                    }

                /*
                 * Apply damage afterwards otherwise the damage dealt to nearby
                 * entities scale with the extra ability damage.
                 */
                event.getAttack().getDamage().multiplicativeModifier(c, DamageType.WEAPON);
            }
        }
    }
}
