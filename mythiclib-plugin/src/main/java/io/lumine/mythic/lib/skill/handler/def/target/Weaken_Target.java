package io.lumine.mythic.lib.skill.handler.def.target;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.event.AttackEvent;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import io.lumine.mythic.lib.version.VParticle;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Weaken_Target extends SkillHandler<TargetSkillResult> implements Listener {
    private static final Map<UUID, Double> MARKED_ENTITIES = new HashMap<>();

    public Weaken_Target() {
        super();

        registerModifiers("duration", "extra-damage");
    }

    @Override
    public TargetSkillResult getResult(SkillMetadata meta) {
        return new TargetSkillResult(meta);
    }

    @Override
    public void whenCast(TargetSkillResult result, SkillMetadata skillMeta) {
        final LivingEntity target = result.getTarget();

        MARKED_ENTITIES.put(target.getUniqueId(), 1 + skillMeta.getParameter("extra-damage") / 100);
        playWeakenEffect(target.getLocation());
        target.getWorld().playSound(target.getLocation(), Sounds.ENTITY_ENDERMAN_HURT, 2, 1.5f);

        /*
         * display particles until the entity is hit again and eventually remove
         * the mark from the entity
         */
        new BukkitRunnable() {
            final long expire = System.currentTimeMillis() + (long) (skillMeta.getParameter("duration") * 1000);

            public void run() {
                if (!MARKED_ENTITIES.containsKey(target.getUniqueId()) || expire < System.currentTimeMillis()) {
                    cancel();
                    return;
                }

                for (double j = 0; j < Math.PI * 2; j += Math.PI / 18)
                    target.getWorld().spawnParticle(VParticle.SMOKE.get(), target.getLocation().clone().add(Math.cos(j) * .7, .1, Math.sin(j) * .7), 0);
            }
        }.runTaskTimer(MythicLib.plugin, 0, 20);
    }

    @EventHandler
    public void a(AttackEvent event) {
        if (event.toBukkit().getCause() != DamageCause.ENTITY_ATTACK
                && event.toBukkit().getCause() != DamageCause.ENTITY_EXPLOSION
                && event.toBukkit().getCause() != DamageCause.PROJECTILE) return;

        final Entity entity = event.getEntity();
        final Double found = MARKED_ENTITIES.get(entity.getUniqueId());
        if (found != null) {
            event.getDamage().multiplicativeModifier(found);
            playWeakenEffect(entity.getLocation());
            MARKED_ENTITIES.remove(entity.getUniqueId());
            entity.getWorld().playSound(entity.getLocation(), Sounds.ENTITY_ENDERMAN_DEATH, 2, 2);
        }
    }

    @EventHandler
    public void removeMark(PlayerItemConsumeEvent event) {
        final Player player = event.getPlayer();
        final ItemStack item = event.getItem();
        if (item.getType() == Material.MILK_BUCKET && MARKED_ENTITIES.containsKey(player.getUniqueId())) {
            MARKED_ENTITIES.remove(player.getUniqueId());
            player.getWorld().playSound(player.getLocation(), Sounds.ENTITY_ENDERMAN_DEATH, 2, 2);
        }
    }

    private void playWeakenEffect(Location loc) {
        new BukkitRunnable() {
            double y = 0;

            public void run() {
                for (int j = 0; j < 3; j++) {
                    y += .07;
                    for (int k = 0; k < 3; k++)
                        loc.getWorld().spawnParticle(VParticle.REDSTONE.get(), loc.clone().add(
                                        Math.cos(y * Math.PI + (k * Math.PI * 2 / 3)) * (3 - y) / 2.5,
                                        y,
                                        Math.sin(y * Math.PI + (k * Math.PI * 2 / 3)) * (3 - y) / 2.5),
                                1, new Particle.DustOptions(Color.BLACK, 1));
                }
                if (y > 3)
                    cancel();
            }
        }.runTaskTimer(MythicLib.plugin, 0, 1);
    }
}
