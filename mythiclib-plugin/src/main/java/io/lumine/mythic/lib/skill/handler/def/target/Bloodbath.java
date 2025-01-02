package io.lumine.mythic.lib.skill.handler.def.target;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class Bloodbath extends SkillHandler<TargetSkillResult> {
    public Bloodbath() {
        super();

        registerModifiers("amount");
    }

    @Override
    public TargetSkillResult getResult(SkillMetadata meta) {
        return new TargetSkillResult(meta);
    }

    @Override
    public void whenCast(TargetSkillResult result, SkillMetadata skillMeta) {
        LivingEntity target = result.getTarget();
        Player caster = skillMeta.getCaster().getPlayer();

        target.getWorld().playSound(target.getLocation(), Sounds.ENTITY_COW_HURT, 1, 2);
        target.getWorld().playEffect(target.getLocation().add(0, 1, 0), Effect.STEP_SOUND, 152);
        caster.setFoodLevel((int) Math.min(20, caster.getFoodLevel() + skillMeta.getParameter("amount")));
    }
}
