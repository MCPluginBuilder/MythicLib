package io.lumine.mythic.lib.skill.handler.def.target;

import io.lumine.mythic.lib.api.event.AttackEvent;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.BuiltinSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import io.lumine.mythic.lib.util.ParabolicProjectile;
import io.lumine.mythic.lib.util.SmallParticleEffect;
import io.lumine.mythic.lib.util.TemporaryHandler;
import io.lumine.mythic.lib.version.VParticle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

@BuiltinSkillHandler(mods = {"ratio", "duration"})
public class Weaken extends SkillHandler<TargetSkillResult> {
    public Weaken(ConfigurationSection config) {
        super(config);
    }

    @Override
    public @NotNull TargetSkillResult getResult(SkillMetadata meta) {
        return new TargetSkillResult(meta);
    }

    @Override
    public void whenCast(TargetSkillResult result, SkillMetadata skillMeta) {
        LivingEntity target = result.getTarget();
        Player caster = skillMeta.getCaster().getPlayer();

        new ParabolicProjectile(caster.getPlayer().getLocation().add(0, 1, 0), target.getLocation().add(0, target.getHeight() / 2, 0), randomVector(caster.getPlayer()), () -> {
            if (!target.isDead()) new Handler(skillMeta, target);
        }, 2, VParticle.WITCH.get());
    }

    private Vector randomVector(Player player) {
        double a = Math.toRadians(player.getEyeLocation().getYaw() + 90);
        a += (RANDOM.nextBoolean() ? 1 : -1) * (Math.random() + .5) * Math.PI / 6;
        return new Vector(Math.cos(a), .8, Math.sin(a)).normalize().multiply(.4);
    }

    static class Handler extends TemporaryHandler {
        private final Entity entity;
        private final double damageCoefficient;

        public Handler(SkillMetadata skillMeta, Entity entity) {
            //super(skillMeta.getCaster().getData());
            //No need to attach a player to that skill

            this.entity = entity;
            this.damageCoefficient = 1 + skillMeta.getParameter("ratio") / 100;
            double duration = skillMeta.getParameter("duration");

            new SmallParticleEffect(entity, VParticle.WITCH.get());

            closeAfter((long) (duration * 20));
        }

        @EventHandler
        public void a(AttackEvent event) {
            if (event.getEntity().equals(entity)) {
                event.getEntity().getWorld().spawnParticle(VParticle.WITCH.get(), entity.getLocation().add(0, entity.getHeight() / 2, 0), 16, .5, .5, .5, 0);
                event.getDamage().multiplicativeModifier(damageCoefficient);
            }
        }
    }
}
