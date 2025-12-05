package io.lumine.mythic.lib.skill.handler.def.simple;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.event.AttackEvent;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.BuiltinSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.util.TemporaryHandler;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@BuiltinSkillHandler(mods = {"power", "radius", "duration"})
public class Magical_Shield extends SkillHandler<SimpleSkillResult> {
    public Magical_Shield(ConfigurationSection config) {
        super(config);
    }

    @Override
    public @NotNull SimpleSkillResult getResult(SkillMetadata meta) {
        return new SimpleSkillResult(meta.getCaster().getPlayer().isOnGround());
    }

    @Override
    public void whenCast(SimpleSkillResult result, SkillMetadata skillMeta) {
        double duration = skillMeta.getParameter("duration");
        double radiusSquared = Math.pow(skillMeta.getParameter("radius"), 2);
        double power = skillMeta.getParameter("power") / 100;

        new Handler(skillMeta.getCaster().getData(), duration, radiusSquared, power);
    }

    static class Handler extends TemporaryHandler {
        private final Location loc;
        private final double radiusSquared, power;

        public Handler(MMOPlayerData playerData, double duration, double radius, double power) {
            super(playerData);

            Player caster = playerData.getPlayer();
            this.loc = caster.getLocation().clone();

            this.radiusSquared = radius * radius;
            this.power = power;

            caster.getWorld().playSound(caster.getLocation(), Sounds.ENTITY_ENDERMAN_TELEPORT, 3, 0);

            runTask(runnable -> runnable.runTaskTimer(MythicLib.plugin, 0, 3));

            closeAfter((long) (duration * 20));
        }

        @Override
        protected @Nullable BukkitRunnable newTask() {
            return new BukkitRunnable() {
                @Override
                public void run() {
                    for (double j = 0; j < Math.PI / 2; j += Math.PI / (28 + RANDOM.nextInt(5)))
                        for (double i = 0; i < Math.PI * 2; i += Math.PI / (14 + RANDOM.nextInt(5)))
                            loc.getWorld().spawnParticle(VParticle.REDSTONE.get(),
                                    loc.clone().add(2.5 * Math.cos(i + j) * Math.sin(j), 2.5 * Math.cos(j), 2.5 * Math.sin(i + j) * Math.sin(j)), 1,
                                    new Particle.DustOptions(Color.FUCHSIA, 1));
                }
            };
        }

        @EventHandler
        public void a(AttackEvent event) {
            if (event.getEntity() instanceof Player
                    && event.getEntity().getWorld().equals(loc.getWorld())
                    && event.getEntity().getLocation().distanceSquared(loc) < radiusSquared)
                event.getDamage().multiplicativeModifier(1 - power);
        }
    }
}
