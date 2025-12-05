package io.lumine.mythic.lib.skill.handler.def.simple;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.BuiltinSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.util.TemporaryHandler;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@BuiltinSkillHandler(mods = {"duration", "damage", "inaccuracy", "force"})
public class Blizzard extends SkillHandler<SimpleSkillResult> {
    public Blizzard(ConfigurationSection config) {
        super(config);
    }

    @Override
    public @NotNull SimpleSkillResult getResult(SkillMetadata meta) {
        return new SimpleSkillResult();
    }

    @Override
    public void whenCast(SimpleSkillResult result, SkillMetadata skillMeta) {
        new Handler(skillMeta);
    }

    static class Handler extends TemporaryHandler {
        private final List<UUID> entities = new ArrayList<>();
        private final double damage, duration, force, inaccuracy;
        private final Player caster;

        public Handler(SkillMetadata skillMeta) {
            super(skillMeta.getCaster().getData());

            this.damage = skillMeta.getParameter("damage");
            this.duration = skillMeta.getParameter("duration") * 10;
            this.force = skillMeta.getParameter("force");
            this.inaccuracy = skillMeta.getParameter("inaccuracy");

            this.caster = skillMeta.getCaster().getPlayer();

            runTask(runnable -> runnable.runTaskTimer(MythicLib.plugin, 0, 2));
        }

        @Override
        protected BukkitRunnable newTask() {
            return new BukkitRunnable() {
                int j = 0;

                public void run() {
                    if (j++ > duration) {
                        Handler.this.closeAfter(10 * 20);
                        return;
                    }

                    Location loc = caster.getEyeLocation();
                    loc.setPitch((float) (loc.getPitch() + (Math.random() - .5) * inaccuracy));
                    loc.setYaw((float) (loc.getYaw() + (Math.random() - .5) * inaccuracy));

                    loc.getWorld().playSound(loc, Sounds.ENTITY_SNOWBALL_THROW, 1, 1);
                    Snowball snowball = caster.launchProjectile(Snowball.class);
                    snowball.setVelocity(loc.getDirection().multiply(1.3 * force));
                    Handler.this.entities.add(snowball.getUniqueId());
                }
            };
        }

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        public void a(EntityDamageByEntityEvent event) {
            if (entities.remove(event.getDamager().getUniqueId()))
                event.setDamage(damage);
        }
    }
}
