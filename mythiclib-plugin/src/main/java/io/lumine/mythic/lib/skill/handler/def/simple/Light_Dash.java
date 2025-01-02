package io.lumine.mythic.lib.skill.handler.def.simple;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Light_Dash extends SkillHandler<SimpleSkillResult> {
    public Light_Dash() {
        super();

        registerModifiers("damage", "length");
    }

    @Override
    public SimpleSkillResult getResult(SkillMetadata meta) {
        return new SimpleSkillResult();
    }

    @Override
    public void whenCast(SimpleSkillResult result, SkillMetadata skillMeta) {
        double damage = skillMeta.getParameter("damage");
        double length = skillMeta.getParameter("length");

        Player caster = skillMeta.getCaster().getPlayer();

        new BukkitRunnable() {
            final Vector vec = caster.getEyeLocation().getDirection();
            final List<Integer> hit = new ArrayList<>();
            int j = 0;

            public void run() {
                if (j++ > 10 * Math.min(10, length))
                    cancel();

                caster.setVelocity(vec);
                caster.getWorld().spawnParticle(VParticle.LARGE_SMOKE.get(), caster.getLocation().add(0, 1, 0), 0);
                caster.getWorld().playSound(caster.getLocation(), Sounds.ENTITY_ENDER_DRAGON_FLAP, 1, 2);
                for (Entity entity : caster.getNearbyEntities(1, 1, 1))
                    if (!hit.contains(entity.getEntityId()) && UtilityMethods.canTarget(caster, entity)) {
                        hit.add(entity.getEntityId());
                        skillMeta.getCaster().attack((LivingEntity) entity, damage, DamageType.SKILL, DamageType.PHYSICAL);
                    }
            }
        }.runTaskTimer(MythicLib.plugin, 0, 2);
    }
}
