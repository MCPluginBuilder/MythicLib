package io.lumine.mythic.lib.skill.handler.def.simple;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.util.NoClipItem;
import io.lumine.mythic.lib.version.VParticle;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Level;

public class Present_Throw extends SkillHandler<SimpleSkillResult> {
    private final ItemStack present = new ItemStack(Material.PLAYER_HEAD);

    public Present_Throw() {
        super();

        registerModifiers("damage", "radius", "force");

        try {
            ItemMeta presentMeta = present.getItemMeta();
            UtilityMethods.setTextureValue((SkullMeta) presentMeta, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTcyNmQ5ZDA2MzJlNDBiZGE1YmNmNjU4MzliYTJjYzk4YTg3YmQ2MTljNTNhZGYwMDMxMGQ2ZmM3MWYwNDJiNSJ9fX0=");
            present.setItemMeta(presentMeta);
        } catch (RuntimeException exception) {
            MythicLib.plugin.getLogger().log(Level.WARNING, "Could not apply 'Present Throw' head texture");
        }
    }

    @Override
    public SimpleSkillResult getResult(SkillMetadata meta) {
        return new SimpleSkillResult();
    }

    @Override
    public void whenCast(SimpleSkillResult result, SkillMetadata skillMeta) {
        double damage = skillMeta.getParameter("damage");
        double radiusSquared = Math.pow(skillMeta.getParameter("radius"), 2);

        Player caster = skillMeta.getCaster().getPlayer();

        final NoClipItem item = new NoClipItem(caster.getLocation().add(0, 1.2, 0), present);
        item.getEntity().setVelocity(caster.getEyeLocation().getDirection().multiply(1.5 * skillMeta.getParameter("force")));

        /*
         * when items are moving through the air, they loose a percent of their
         * velocity proportionally to their coordinates in each axis. this means
         * that if the trajectory is not affected, the ratio of x/y will always
         * be the same. check for any change of that ratio to check for a
         * trajectory change
         */
        final double trajRatio = item.getEntity().getVelocity().getX() / item.getEntity().getVelocity().getZ();
        caster.getWorld().playSound(caster.getLocation(), Sounds.ENTITY_SNOWBALL_THROW, 1, 0);
        new BukkitRunnable() {
            int ti = 0;

            public void run() {
                if (ti++ > 70 || item.getEntity().isDead()) {
                    item.close();
                    cancel();
                }

                double currentTrajRatio = item.getEntity().getVelocity().getX() / item.getEntity().getVelocity().getZ();
                item.getEntity().getWorld().spawnParticle(VParticle.INSTANT_EFFECT.get(), item.getEntity().getLocation().add(0, .1, 0), 0);
                if (item.getEntity().isOnGround() || Math.abs(trajRatio - currentTrajRatio) > .1) {
                    item.getEntity().getWorld().spawnParticle(VParticle.FIREWORK.get(), item.getEntity().getLocation().add(0, .1, 0), 128, 0, 0, 0, .25);
                    item.getEntity().getWorld().playSound(item.getEntity().getLocation(), Sounds.ENTITY_FIREWORK_ROCKET_TWINKLE, 2, 1.5f);
                    for (Entity entity : UtilityMethods.getNearbyChunkEntities(item.getEntity().getLocation()))
                        if (entity.getLocation().distanceSquared(item.getEntity().getLocation()) < radiusSquared && UtilityMethods.canTarget(caster, entity))
                            skillMeta.getCaster().attack((LivingEntity) entity, damage, DamageType.SKILL, DamageType.MAGIC, DamageType.PROJECTILE);
                    item.close();
                    cancel();
                }
            }
        }.runTaskTimer(MythicLib.plugin, 0, 1);
    }
}
