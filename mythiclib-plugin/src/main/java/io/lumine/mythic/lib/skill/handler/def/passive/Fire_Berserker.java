package io.lumine.mythic.lib.skill.handler.def.passive;

import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.player.skill.PassiveSkill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.BuiltinSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.AttackSkillResult;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

@BuiltinSkillHandler(mods = {"extra"}, triggerable = false)
public class Fire_Berserker extends SkillHandler<AttackSkillResult> implements Listener {
    public Fire_Berserker(ConfigurationSection config) {
        super(config);
    }

    @Override
    public @NotNull AttackSkillResult getResult(SkillMetadata meta) {
        return new AttackSkillResult(meta);
    }

    @Override
    public void whenCast(AttackSkillResult result, SkillMetadata skillMeta) {
        skillMeta.getAttackSource().getDamage().multiplicativeModifier(1 + skillMeta.getParameter("extra") / 100);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void a(PlayerAttackEvent event) {
        MMOPlayerData data = event.getAttacker().getData();
        if (event.getAttacker().getPlayer().getFireTicks() <= 0)
            return;

        PassiveSkill skill = data.getPassiveSkillMap().getSkill(this);
        if (skill == null)
            return;

        skill.getTriggeredSkill().cast(SkillMetadata.of(event));
    }
}
