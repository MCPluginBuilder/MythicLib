package io.lumine.mythic.lib.player.skill;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.modifier.PlayerModifier;
import io.lumine.mythic.lib.skill.SimpleSkill;
import io.lumine.mythic.lib.skill.Skill;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * There is one PassiveSkill instance per passive skill the player has.
 * A passive skill can be registered by MMOItems items or MMOCore passive skills.
 * <p>
 * Any skill registered by MMOItems is considered passive. The important
 * distinction here is rather silent skills; see {@link TriggerType#isSilent()}
 * <p>
 * Skills that are cast in MMOCore using the casting mode are also
 * active and any skill that has to be triggered is passive. It's
 * much less confusing in MMOCore than in MI.
 * <p>
 * The only active skills are the ones cast by MMOCore using the CAST trigger type.
 *
 * @author Jules
 */
public class PassiveSkill extends PlayerModifier {

    /**
     * Skill cast whenever the action is performed
     */
    private final Skill triggered;
    private final TriggerType trigger;

    public PassiveSkill(String key, TriggerType trigger, Skill triggered) {
        this(key, trigger, triggered, EquipmentSlot.OTHER, ModifierSource.OTHER);
    }

    /**
     * @param key            A key like 'item' or 'itemSet' indicating what is giving a triggered skill to the player.
     *                       There can be multiple skills with the same key, it's not a unique identifier.
     *                       It can be later used to isolate and unregister skills with a certain key.
     * @param trigger        What action triggers this skill
     * @param triggered      The skill being triggered
     * @param equipmentSlot  The equipment slot granting this passive skill
     * @param modifierSource The source of the passive skill
     */
    public PassiveSkill(@NotNull String key, @NotNull TriggerType trigger, @NotNull Skill triggered, @NotNull EquipmentSlot equipmentSlot, @NotNull ModifierSource modifierSource) {
        super(key, equipmentSlot, modifierSource);

        this.trigger = trigger;
        this.triggered = triggered;
    }

    public PassiveSkill(ConfigObject obj) {
        super(obj.getString("key"), EquipmentSlot.OTHER, ModifierSource.OTHER);

        triggered = new SimpleSkill(MythicLib.plugin.getSkills().getHandlerOrThrow(obj.getString("skill")));
        trigger = TriggerType.valueOf(obj.getString("trigger"));
    }

    /**
     * @see Skill#getTrigger()
     * @deprecated
     */
    @Deprecated
    public PassiveSkill(String key, Skill triggered, EquipmentSlot equipmentSlot, ModifierSource modifierSource) {
        super(key, equipmentSlot, modifierSource);

        TriggerType backwardsCompatibleTrigger = triggered.getTrigger();
        Validate.isTrue(backwardsCompatibleTrigger.isPassive(), "Skill is active");
        this.triggered = Objects.requireNonNull(triggered, "Skill cannot be null");
        this.trigger = backwardsCompatibleTrigger;
    }

    @NotNull
    public Skill getTriggeredSkill() {
        return triggered;
    }

    /**
     * Zero when skill does not use a timer. The user inputs
     * it in ticks but that field is expressed in milliseconds
     */
    public long getTimerPeriod() {
        return Math.max(1, (long) triggered.getParameter("timer")) * 50;
    }

    /**
     * @see #getTrigger()
     * @deprecated
     */
    @Deprecated
    public TriggerType getType() {
        return getTrigger();
    }

    @NotNull
    public TriggerType getTrigger() {
        return trigger;
    }

    @Override
    public void register(MMOPlayerData playerData) {
        playerData.getPassiveSkillMap().addModifier(this);
    }

    @Override
    public void unregister(MMOPlayerData playerData) {
        playerData.getPassiveSkillMap().removeModifier(getUniqueId());
    }

    @NotNull
    public static PassiveSkill fromConfig(@NotNull ConfigObject config) {
        return new PassiveSkill(config);
    }
}
