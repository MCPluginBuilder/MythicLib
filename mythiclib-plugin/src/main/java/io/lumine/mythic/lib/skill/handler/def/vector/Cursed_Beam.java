package io.lumine.mythic.lib.skill.handler.def.vector;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.BuiltinSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.VectorSkillResult;
import io.lumine.mythic.lib.util.TemporaryHandler;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@BuiltinSkillHandler(mods = {"damage", "duration"})
public class Cursed_Beam extends SkillHandler<VectorSkillResult> {
    private final List<DamageType> damageTypes;

    public Cursed_Beam(ConfigurationSection config) {
        super(config);

        damageTypes = DamageType.listFromConfig(List.of(DamageType.SKILL, DamageType.MAGIC, DamageType.PROJECTILE), config.get("damage_types"));
    }

    @Override
    public @NotNull VectorSkillResult getResult(SkillMetadata meta) {
        return new VectorSkillResult(meta);
    }

    @Override
    public void whenCast(VectorSkillResult result, SkillMetadata skillMeta) {
        Player caster = skillMeta.getCaster().getPlayer();

        double duration = skillMeta.getParameter("duration");

        caster.getWorld().playSound(caster.getLocation(), Sounds.ENTITY_WITHER_SHOOT, 2, 2);
        TemporaryHandler.timerTask(skillMeta.getCaster().getData(), 1, handler -> new BukkitRunnable() {
            final Vector dir = result.getTarget().multiply(.3);
            final Location loc = caster.getEyeLocation().clone();
            final double r = 0.4;
            int ti = 0;

            public void run() {
                if (++ti > 50) {
                    handler.close();
                    return;
                }

                List<Entity> entities = UtilityMethods.getNearbyChunkEntities(loc);
                for (double j = 0; j < 4; j++) {
                    loc.add(dir);
                    for (double i = 0; i < Math.PI * 2; i += Math.PI / 6) {
                        Vector vec = UtilityMethods.rotate(new Vector(r * Math.cos(i), r * Math.sin(i), 0), loc.getDirection());
                        loc.add(vec);
                        loc.getWorld().spawnParticle(VParticle.WITCH.get(), loc, 0);
                        loc.add(vec.multiply(-1));
                    }

                    for (Entity target : entities)
                        if (UtilityMethods.canTarget(caster, loc, target)) {
                            playHitEffect(target);
                            double damage = skillMeta.getParameter("damage");
                            loc.getWorld().playSound(loc, Sounds.ENTITY_ENDERMAN_TELEPORT, 2, .7f);

                            // Damage nearby entities
                            for (Entity entity : entities)
                                if (UtilityMethods.canTarget(caster, entity) && loc.distanceSquared(entity.getLocation().add(0, 1, 0)) < 9) {
                                    skillMeta.getCaster().attack((LivingEntity) entity, damage, damageTypes);
                                    ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int) (duration * 20), 0));
                                }

                            handler.close();
                            return;
                        }
                }
            }
        });
    }

    private void playHitEffect(Entity ent) {
        new BukkitRunnable() {
            final Location loc2 = ent.getLocation();
            double y = 0;

            public void run() {
                for (int i = 0; i < 3; i++) {
                    y += .05;
                    for (int j = 0; j < 2; j++) {
                        double xz = y * Math.PI * .8 + (j * Math.PI);
                        loc2.getWorld().spawnParticle(VParticle.WITCH.get(), loc2.clone().add(Math.cos(xz) * 2.5, y, Math.sin(xz) * 2.5), 0);
                    }
                }
                if (y >= 3) cancel();
            }
        }.runTaskTimer(MythicLib.plugin, 0, 1);
    }
}
