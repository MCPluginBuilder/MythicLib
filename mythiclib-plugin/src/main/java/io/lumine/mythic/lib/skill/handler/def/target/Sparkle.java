package io.lumine.mythic.lib.skill.handler.def.target;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import io.lumine.mythic.lib.version.VParticle;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Sparkle extends SkillHandler<TargetSkillResult> {
    public Sparkle() {
        super();

        registerModifiers("damage", "limit", "radius");
    }

    @Override
    public TargetSkillResult getResult(SkillMetadata meta) {
        return new TargetSkillResult(meta);
    }

    @Override
    public void whenCast(TargetSkillResult result, SkillMetadata skillMeta) {
        LivingEntity target = result.getTarget();
        Player caster = skillMeta.getCaster().getPlayer();

        double damage = skillMeta.getParameter("damage");
        double radius = skillMeta.getParameter("radius");
        double limit = skillMeta.getParameter("limit");

        skillMeta.getCaster().attack(target, damage, DamageType.SKILL, DamageType.MAGIC);
        target.getWorld().spawnParticle(VParticle.LARGE_EXPLOSION.get(), target.getLocation().add(0, 1, 0), 0);
        target.getWorld().playSound(target.getLocation(), Sounds.ENTITY_FIREWORK_ROCKET_TWINKLE, 2, 2);

        int count = 0;
        for (Entity entity : target.getNearbyEntities(radius, radius, radius))
            if (count < limit && UtilityMethods.canTarget(caster, entity)) {
                count++;
                skillMeta.getCaster().attack((LivingEntity) entity, damage, DamageType.SKILL, DamageType.MAGIC);
                entity.getWorld().playSound(entity.getLocation(), Sounds.ENTITY_FIREWORK_ROCKET_TWINKLE, 2, 2);
                Location loc_t = target.getLocation().add(0, .75, 0);
                Location loc_ent = entity.getLocation().add(0, .75, 0);
                for (double j1 = 0; j1 < 1; j1 += .04) {
                    Vector d = loc_ent.toVector().subtract(loc_t.toVector());
                    target.getWorld().spawnParticle(VParticle.FIREWORK.get(), loc_t.clone().add(d.multiply(j1)), 3, .1, .1, .1, .008);
                }
            }
    }
}
