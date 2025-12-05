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
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@BuiltinSkillHandler(mods = {"duration", "damage", "inaccuracy", "force"})
public class Chicken_Wraith extends SkillHandler<SimpleSkillResult> {
    public Chicken_Wraith(ConfigurationSection config) {
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
        private final List<Integer> entities = new ArrayList<>();
        private final double damage, duration, inaccuracy, force;
        private final Player caster;

        public Handler(SkillMetadata skillMeta) {
            super(skillMeta.getCaster().getData());

            this.damage = skillMeta.getParameter("damage");
            this.duration = skillMeta.getParameter("duration") * 10;
            this.inaccuracy = skillMeta.getParameter("inaccuracy");
            this.force = skillMeta.getParameter("force");

            this.caster = skillMeta.getCaster().getPlayer();

            runTask(runnable -> runnable.runTaskTimer(MythicLib.plugin, 0, 2));
        }

        @Override
        protected @Nullable BukkitRunnable newTask() {
            return new BukkitRunnable() {
                int j = 0;

                public void run() {
                    if (j++ > duration) {
                        closeAfter(5 * 20);
                        return;
                    }

                    Location loc = caster.getEyeLocation();
                    loc.setPitch((float) (loc.getPitch() + (Math.random() - .5) * inaccuracy));
                    loc.setYaw((float) (loc.getYaw() + (Math.random() - .5) * inaccuracy));

                    // Launch egg
                    loc.getWorld().playSound(loc, Sounds.ENTITY_CHICKEN_EGG, 1, 1);
                    Egg egg = caster.launchProjectile(Egg.class);
                    egg.setVelocity(loc.getDirection().multiply(1.3 * force));

                    Handler.this.entities.add(egg.getEntityId());
                }
            };
        }

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        public void a(PlayerEggThrowEvent event) {
            if (entities.contains(event.getEgg().getEntityId()))
                event.setHatching(false);
        }

        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        public void b(EntityDamageByEntityEvent event) {
            if (entities.contains(event.getDamager().getEntityId()))
                // TODO notify MythicLib of custom damage type
                event.setDamage(damage);
        }
    }
}
