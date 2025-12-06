package io.lumine.mythic.lib.skill.handler.def.vector;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.BuiltinSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.VectorSkillResult;
import io.lumine.mythic.lib.util.TemporaryHandler;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@BuiltinSkillHandler(mods = {"damage", "fangs"})
public class Corrupted_Fangs extends SkillHandler<VectorSkillResult> {
    private final List<DamageType> damageTypes;

    public Corrupted_Fangs(ConfigurationSection config) {
        super(config);

        damageTypes = DamageType.listFromConfig(List.of(DamageType.SKILL, DamageType.MAGIC), config.get("damage_types"));
    }

    @Override
    public @NotNull VectorSkillResult getResult(SkillMetadata meta) {
        return new VectorSkillResult(meta);
    }

    @Override
    public void whenCast(VectorSkillResult result, SkillMetadata skillMeta) {
        Player caster = skillMeta.getCaster().getPlayer();

        caster.getWorld().playSound(caster.getLocation(), Sounds.ENTITY_WITHER_SHOOT, 2, 2);
        new Handler(skillMeta, result.getTarget());
    }

    class Handler extends TemporaryHandler {
        final Set<Integer> entities = new HashSet<>();
        final PlayerMetadata caster;
        final double skillDamage;
        final Location loc;
        final int fangAmount;
        final Vector dir;

        public Handler(SkillMetadata skillMetadata, Vector dir) {
            super(skillMetadata.getCaster().getData());

            this.loc = skillMetadata.getCaster().getPlayer().getLocation();
            this.caster = skillMetadata.getCaster();
            this.skillDamage = skillMetadata.getParameter("damage");
            this.fangAmount = (int) skillMetadata.getParameter("fangs");
            this.dir = normalize(dir.setY(0)).multiply(2);

            runTask(runnable -> runnable.runTaskTimer(MythicLib.plugin, 0, 1));
        }

        private Vector normalize(Vector vec) {
            final var lengthSquared = vec.lengthSquared();
            if (lengthSquared == 0) return new Vector(1, 0, 0);
            return vec.multiply(1 / Math.sqrt(lengthSquared));
        }

        @Override
        protected @Nullable BukkitRunnable newTask() {
            return new BukkitRunnable() {
                double ti = 0;

                public void run() {
                    if (ti++ >= fangAmount) {
                        Handler.this.closeAfter(3 * 20);
                        return;
                    }

                    loc.add(dir);
                    EvokerFangs evokerFangs = (EvokerFangs) loc.getWorld().spawnEntity(loc, EntityType.EVOKER_FANGS);
                    Handler.this.entities.add(evokerFangs.getEntityId());
                }
            };
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void a(EntityDamageByEntityEvent event) {
            if (event.getDamager() instanceof EvokerFangs && entities.contains(event.getDamager().getEntityId())) {
                event.setCancelled(true);

                if (UtilityMethods.canTarget(caster.getPlayer(), event.getEntity()))
                    caster.attack((LivingEntity) event.getEntity(), skillDamage, damageTypes);
            }
        }
    }
}
