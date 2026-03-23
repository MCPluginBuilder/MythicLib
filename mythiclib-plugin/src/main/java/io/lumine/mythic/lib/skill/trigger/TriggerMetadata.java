package io.lumine.mythic.lib.skill.trigger;

import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.skill.Skill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.Lazy;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Contains sufficient information in order to trigger either
 * one or multiple skills. This is an utility class for calling
 * skills on various events.
 * <p>
 * The most prominent feature of this class is the use of a Lazy
 * value to generate a SkillMetadata only when needed. If the player
 * has no skill with said trigger, no SkillMetadata is ever created.
 * <p>
 * This class proposes redundancies to reduce the number of useless
 * stat map lookups. You can provide a player stat cache or let the
 * class generate it when turning this class into a skill metadata.
 *
 * @author jules
 */
// TODO split SkillMetadata into ScriptMetadata and make initialization very cheap. Remove this class.
public class TriggerMetadata {
    private final MMOPlayerData playerData;
    private final TriggerType triggerType;
    private final EquipmentSlot actionHand;
    private final Lazy<SkillMetadata> skillMetaGenerator;

    public TriggerMetadata(@NotNull MMOPlayerData playerData, @NotNull TriggerType triggerType) {
        this(playerData, triggerType, (Entity) null);
    }

    public TriggerMetadata(@NotNull MMOPlayerData playerData, @NotNull TriggerType triggerType, @Nullable Entity target) {
        this(playerData, triggerType, EquipmentSlot.MAIN_HAND, null, target, null, null, null);
    }

    public TriggerMetadata(@NotNull MMOPlayerData playerData, @NotNull TriggerType triggerType, @Nullable Location targetLocation) {
        this(playerData, triggerType, EquipmentSlot.MAIN_HAND, null, null, targetLocation, null, null);
    }

    public TriggerMetadata(@NotNull MMOPlayerData playerData, @NotNull TriggerType triggerType, @NotNull Location source, @Nullable Location targetLocation) {
        this(playerData, triggerType, EquipmentSlot.MAIN_HAND, source, null, targetLocation, null, null);
    }

    public TriggerMetadata(@NotNull PlayerAttackEvent attackEvent, @NotNull TriggerType triggerType) {
        this(attackEvent.getAttacker(), triggerType, attackEvent.getEntity(), attackEvent.getAttack());
    }

    public TriggerMetadata(@NotNull PlayerMetadata caster, @NotNull TriggerType triggerType, @Nullable Entity target, @Nullable AttackMetadata attack) {
        this(caster.getData(), triggerType, caster.getActionHand(), null, target, null, attack, caster);
    }

    public TriggerMetadata(@NotNull MMOPlayerData playerData,
                           @NotNull TriggerType triggerType,
                           @Nullable EquipmentSlot actionHand,
                           @Nullable Location source,
                           @Nullable Entity target,
                           @Nullable Location targetLocation,
                           @Nullable AttackMetadata attack,
                           @Nullable PlayerMetadata caster) {
        this(playerData, triggerType, actionHand, Lazy.of(() -> SkillMetadata.of(playerData, actionHand, source, target, targetLocation, attack, caster, null)));
    }

    public TriggerMetadata(@NotNull MMOPlayerData playerData,
                           @NotNull TriggerType triggerType,
                           @Nullable EquipmentSlot actionHand,
                           @NotNull Lazy<SkillMetadata> generator) {
        this.playerData = Objects.requireNonNull(playerData);
        this.triggerType = Objects.requireNonNull(triggerType);
        this.actionHand = Objects.requireNonNullElse(actionHand, EquipmentSlot.MAIN_HAND);
        this.skillMetaGenerator = generator;
    }

    @NotNull
    public MMOPlayerData getPlayerData() {
        return playerData;
    }

    @NotNull
    public TriggerType getTriggerType() {
        return triggerType;
    }

    @NotNull
    public EquipmentSlot getActionHand() {
        return actionHand;
    }

    @NotNull
    public SkillMetadata toSkillMetadata(@NotNull Skill cast) {
        return this.skillMetaGenerator.get().clone(cast);
    }

    //region Deprecated

    @Deprecated
    public TriggerMetadata(@NotNull PlayerAttackEvent attackEvent) {
        this(attackEvent, TriggerType.API);
    }

    @Deprecated
    public TriggerMetadata(@NotNull PlayerMetadata caster, @Nullable Entity target, @Nullable AttackMetadata attack) {
        this(caster, TriggerType.API, target, attack);
    }

    @Deprecated
    public TriggerMetadata(@NotNull AttackMetadata attack, @Nullable Entity target) {
        this((PlayerMetadata) Objects.requireNonNull(attack.getAttacker()), TriggerType.API, target, attack);
    }

    @Deprecated
    public TriggerMetadata(@NotNull PlayerMetadata caster) {
        this(caster.getData(), TriggerType.API, EquipmentSlot.MAIN_HAND, null, null, null, null, caster);
    }

    //endregion
}
