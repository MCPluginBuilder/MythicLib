package io.lumine.mythic.lib.skill.handler.def.location;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.BuiltinSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.LocationSkillResult;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@BuiltinSkillHandler(mods = {"damage", "duration", "amplifier"})
public class Corrupt extends SkillHandler<LocationSkillResult> {
    private final List<DamageType> damageTypes;

    public Corrupt(ConfigurationSection config) {
        super(config);

        damageTypes = DamageType.listFromConfig(List.of(DamageType.SKILL, DamageType.MAGIC), config.get("damage_types"));
    }

    @Override
    public @NotNull LocationSkillResult getResult(SkillMetadata meta) {
        return new LocationSkillResult(meta);
    }

    @Override
    public void whenCast(LocationSkillResult result, SkillMetadata skillMeta) {
        Location loc = result.getTarget();
        Player caster = skillMeta.getCaster().getPlayer();

        double damage = skillMeta.getParameter("damage");
        double duration = skillMeta.getParameter("duration");
        double amplifier = skillMeta.getParameter("amplifier");
        double radius = 2.7;

        loc.add(0, -1, 0);
        caster.getWorld().playSound(caster.getLocation(), Sounds.ENTITY_ENDERMAN_HURT, 1, .5f);
        for (double j = 0; j < Math.PI * 2; j += Math.PI / 36) {
            Location loc1 = loc.clone().add(Math.cos(j) * radius, 1, Math.sin(j) * radius);
            double y_max = .5 + Math.random();
            for (double y = 0; y < y_max; y += .1)
                loc1.getWorld().spawnParticle(VParticle.REDSTONE.get(), loc1.clone().add(0, y, 0), 1, new Particle.DustOptions(Color.PURPLE, 1));
        }

        for (Entity entity : UtilityMethods.getNearbyChunkEntities(loc))
            if (UtilityMethods.canTarget(caster, entity) && entity.getLocation().distanceSquared(loc) <= radius * radius) {
                skillMeta.getCaster().attack((LivingEntity) entity, damage, damageTypes);
                ((LivingEntity) entity).removePotionEffect(PotionEffectType.WITHER);
                ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.WITHER, (int) (duration * 20), (int) amplifier));
            }
    }
}
