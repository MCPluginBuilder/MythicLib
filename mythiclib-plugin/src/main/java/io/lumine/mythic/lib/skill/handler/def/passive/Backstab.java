package io.lumine.mythic.lib.skill.handler.def.passive;

import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.player.skill.PassiveSkill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.BuiltinSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.AttackSkillResult;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@BuiltinSkillHandler(mods = {"extra"}, triggerable = false)
public class Backstab extends SkillHandler<AttackSkillResult> implements Listener {
    private final List<DamageType> damageTypes;

    public Backstab(ConfigurationSection config) {
        super(config);

        damageTypes = DamageType.listFromConfig(List.of(DamageType.PHYSICAL), config.get("damage_types"));
    }

    @Override
    public @NotNull AttackSkillResult getResult(SkillMetadata meta) {
        return new AttackSkillResult(meta);
    }

    @Override
    public void whenCast(AttackSkillResult result, SkillMetadata skillMeta) {
        LivingEntity target = (LivingEntity) skillMeta.getTargetEntityOrNull();
        result.getAttack().getDamage().multiplicativeModifier(1 + skillMeta.getParameter("extra") / 100, damageTypes);
        target.getWorld().spawnParticle(VParticle.ENCHANTED_HIT.get(), target.getLocation().add(0, target.getHeight() / 2, 0), 32, 0, 0, 0, .5);
        target.getWorld().playSound(target.getLocation(), Sounds.ENTITY_ENDERMAN_HURT, 1, 1.5f);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void a(PlayerAttackEvent event) {
        MMOPlayerData data = event.getAttacker().getData();
        LivingEntity target = event.getEntity();
        if (!event.getAttack().getDamage().hasAnyType(damageTypes)
                || event.getAttacker().getPlayer().getEyeLocation().getDirection().angle(target.getEyeLocation().getDirection()) > Math.PI / 3
                || event.getAttacker().getPlayer().getGameMode() == GameMode.SPECTATOR)
            return;

        PassiveSkill skill = data.getPassiveSkillMap().getSkill(this);
        if (skill == null)
            return;

        skill.getTriggeredSkill().cast(SkillMetadata.of(event));
    }
}
