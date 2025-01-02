package io.lumine.mythic.lib.skill.handler.def.simple;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.comp.interaction.InteractionType;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Burning_Hands extends SkillHandler<SimpleSkillResult> {
    public Burning_Hands() {
        super();

        registerModifiers("duration", "damage");
    }

    @Override
    public SimpleSkillResult getResult(SkillMetadata meta) {
        return new SimpleSkillResult();
    }

    @Override
    public void whenCast(SimpleSkillResult result, SkillMetadata skillMeta) {
        double duration = skillMeta.getParameter("duration") * 10;
        double damage = skillMeta.getParameter("damage") / 2;

        Player caster = skillMeta.getCaster().getPlayer();

        new BukkitRunnable() {
            int j = 0;

            public void run() {
                if (j++ > duration)
                    cancel();

                Location loc = caster.getLocation().add(0, 1.2, 0);
                loc.getWorld().playSound(loc, Sounds.BLOCK_FIRE_AMBIENT, 1, 1);

                for (double m = -45; m < 45; m += 5) {
                    double a = (m + caster.getEyeLocation().getYaw() + 90) * Math.PI / 180;
                    Vector vec = new Vector(Math.cos(a), (RANDOM.nextDouble() - .5) * .2, Math.sin(a));
                    Location source = loc.clone().add(vec.clone().setY(0));
                    source.getWorld().spawnParticle(Particle.FLAME, source, 0, vec.getX(), vec.getY(), vec.getZ(), .5);
                    if (j % 2 == 0)
                        source.getWorld().spawnParticle(VParticle.SMOKE.get(), source, 0, vec.getX(), vec.getY(), vec.getZ(), .5);
                }

                if (j % 5 == 0)
                    for (Entity entity : UtilityMethods.getNearbyChunkEntities(loc))
                        if (entity.getLocation().distanceSquared(loc) < 60
                                && caster.getEyeLocation().getDirection()
                                .angle(entity.getLocation().toVector().subtract(caster.getLocation().toVector())) < Math.PI / 6
                                && MythicLib.plugin.getEntities().canInteract(caster, entity, InteractionType.OFFENSE_SKILL))
                            skillMeta.getCaster().attack((LivingEntity) entity, damage, DamageType.SKILL, DamageType.MAGIC);

            }
        }.runTaskTimer(MythicLib.plugin, 0, 2);
    }
}
