package io.lumine.mythic.lib.skill.handler.def.simple;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class Heal extends SkillHandler<SimpleSkillResult> {
    public Heal() {
        super();

        registerModifiers("heal");
    }

    @Override
    public SimpleSkillResult getResult(SkillMetadata meta) {
        return new SimpleSkillResult();
    }

    @Override
    public void whenCast(SimpleSkillResult result, SkillMetadata skillMeta) {
        final Player caster = skillMeta.getCaster().getPlayer();

        caster.getWorld().playSound(caster.getLocation(), Sounds.ENTITY_PLAYER_LEVELUP, 1, 2);
        caster.getWorld().spawnParticle(Particle.HEART, caster.getLocation().add(0, .75, 0), 16, 1, 1, 1, 0);
        caster.getWorld().spawnParticle(VParticle.HAPPY_VILLAGER.get(), caster.getLocation().add(0, .75, 0), 16, 1, 1, 1, 0);
        UtilityMethods.heal(caster, skillMeta.getParameter("heal"));
    }
}
