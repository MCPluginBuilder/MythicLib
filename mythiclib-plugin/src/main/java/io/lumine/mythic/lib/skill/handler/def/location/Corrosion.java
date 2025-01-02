package io.lumine.mythic.lib.skill.handler.def.location;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.LocationSkillResult;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Corrosion extends SkillHandler<LocationSkillResult> {
    public Corrosion() {
        super();

        registerModifiers("duration", "amplifier", "radius");
    }

    @Override
    public LocationSkillResult getResult(SkillMetadata meta) {
        return new LocationSkillResult(meta);
    }

    @Override
    public void whenCast(LocationSkillResult result, SkillMetadata skillMeta) {
        Location loc = result.getTarget();
        Player caster = skillMeta.getCaster().getPlayer();

        int duration = (int) (skillMeta.getParameter("duration") * 20);
        int amplifier = (int) skillMeta.getParameter("amplifier");
        double radiusSquared = Math.pow(skillMeta.getParameter("radius"), 2);

        loc.getWorld().spawnParticle(VParticle.ITEM_SLIME.get(), loc, 48, 2, 2, 2, 0);
        loc.getWorld().spawnParticle(VParticle.HAPPY_VILLAGER.get(), loc, 32, 2, 2, 2, 0);
        loc.getWorld().playSound(loc, Sounds.BLOCK_BREWING_STAND_BREW, 2, 0);

        for (Entity entity : UtilityMethods.getNearbyChunkEntities(loc))
            if (entity.getLocation().distanceSquared(loc) < radiusSquared && UtilityMethods.canTarget(caster, entity)) {
                ((LivingEntity) entity).removePotionEffect(PotionEffectType.POISON);
                ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.POISON, duration, amplifier));
            }
    }
}
