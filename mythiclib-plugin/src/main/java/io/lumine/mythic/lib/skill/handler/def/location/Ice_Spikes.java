package io.lumine.mythic.lib.skill.handler.def.location;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.BuiltinSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.LocationSkillResult;
import io.lumine.mythic.lib.util.Line3D;
import io.lumine.mythic.lib.util.TemporaryHandler;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import io.lumine.mythic.lib.version.VPotionEffectType;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@BuiltinSkillHandler(mods = {"damage", "slow"})
public class Ice_Spikes extends SkillHandler<LocationSkillResult> {
    private final List<DamageType> damageTypes;

    private static final double RADIUS = 3;

    public Ice_Spikes(ConfigurationSection config) {
        super(config);

        damageTypes = DamageType.listFromConfig(List.of(DamageType.SKILL, DamageType.MAGIC), config.get("damage_types"));
    }

    @NotNull
    @Override
    public LocationSkillResult getResult(SkillMetadata meta) {
        return new LocationSkillResult(meta, 20);
    }

    @Override
    public void whenCast(LocationSkillResult result, SkillMetadata skillMeta) {
        Location loc = result.getTarget();
        Player caster = skillMeta.getCaster().getPlayer();

        double damage = skillMeta.getParameter("damage");
        int slow = (int) (20 * skillMeta.getParameter("slow"));

        TemporaryHandler.timerTask(skillMeta.getCaster().getData(), 5, handler -> new BukkitRunnable() {
            int j = 0;

            @Override
            public void run() {

                if (j++ > 8) {
                    handler.close();
                    return;
                }

                Location loc1 = loc.clone().add(offset() * RADIUS, 0, offset() * RADIUS).add(0, 2, 0);
                loc.getWorld().spawnParticle(VParticle.FIREWORK.get(), loc1, 32, 0, 2, 0, 0);
                loc.getWorld().spawnParticle(VParticle.SNOWFLAKE.get(), loc1, 32, 0, 2, 0, 0);
                loc.getWorld().playSound(loc1, Sounds.BLOCK_GLASS_BREAK, 2, 0);

                Line3D line = new Line3D(loc, new Vector(0, 1, 0));
                for (Entity entity : UtilityMethods.getNearbyChunkEntities(loc1))
                    if (line.distanceSquared(entity.getLocation().toVector()) < RADIUS && Math.abs(entity.getLocation().getY() - loc1.getY()) < 10 && UtilityMethods.canTarget(caster, entity)) {
                        skillMeta.getCaster().attack((LivingEntity) entity, damage, damageTypes);
                        ((LivingEntity) entity).addPotionEffect(new PotionEffect(VPotionEffectType.SLOWNESS.get(), slow, 0));
                    }
            }
        });
    }

    private double offset() {
        return Math.random() * (RANDOM.nextBoolean() ? 1 : -1);
    }
}
