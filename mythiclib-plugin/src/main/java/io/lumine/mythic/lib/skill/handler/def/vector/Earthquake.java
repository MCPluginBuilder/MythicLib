package io.lumine.mythic.lib.skill.handler.def.vector;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.BuiltinSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.VectorSkillResult;
import io.lumine.mythic.lib.util.TemporaryHandler;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VPotionEffectType;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@BuiltinSkillHandler(mods = {"damage", "duration", "amplifier"})
public class Earthquake extends SkillHandler<VectorSkillResult> {
    private final List<DamageType> damageTypes;

    public Earthquake(ConfigurationSection config) {
        super(config);

        damageTypes = DamageType.listFromConfig(List.of(DamageType.SKILL, DamageType.MAGIC), config.get("damage_types"));
    }

    @Override
    public @NotNull VectorSkillResult getResult(SkillMetadata meta) {
        return meta.getCaster().getPlayer().isOnGround() ? new VectorSkillResult(meta) : new VectorSkillResult((Vector) null);
    }

    @Override
    public void whenCast(VectorSkillResult result, SkillMetadata skillMeta) {
        Player caster = skillMeta.getCaster().getPlayer();

        double damage = skillMeta.getParameter("damage");
        double slowDuration = skillMeta.getParameter("duration");
        double slowAmplifier = skillMeta.getParameter("amplifier");

        TemporaryHandler.timerTask(skillMeta.getCaster().getData(), 1, handler -> new BukkitRunnable() {
            final Vector vec = result.getTarget().setY(0);
            final Location loc = caster.getLocation();
            final List<Integer> hit = new ArrayList<>();
            int ti = 0;

            public void run() {
                ti++;
                if (ti > 20) {
                    handler.close();
                    return;
                }

                loc.add(vec);
                loc.getWorld().spawnParticle(Particle.CLOUD, loc, 5, .5, 0, .5, 0);
                loc.getWorld().playSound(loc, Sounds.BLOCK_GRAVEL_BREAK, 2, 1);

                for (Entity entity : UtilityMethods.getNearbyChunkEntities(loc))
                    if (UtilityMethods.canTarget(caster, entity) && loc.distanceSquared(entity.getLocation()) < 2 && !hit.contains(entity.getEntityId())) {
                        hit.add(entity.getEntityId());
                        skillMeta.getCaster().attack((LivingEntity) entity, damage, damageTypes);
                        UtilityMethods.forcePotionEffect((LivingEntity) entity, VPotionEffectType.SLOWNESS.get(), slowDuration, (int) slowAmplifier);
                    }
            }
        });
    }
}
