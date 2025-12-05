package io.lumine.mythic.lib.skill.handler.def.simple;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.BuiltinSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.util.TemporaryHandler;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

@BuiltinSkillHandler
public class Hoearthquake extends SkillHandler<SimpleSkillResult> {
    public Hoearthquake(ConfigurationSection config) {
        super(config);
    }

    @Override
    public @NotNull SimpleSkillResult getResult(SkillMetadata meta) {
        return new SimpleSkillResult(meta.getCaster().getPlayer().isOnGround());
    }

    @Override
    public void whenCast(SimpleSkillResult result, SkillMetadata skillMeta) {
        Player caster = skillMeta.getCaster().getPlayer();

        TemporaryHandler.timerTask(skillMeta.getCaster().getData(), 1, handler -> new BukkitRunnable() {
            final Vector vec = caster.getEyeLocation().getDirection().setY(0);
            final Location loc = caster.getLocation();
            int ti = 0;

            public void run() {
                if (ti++ > 20) {
                    handler.close();
                    return;
                }

                loc.add(vec);
                loc.getWorld().playSound(loc, Sounds.BLOCK_GRAVEL_BREAK, 2, 1);
                loc.getWorld().spawnParticle(Particle.CLOUD, loc, 1, .5, 0, .5, 0);

                for (int x = -1; x < 2; x++)
                    for (int z = -1; z < 2; z++) {
                        Block b = loc.clone().add(x, -1, z).getBlock();
                        if (b.getType() == VMaterial.GRASS_BLOCK.get() || b.getType() == Material.DIRT) {
                            BlockBreakEvent event = new BlockBreakEvent(b, caster);
                            event.setDropItems(false);
                            Bukkit.getPluginManager().callEvent(event);
                            if (!event.isCancelled()) b.setType(Material.FARMLAND);
                        }
                    }
            }
        });
    }
}
