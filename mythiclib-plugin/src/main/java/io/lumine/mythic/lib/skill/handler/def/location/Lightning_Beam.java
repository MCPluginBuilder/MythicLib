package io.lumine.mythic.lib.skill.handler.def.location;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.LocationSkillResult;
import io.lumine.mythic.lib.version.VParticle;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Lightning_Beam extends SkillHandler<LocationSkillResult> {
    public Lightning_Beam() {
        super();

        registerModifiers("damage", "radius");
    }

    @Override
    public LocationSkillResult getResult(SkillMetadata meta) {
        return new LocationSkillResult(meta);
    }

    @Override
    public void whenCast(LocationSkillResult result, SkillMetadata skillMeta) {
        Location loc = result.getTarget();
        Player caster = skillMeta.getCaster().getPlayer();

        double damage = skillMeta.getParameter("damage");
        double radius = skillMeta.getParameter("radius");

        for (Entity entity : UtilityMethods.getNearbyChunkEntities(loc))
            if (UtilityMethods.canTarget(caster, entity) && entity.getLocation().distanceSquared(loc) <= radius * radius)
                skillMeta.getCaster().attack((LivingEntity) entity, damage, DamageType.SKILL, DamageType.MAGIC);

        caster.getWorld().playSound(caster.getLocation(), Sounds.ENTITY_FIREWORK_ROCKET_BLAST, 1, 0);
        loc.getWorld().spawnParticle(VParticle.FIREWORK.get(), loc, 64, 0, 0, 0, .2);
        loc.getWorld().spawnParticle(VParticle.EXPLOSION.get(), loc, 32, 0, 0, 0, .2);
        Vector vec = new Vector(0, .3, 0);
        for (double j = 0; j < 40; j += .3)
            loc.getWorld().spawnParticle(VParticle.FIREWORK.get(), loc.add(vec), 6, .1, .1, .1, .01);
    }

    private Location getFirstNonSolidBlock(Location loc) {
        Location initial = loc.clone();
        for (int j = 0; j < 5; j++)
            if (!loc.add(0, 1, 0).getBlock().getType().isSolid())
                return loc;
        return initial;
    }
}
