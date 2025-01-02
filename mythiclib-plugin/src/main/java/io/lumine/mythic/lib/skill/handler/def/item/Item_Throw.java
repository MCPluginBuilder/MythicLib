package io.lumine.mythic.lib.skill.handler.def.item;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.ItemSkillResult;
import io.lumine.mythic.lib.util.NoClipItem;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class Item_Throw extends SkillHandler<ItemSkillResult> {
    public Item_Throw() {
        super();

        registerModifiers("damage", "force");
    }

    @Override
    public ItemSkillResult getResult(SkillMetadata meta) {
        return new ItemSkillResult(meta);
    }

    @Override
    public void whenCast(ItemSkillResult result, SkillMetadata skillMeta) {
        ItemStack itemStack = result.getItem();
        Player caster = skillMeta.getCaster().getPlayer();

        final NoClipItem item = new NoClipItem(caster.getLocation().add(0, 1.2, 0), itemStack);
        item.getEntity().setVelocity(result.getTarget().multiply(1.5 * skillMeta.getParameter("force")));
        caster.getWorld().playSound(caster.getLocation(), Sounds.ENTITY_SNOWBALL_THROW, 1, 0);
        new BukkitRunnable() {
            double ti = 0;

            public void run() {
                if (ti++ > 20 || item.getEntity().isDead()) {
                    item.close();
                    cancel();
                }

                item.getEntity().getWorld().spawnParticle(Particle.CRIT, item.getEntity().getLocation(), 0);
                for (Entity target : item.getEntity().getNearbyEntities(1, 1, 1))
                    if (UtilityMethods.canTarget(caster, target)) {
                        skillMeta.getCaster().attack((LivingEntity) target, skillMeta.getParameter("damage"), DamageType.SKILL, DamageType.PHYSICAL);
                        item.close();
                        cancel();
                    }
            }
        }.runTaskTimer(MythicLib.plugin, 0, 1);
    }
}
