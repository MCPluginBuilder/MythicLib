package io.lumine.mythic.lib.skill.handler.def.item;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.ItemSkillResult;
import io.lumine.mythic.lib.util.NoClipItem;
import io.lumine.mythic.lib.version.VParticle;
import io.lumine.mythic.lib.version.VPotionEffectType;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class Item_Bomb extends SkillHandler<ItemSkillResult> {
    public Item_Bomb() {
        super();

        registerModifiers("damage", "radius", "slow-duration", "slow-amplifier");
    }

    @Override
    public ItemSkillResult getResult(SkillMetadata meta) {
        return new ItemSkillResult(meta, Material.COAL_BLOCK);
    }

    @Override
    public void whenCast(ItemSkillResult result, SkillMetadata skillMeta) {
        ItemStack itemStack = result.getItem();
        Player caster = skillMeta.getCaster().getPlayer();

        final NoClipItem item = new NoClipItem(caster.getLocation().add(0, 1.2, 0), itemStack);
        item.getEntity().setVelocity(result.getTarget().multiply(1.3));
        caster.getWorld().playSound(caster.getLocation(), Sounds.ENTITY_SNOWBALL_THROW, 2, 0);

        new BukkitRunnable() {
            int j = 0;

            public void run() {
                if (j++ > 40) {
                    double radius = skillMeta.getParameter("radius");
                    double damage = skillMeta.getParameter("damage");
                    double slowDuration = skillMeta.getParameter("slow-duration");
                    double slowAmplifier = skillMeta.getParameter("slow-amplifier");

                    for (Entity entity : item.getEntity().getNearbyEntities(radius, radius, radius))
                        if (UtilityMethods.canTarget(caster, entity)) {
                            skillMeta.getCaster().attack((LivingEntity) entity, damage, DamageType.SKILL, DamageType.PHYSICAL);
                            UtilityMethods.forcePotionEffect((LivingEntity) entity, VPotionEffectType.SLOWNESS.get(), slowDuration, (int) slowAmplifier);
                        }

                    item.getEntity().getWorld().spawnParticle(VParticle.LARGE_EXPLOSION.get(), item.getEntity().getLocation(), 24, 2, 2, 2, 0);
                    item.getEntity().getWorld().spawnParticle(VParticle.EXPLOSION.get(), item.getEntity().getLocation(), 48, 0, 0, 0, .2);
                    item.getEntity().getWorld().playSound(item.getEntity().getLocation(), Sounds.ENTITY_GENERIC_EXPLODE, 3, 0);

                    item.close();
                    cancel();
                    return;
                }

                item.getEntity().getWorld().spawnParticle(VParticle.LARGE_SMOKE.get(), item.getEntity().getLocation().add(0, .2, 0), 0);
                item.getEntity().getWorld().spawnParticle(VParticle.FIREWORK.get(), item.getEntity().getLocation().add(0, .2, 0), 1, 0, 0, 0, .1);
                item.getEntity().getWorld().playSound(item.getEntity().getLocation(), Sounds.BLOCK_NOTE_BLOCK_HAT, 2, (float) (.5 + (j / 40. * 1.5)));
            }
        }.runTaskTimer(MythicLib.plugin, 0, 1);
    }
}
