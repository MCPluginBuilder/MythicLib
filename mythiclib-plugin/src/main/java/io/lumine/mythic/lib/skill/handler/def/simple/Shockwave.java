package io.lumine.mythic.lib.skill.handler.def.simple;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.BuiltinSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.util.TemporaryHandler;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@BuiltinSkillHandler(mods = {"knock-up", "length"})
public class Shockwave extends SkillHandler<SimpleSkillResult> {
    public Shockwave(ConfigurationSection config) {
        super(config);
    }

    @Override
    public @NotNull SimpleSkillResult getResult(SkillMetadata meta) {
        return new SimpleSkillResult();
    }

    @Override
    public void whenCast(SimpleSkillResult result, SkillMetadata skillMeta) {
        double knockUp = skillMeta.getParameter("knock-up");
        double length = skillMeta.getParameter("length");

        Player caster = skillMeta.getCaster().getPlayer();

        TemporaryHandler.timerTask(skillMeta.getCaster().getData(), 1, handler -> new BukkitRunnable() {
            final Vector vec = caster.getEyeLocation().getDirection().setY(0);
            final Location loc = caster.getLocation();
            final List<Integer> hit = new ArrayList<>();
            int ti = 0;

            public void run() {
                if (++ti >= Math.min(20, length)) {
                    handler.close();
                    return;
                }

                loc.add(vec);

                loc.getWorld().playSound(loc, Sounds.BLOCK_GRAVEL_BREAK, 1, 2);
                loc.getWorld().spawnParticle(VParticle.BLOCK.get(), loc, 12, .5, 0, .5, 0, Material.DIRT.createBlockData());

                for (Entity ent : UtilityMethods.getNearbyChunkEntities(loc))
                    if (ent.getLocation().distanceSquared(loc) < 1.1 * 1.1 && UtilityMethods.canTarget(caster, ent) && !hit.contains(ent.getEntityId())) {
                        hit.add(ent.getEntityId());
                        ent.playEffect(EntityEffect.HURT);
                        ent.setVelocity(ent.getVelocity().setY(.4 * knockUp));
                    }
            }
        });
    }
}
