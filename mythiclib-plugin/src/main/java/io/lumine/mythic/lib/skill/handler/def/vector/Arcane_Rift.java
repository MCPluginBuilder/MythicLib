package io.lumine.mythic.lib.skill.handler.def.vector;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.VectorSkillResult;
import io.lumine.mythic.lib.version.VParticle;
import io.lumine.mythic.lib.version.VPotionEffectType;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Arcane_Rift extends SkillHandler<VectorSkillResult> {
    public Arcane_Rift() {
        super();

        registerModifiers("damage", "amplifier", "speed", "duration");
    }

    @Override
    public VectorSkillResult getResult(SkillMetadata meta) {
        return meta.getCaster().getPlayer().isOnGround() ? new VectorSkillResult(meta) : new VectorSkillResult((Vector) null);
    }

    @Override
    public void whenCast(VectorSkillResult result, SkillMetadata skillMeta) {
        Player caster = skillMeta.getCaster().getPlayer();

        double damage = skillMeta.getParameter("damage");
        double slowDuration = skillMeta.getParameter("duration");
        double slowAmplifier = skillMeta.getParameter("amplifier");

        caster.getWorld().playSound(caster.getLocation(), Sounds.ENTITY_ENDERMAN_DEATH, 2, .5f);
        new BukkitRunnable() {
            final Vector vec = result.getTarget().setY(0).normalize().multiply(.5 * skillMeta.getParameter("speed"));
            final Location loc = caster.getLocation();
            final int duration = (int) (20 * Math.min(skillMeta.getParameter("duration"), 10.));
            final List<Integer> hit = new ArrayList<>();
            int ti = 0;

            public void run() {
                if (ti++ > duration)
                    cancel();

                loc.add(vec);
                loc.getWorld().spawnParticle(VParticle.WITCH.get(), loc, 5, .5, 0, .5, 0);

                for (Entity entity : UtilityMethods.getNearbyChunkEntities(loc))
                    if (UtilityMethods.canTarget(caster, entity) && loc.distanceSquared(entity.getLocation()) < 2 && !hit.contains(entity.getEntityId())) {
                        hit.add(entity.getEntityId());
                        skillMeta.getCaster().attack((LivingEntity) entity, damage, DamageType.SKILL, DamageType.MAGIC);
                        ((LivingEntity) entity).addPotionEffect(new PotionEffect(VPotionEffectType.SLOWNESS.get(), (int) (slowDuration * 20), (int) slowAmplifier));
                    }
            }
        }.runTaskTimer(MythicLib.plugin, 0, 1);
    }
}
