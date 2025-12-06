package io.lumine.mythic.lib.skill.handler.def.simple;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.BuiltinSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import io.lumine.mythic.lib.version.VPotionEffectType;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@BuiltinSkillHandler(mods = {"damage", "radius"})
public class Overload extends SkillHandler<SimpleSkillResult> {
    private final List<DamageType> damageTypes;

    public Overload(ConfigurationSection config) {
        super(config);

        damageTypes = DamageType.listFromConfig(List.of(DamageType.SKILL, DamageType.MAGIC), config.get("damage_types"));
    }

    @Override
    public @NotNull SimpleSkillResult getResult(SkillMetadata meta) {
        return new SimpleSkillResult();
    }

    @Override
    public void whenCast(SimpleSkillResult result, SkillMetadata skillMeta) {
        double damage = skillMeta.getParameter("damage");
        double radius = skillMeta.getParameter("radius");

        Player caster = skillMeta.getCaster().getPlayer();

        caster.getWorld().playSound(caster.getLocation(), Sounds.ENTITY_GENERIC_EXPLODE, 2, 0);
        caster.getWorld().playSound(caster.getLocation(), Sounds.ENTITY_FIREWORK_ROCKET_TWINKLE, 2, 0);
        caster.addPotionEffect(new PotionEffect(VPotionEffectType.SLOWNESS.get(), 2, 254));

        for (Entity entity : caster.getNearbyEntities(radius, radius, radius))
            if (UtilityMethods.canTarget(caster, entity))
                skillMeta.getCaster().attack((LivingEntity) entity, damage, damageTypes);

        double step = 12 + (radius * 2.5);
        for (double j = 0; j < Math.PI * 2; j += Math.PI / step) {
            Location loc = caster.getLocation().clone().add(Math.cos(j) * radius, 1, Math.sin(j) * radius);
            caster.getWorld().spawnParticle(Particle.CLOUD, loc, 4, 0, 0, 0, .05);
            caster.getWorld().spawnParticle(VParticle.FIREWORK.get(), loc, 4, 0, 0, 0, .05);
        }
    }
}
