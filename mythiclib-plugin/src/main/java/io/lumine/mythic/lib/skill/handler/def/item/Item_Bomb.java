package io.lumine.mythic.lib.skill.handler.def.item;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.BuiltinSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.ItemSkillResult;
import io.lumine.mythic.lib.util.NoClipItem;
import io.lumine.mythic.lib.util.TemporaryHandler;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import io.lumine.mythic.lib.version.VPotionEffectType;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@BuiltinSkillHandler(mods = {"damage", "radius", "slow-duration", "slow-amplifier"})
public class Item_Bomb extends SkillHandler<ItemSkillResult> {
    private final List<DamageType> damageTypes;

    public Item_Bomb(ConfigurationSection config) {
        super(config);

        damageTypes = DamageType.listFromConfig(List.of(DamageType.SKILL, DamageType.PHYSICAL), config.get("damage_types"));
    }

    @Override
    public @NotNull ItemSkillResult getResult(SkillMetadata meta) {
        return new ItemSkillResult(meta, Material.COAL_BLOCK);
    }

    @Override
    public void whenCast(ItemSkillResult result, SkillMetadata skillMeta) {
        ItemStack itemStack = result.getItem();
        Player caster = skillMeta.getCaster().getPlayer();

        final NoClipItem item = new NoClipItem(caster.getLocation().add(0, 1.2, 0), itemStack);
        item.getEntity().setVelocity(result.getTarget().multiply(1.3));
        caster.getWorld().playSound(caster.getLocation(), Sounds.ENTITY_SNOWBALL_THROW, 2, 0);

        TemporaryHandler.timerTask(skillMeta.getCaster().getData(), 1, handler -> new BukkitRunnable() {
            int j = 0;

            public void run() {
                if (j++ > 40) {
                    double radius = skillMeta.getParameter("radius");
                    double damage = skillMeta.getParameter("damage");
                    double slowDuration = skillMeta.getParameter("slow-duration");
                    double slowAmplifier = skillMeta.getParameter("slow-amplifier");

                    for (Entity entity : item.getEntity().getNearbyEntities(radius, radius, radius))
                        if (UtilityMethods.canTarget(caster, entity)) {
                            skillMeta.getCaster().attack((LivingEntity) entity, damage, damageTypes);
                            UtilityMethods.forcePotionEffect((LivingEntity) entity, VPotionEffectType.SLOWNESS.get(), slowDuration, (int) slowAmplifier);
                        }

                    item.getEntity().getWorld().spawnParticle(VParticle.LARGE_EXPLOSION.get(), item.getEntity().getLocation(), 24, 2, 2, 2, 0);
                    item.getEntity().getWorld().spawnParticle(VParticle.EXPLOSION.get(), item.getEntity().getLocation(), 48, 0, 0, 0, .2);
                    item.getEntity().getWorld().playSound(item.getEntity().getLocation(), Sounds.ENTITY_GENERIC_EXPLODE, 3, 0);

                    item.close();
                    handler.close();
                    return;
                }

                item.getEntity().getWorld().spawnParticle(VParticle.LARGE_SMOKE.get(), item.getEntity().getLocation().add(0, .2, 0), 0);
                item.getEntity().getWorld().spawnParticle(VParticle.FIREWORK.get(), item.getEntity().getLocation().add(0, .2, 0), 1, 0, 0, 0, .1);
                item.getEntity().getWorld().playSound(item.getEntity().getLocation(), Sounds.BLOCK_NOTE_BLOCK_HAT, 2, (float) (.5 + (j / 40. * 1.5)));
            }
        });
    }
}
