package io.lumine.mythic.lib.comp.mythicmobs;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.compatibility.MythicLibSupport;
import io.lumine.mythic.bukkit.events.MythicConditionLoadEvent;
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import io.lumine.mythic.bukkit.events.MythicReloadedEvent;
import io.lumine.mythic.bukkit.utils.Events;
import io.lumine.mythic.bukkit.utils.plugin.ReloadableModule;
import io.lumine.mythic.core.skills.placeholders.Placeholder;
import io.lumine.mythic.core.skills.variables.Variable;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.StatMap;
import io.lumine.mythic.lib.comp.mythicmobs.condition.CanTargetCondition;
import io.lumine.mythic.lib.comp.mythicmobs.mechanic.MMODamageMechanic;
import io.lumine.mythic.lib.comp.mythicmobs.mechanic.MultiplyDamageMechanic;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.manager.StatManager;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.player.cooldown.CooldownInfo;
import io.lumine.mythic.lib.player.cooldown.CooldownMap;
import io.lumine.mythic.lib.skill.result.MythicMobsSkillResult;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Optional;

public class MythicLibSupportImpl extends ReloadableModule<MythicBukkit> implements MythicLibSupport {
    public MythicLibSupportImpl() {
        super(MythicBukkit.inst());
    }

    @Override
    public void load(MythicBukkit plugin) {

        Events.subscribe(MythicMechanicLoadEvent.class).handler(event -> {

            // MMO damage mechanic
            if (event.getMechanicName().equalsIgnoreCase("mmodamage") || event.getMechanicName().equalsIgnoreCase("mmod"))
                event.register(new MMODamageMechanic(event.getContainer().getManager(), event.getContainer().getFile(), event.getMechanicName(), event.getConfig()));

            // Multiply damage mechanic
            if (event.getMechanicName().equalsIgnoreCase("multiplydamage"))
                event.register(new MultiplyDamageMechanic(event.getConfig()));

        });

        Events.subscribe(MythicConditionLoadEvent.class).handler(event -> {

            // Can Target condition
            if (event.getConditionName().equalsIgnoreCase("canTarget"))
                event.register(new CanTargetCondition(event.getConditionName(), event.getConfig()));

        });

        // Register skill placeholders on reload
        Events.subscribe(MythicReloadedEvent.class).handler(event -> registerPlaceholders());

        // Register skill placeholders
        registerPlaceholders();
    }

    @Override
    public void unload() {
    }

    private void registerPlaceholders() {

        // MMOItems/MMOCore skill modifier
        MythicBukkit.inst().getPlaceholderManager().register("modifier", Placeholder.meta((metadata, arg) -> {
            if (!(metadata instanceof SkillMetadata))
                throw new RuntimeException("Cannot use this placeholder outside of skill");

            final Variable var = ((SkillMetadata) metadata).getVariables().get(MythicMobsSkillResult.MMO_SKILLMETADATA_TAG);
            Validate.notNull(var, "Could not find MythicLib skill variable");
            final io.lumine.mythic.lib.skill.SkillMetadata mmoMetadata = (io.lumine.mythic.lib.skill.SkillMetadata) var.get();
            return String.valueOf(mmoMetadata.getParameter(arg));
        }));

        // MMOItems/MMOCore skill modifier (as int)
        MythicBukkit.inst().getPlaceholderManager().register("modifier.int", Placeholder.meta((metadata, arg) -> {
            if (!(metadata instanceof SkillMetadata))
                throw new RuntimeException("Cannot use this placeholder outside of skill");

            final Variable var = ((SkillMetadata) metadata).getVariables().get(MythicMobsSkillResult.MMO_SKILLMETADATA_TAG);
            Validate.notNull(var, "Could not find MythicLib skill variable");
            final io.lumine.mythic.lib.skill.SkillMetadata mmoMetadata = (io.lumine.mythic.lib.skill.SkillMetadata) var.get();
            return String.valueOf((int) mmoMetadata.getParameter(arg));
        }));

        // MythicLib current damage amount
        MythicBukkit.inst().getPlaceholderManager().register("mmodamage", Placeholder.meta((metadata, arg) -> {
            if (!(metadata instanceof SkillMetadata))
                throw new RuntimeException("Cannot use this placeholder outside of skill");

            final SkillMetadata skillMeta = (SkillMetadata) metadata;
            final Optional<AbstractEntity> damagedOpt = skillMeta.getEntityTargets().stream().findFirst();
            Validate.isTrue(damagedOpt.isPresent(), "Could not find target entity");
            final Entity damaged = damagedOpt.get().getBukkitEntity();

            final AttackMetadata attackMeta = MythicLib.plugin.getDamage().findAttack(new EntityDamageEvent(damaged, EntityDamageEvent.DamageCause.CUSTOM, 0));
            Validate.notNull(attackMeta, "Entity not being attacked");

            if (arg != null && !arg.isEmpty()) {
                final DamageType type = DamageType.valueOf(UtilityMethods.enumName(arg));
                return String.valueOf(attackMeta.getDamage().getDamage(type));
            }

            return String.valueOf(attackMeta.getDamage().getDamage());
        }));

        // Stats
        MythicBukkit.inst().getPlaceholderManager().register("mmostat", Placeholder.meta((metadata, arg) -> {

            /**
             * This has to be checked first before trying to get the stat value using
             * the MMOPlayerData. The stat map provided by that MMOStatMap variable is a CACHED
             * stat map that corresponds to the player stat by the time where he cast
             * the skill.
             *
             * If a player casts a skill and quickly switches items in hand, he can change
             * his stats and by the time his damaging skill hits the target, his stats would
             * have changed which can give him a huge advantage if well used, hence the
             * need of caching the player stats whenever casting an ability.
             */
            if (metadata instanceof SkillMetadata && ((SkillMetadata) metadata).getVariables().has(MythicMobsSkillResult.MMO_SKILLMETADATA_TAG)) {
                final Variable var = ((SkillMetadata) metadata).getVariables().get(MythicMobsSkillResult.MMO_SKILLMETADATA_TAG);
                final io.lumine.mythic.lib.skill.SkillMetadata mmoMetadata = (io.lumine.mythic.lib.skill.SkillMetadata) var.get();
                final PlayerMetadata caster = mmoMetadata.getCaster();
                final String statName = arg.toUpperCase();
                return StatManager.format(statName, caster.getStat(statName));
            }

            final SkillCaster caster = metadata.getCaster();
            if (!caster.getEntity().isPlayer()) return "0";

            final MMOPlayerData playerData = MMOPlayerData.get(caster.getEntity().getUniqueId());
            return MythicLib.inst().getMMOConfig().decimals.format(playerData.getStatMap().getStat(arg.toUpperCase()));
        }));

        // Target Stats
        MythicBukkit.inst().getPlaceholderManager().register("target.stat", Placeholder.entity((entity, arg) -> {
            if (!entity.isPlayer()) return "0";

            final StatMap statMap = MMOPlayerData.get(entity.getUniqueId()).getStatMap();
            final String statName = arg.toUpperCase();
            return StatManager.format(statName, statMap.getStat(statName));
        }));

        // Cooldowns
        MythicBukkit.inst().getPlaceholderManager().register("cooldown", Placeholder.meta((metadata, arg) -> {
            final CooldownMap cooldownMap = MMOPlayerData.get(metadata.getCaster().getEntity().getUniqueId()).getCooldownMap();
            final CooldownInfo info = cooldownMap.getInfo(arg);
            return String.valueOf(info == null ? 0 : info.getRemaining() / 1000d);
        }));
    }
}
