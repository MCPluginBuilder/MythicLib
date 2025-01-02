package io.lumine.mythic.lib.skill.handler.def.target;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import io.lumine.mythic.lib.version.VParticle;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class Blind extends SkillHandler<TargetSkillResult> {
    public Blind() {
        super();

        registerModifiers("duration");
    }

    @Override
    public TargetSkillResult getResult(SkillMetadata meta) {
        return new TargetSkillResult(meta);
    }

    @Override
    public void whenCast(TargetSkillResult result, SkillMetadata skillMeta) {
        LivingEntity target = result.getTarget();
        Player caster = skillMeta.getCaster().getPlayer();

        target.getWorld().playSound(target.getLocation(), Sounds.ENTITY_ENDERMAN_HURT, 1, 2);
        for (double i = 0; i < Math.PI * 2; i += Math.PI / 24)
            for (double j = 0; j < 2; j++) {
                Location loc = target.getLocation();
                Vector vec = UtilityMethods.rotate(new Vector(Math.cos(i), 1 + Math.cos(i + (Math.PI * j)) * .5, Math.sin(i)),
                        caster.getLocation().getDirection());
                loc.getWorld().spawnParticle(VParticle.REDSTONE.get(), loc.add(vec), 1, new Particle.DustOptions(Color.BLACK, 1));
            }
        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int) (skillMeta.getParameter("duration") * 20), 0));
    }
}
