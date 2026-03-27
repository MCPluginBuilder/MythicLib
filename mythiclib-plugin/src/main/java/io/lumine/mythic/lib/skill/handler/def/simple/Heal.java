package io.lumine.mythic.lib.skill.handler.def.simple;

import io.lumine.mythic.lib.player.resource.Resources;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.BuiltinSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@BuiltinSkillHandler(mods = {"heal"})
public class Heal extends SkillHandler<SimpleSkillResult> {
    public Heal(ConfigurationSection config) {
        super(config);
    }

    @Override
    public @NotNull SimpleSkillResult getResult(SkillMetadata meta) {
        return new SimpleSkillResult();
    }

    @Override
    public void whenCast(SimpleSkillResult result, SkillMetadata skillMeta) {
        final Player caster = skillMeta.getCaster().getPlayer();

        caster.getWorld().playSound(caster.getLocation(), Sounds.ENTITY_PLAYER_LEVELUP, 1, 2);
        caster.getWorld().spawnParticle(Particle.HEART, caster.getLocation().add(0, .75, 0), 16, 1, 1, 1, 0);
        caster.getWorld().spawnParticle(VParticle.HAPPY_VILLAGER.get(), caster.getLocation().add(0, .75, 0), 16, 1, 1, 1, 0);
        Resources.heal(caster, skillMeta.getParameter("heal"));
    }
}
