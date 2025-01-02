package io.lumine.mythic.lib.skill.handler.def.simple;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.version.VParticle;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;

public class Blink extends SkillHandler<SimpleSkillResult> {
    public Blink() {
        super();

        registerModifiers("range");
    }

    @Override
    public SimpleSkillResult getResult(SkillMetadata meta) {
        return new SimpleSkillResult();
    }

    /**
     * Lower bound to prevent the player from not teleporting at all
     */
    private static final double MIN_RANGE = 1;

    @Override
    public void whenCast(SimpleSkillResult result, SkillMetadata skillMeta) {
        final Player caster = skillMeta.getCaster().getPlayer();
        final double range = Math.max(MIN_RANGE, skillMeta.getParameter("range"));
        final Vector dir = caster.getEyeLocation().getDirection();

        // Effects on prev position
        caster.getWorld().spawnParticle(VParticle.LARGE_EXPLOSION.get(), caster.getLocation().add(0, 1, 0), 0);
        caster.getWorld().spawnParticle(VParticle.INSTANT_EFFECT.get(), caster.getLocation().add(0, 1, 0), 32, 0, 0, 0, .1);
        caster.getWorld().playSound(caster.getLocation(), Sounds.ENTITY_ENDERMAN_TELEPORT, 1, 1);

        @Nullable final RayTraceResult rtResult = caster.getWorld().rayTraceBlocks(caster.getEyeLocation(), dir, range, FluidCollisionMode.NEVER, true);
        final Location loc = rtResult == null || rtResult.getHitBlock() == null ? caster.getLocation().add(dir.clone().multiply(range)) :
                rtResult.getHitPosition().add(rtResult.getHitBlockFace().getDirection().multiply(.3)).toLocation(caster.getWorld());
        loc.setYaw(caster.getLocation().getYaw());
        loc.setPitch(caster.getLocation().getPitch());
        caster.teleport(loc);

        // Effects on newest position
        caster.getWorld().spawnParticle(VParticle.LARGE_EXPLOSION.get(), caster.getLocation().add(0, 1, 0), 0);
        caster.getWorld().spawnParticle(VParticle.INSTANT_EFFECT.get(), caster.getLocation().add(0, 1, 0), 32, 0, 0, 0, .1);
    }
}
