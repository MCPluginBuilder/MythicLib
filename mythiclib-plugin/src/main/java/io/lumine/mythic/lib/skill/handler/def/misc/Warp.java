package io.lumine.mythic.lib.skill.handler.def.misc;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.BuiltinSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.SkillResult;
import io.lumine.mythic.lib.util.ParabolicProjectile;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

@BuiltinSkillHandler(mods = {"range"})
public class Warp extends SkillHandler<Warp.WarpSkillResult> {
    public Warp(ConfigurationSection config) {
        super(config);
    }

    @NotNull
    @Override
    public WarpSkillResult getResult(SkillMetadata meta) {
        return new WarpSkillResult(meta);
    }

    @Override
    public void whenCast(WarpSkillResult result, SkillMetadata skillMeta) {
        Player caster = skillMeta.getCaster().getPlayer();

        caster.getWorld().playSound(caster.getLocation(), Sounds.BLOCK_END_PORTAL_FRAME_FILL, 1, 2);
        Location loc = result.getLocation();

        new ParabolicProjectile(caster.getLocation().add(0, 1, 0), loc.clone().add(0, 1, 0), () -> {
            if (UtilityMethods.isInvalidated(skillMeta.getCaster())) return;

            caster.teleport(loc);
            caster.getWorld().spawnParticle(VParticle.LARGE_EXPLOSION.get(), caster.getLocation().add(0, 1, 0), 0);
            VParticle.INSTANT_EFFECT.spawnSafeSpell(caster.getLocation().add(0, 1, 0), 32, 0, 0, 0, .1);
            caster.getWorld().playSound(caster.getLocation(), Sounds.ENTITY_ENDERMAN_TELEPORT, 1, 1);
        }, 2, VParticle.INSTANT_EFFECT);
    }

    public static class WarpSkillResult implements SkillResult {
        private final Location loc;

        public WarpSkillResult(SkillMetadata meta) {
            Player caster = meta.getCaster().getPlayer();
            RayTraceResult res = caster.rayTraceBlocks(meta.getParameter("range"), FluidCollisionMode.NEVER);
            loc = res == null ? null : res.getHitPosition().toLocation(caster.getWorld());
            if (loc != null) {
                final Vector dir = caster.getEyeLocation().getDirection();
                loc.setDirection(dir);
                loc.add(dir.setY(0).multiply(-.5));
            }
        }

        @Override
        public boolean isSuccessful() {
            return loc != null;
        }

        public Location getLocation() {
            return loc;
        }
    }
}
