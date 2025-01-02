package io.lumine.mythic.lib.skill.handler.def.simple;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.util.TemporaryListener;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Blizzard extends SkillHandler<SimpleSkillResult> {
    public Blizzard() {
        super();

        registerModifiers("duration", "damage", "inaccuracy", "force");
    }

    @Override
    public SimpleSkillResult getResult(SkillMetadata meta) {
        return new SimpleSkillResult();
    }

    @Override
    public void whenCast(SimpleSkillResult result, SkillMetadata skillMeta) {
        double duration = skillMeta.getParameter("duration") * 10;
        double force = skillMeta.getParameter("force");
        double inaccuracy = skillMeta.getParameter("inaccuracy");

        Player caster = skillMeta.getCaster().getPlayer();

        new BukkitRunnable() {
            final CustomSnowballRegistry handler = new CustomSnowballRegistry(skillMeta.getParameter("damage"));
            int j = 0;

            public void run() {
                if (j++ > duration) {
                    handler.close(10 * 20);
                    cancel();
                    return;
                }

                Location loc = caster.getEyeLocation();
                loc.setPitch((float) (loc.getPitch() + (RANDOM.nextDouble() - .5) * inaccuracy));
                loc.setYaw((float) (loc.getYaw() + (RANDOM.nextDouble() - .5) * inaccuracy));

                loc.getWorld().playSound(loc, Sounds.ENTITY_SNOWBALL_THROW, 1, 1);
                Snowball snowball = caster.launchProjectile(Snowball.class);
                snowball.setVelocity(loc.getDirection().multiply(1.3 * force));
                handler.entities.add(snowball.getUniqueId());
            }
        }.runTaskTimer(MythicLib.plugin, 0, 2);
    }

    public static class CustomSnowballRegistry extends TemporaryListener {
        private final List<UUID> entities = new ArrayList<>();
        private final double damage;

        public CustomSnowballRegistry(double damage) {
            this.damage = damage;
        }

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        public void a(EntityDamageByEntityEvent event) {
            if (entities.remove(event.getDamager().getUniqueId()))
                event.setDamage(damage);
        }
    }
}
