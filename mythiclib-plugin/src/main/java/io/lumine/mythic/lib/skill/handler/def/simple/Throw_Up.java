package io.lumine.mythic.lib.skill.handler.def.simple;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.BuiltinSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.util.NoClipItem;
import io.lumine.mythic.lib.util.TemporaryHandler;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@BuiltinSkillHandler(mods = {"duration", "damage"})
public class Throw_Up extends SkillHandler<SimpleSkillResult> {
    private final List<DamageType> damageTypes;

    public Throw_Up(ConfigurationSection config) {
        super(config);

        damageTypes = DamageType.listFromConfig(List.of(DamageType.SKILL, DamageType.PHYSICAL, DamageType.PROJECTILE), config.get("damage_types"));
    }

    @Override
    public @NotNull SimpleSkillResult getResult(SkillMetadata meta) {
        return new SimpleSkillResult();
    }

    @Override
    public void whenCast(SimpleSkillResult result, SkillMetadata skillMeta) {
        double duration = skillMeta.getParameter("duration") * 10;
        double dps = skillMeta.getParameter("damage") / 2;

        Player caster = skillMeta.getCaster().getPlayer();

        TemporaryHandler.timerTask(skillMeta.getCaster().getData(), 2, handler -> new BukkitRunnable() {
            int j = 0;

            public void run() {
                if (++j > duration) {
                    handler.close();
                    return;
                }

                Location loc = caster.getEyeLocation();
                loc.setPitch((float) (loc.getPitch() + (Math.random() - .5) * 30));
                loc.setYaw((float) (loc.getYaw() + (Math.random() - .5) * 30));

                // Deal damage every 10 ticks
                if (j % 5 == 0)
                    for (Entity entity : UtilityMethods.getNearbyChunkEntities(loc))
                        if (entity.getLocation().distanceSquared(loc) < 40 && caster.getEyeLocation().getDirection().angle(entity.getLocation().toVector().subtract(caster.getLocation().toVector())) < Math.PI / 6 && UtilityMethods.canTarget(caster, entity))
                            skillMeta.getCaster().attack((LivingEntity) entity, dps, damageTypes);

                loc.getWorld().playSound(loc, Sounds.ENTITY_ZOMBIE_HURT, 1, 1);

                NoClipItem item = new NoClipItem(caster.getLocation().add(0, 1.2, 0), new ItemStack(Material.ROTTEN_FLESH));
                Bukkit.getScheduler().scheduleSyncDelayedTask(MythicLib.plugin, item::close, 40);
                item.getEntity().setVelocity(loc.getDirection().multiply(.8));
                caster.getWorld().spawnParticle(VParticle.LARGE_SMOKE.get(), caster.getLocation().add(0, 1.2, 0), 0, loc.getDirection().getX(), loc.getDirection().getY(), loc.getDirection().getZ(), 1);
            }
        });
    }
}
