package io.lumine.mythic.lib.skill.trigger;

import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.script.variable.VariableList;
import io.lumine.mythic.lib.script.variable.VariableScope;
import io.lumine.mythic.lib.skill.Skill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Contains sufficient information in order to trigger either
 * one or multiple skills.
 * <p>
 * This class proposes redundancies to reduce the number of useless
 * stat map lookups. You can provide a player stat cache or let the
 * class generate it when turning this class into a skill metadata.
 *
 * @author jules
 */
public class TriggerMetadata {
    private final MMOPlayerData playerData;
    private final TriggerType triggerType;
    private final EquipmentSlot actionHand;

    @NotNull
    private final Location source;

    @Nullable
    private final Entity target;

    @Nullable
    private final AttackMetadata attack;

    @Nullable
    private final Location targetLocation;

    /**
     * The instanciation of a PlayerMetadata can be quite intensive in
     * computation, especially because it can be up to 20 times a second
     * for every player in the server. It performs a full stat map lookup
     * and caches final stat values.
     * <p>
     * For this reason, it's best to NOT generate the PlayerMetadata unless
     * it has been provided beforehand in the constructor, until it's finally
     * asked for in the getter.
     */
    @Nullable
    private PlayerMetadata caster;

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

    /**
     * The player responsible for the attack is the one triggering the skill.
     */
    public TriggerMetadata(@NotNull PlayerAttackEvent attackEvent, @NotNull TriggerType triggerType) {
        this(attackEvent.getAttacker(), triggerType, attackEvent.getEntity(), attackEvent.getAttack());
    }

    public TriggerMetadata(@NotNull PlayerMetadata caster, @NotNull TriggerType triggerType, @Nullable Entity target, @Nullable AttackMetadata attack) {
        this(caster.getData(), triggerType, caster.getActionHand(), null, target, null, attack, caster);
    }

    public TriggerMetadata(@NotNull MMOPlayerData playerData, @NotNull TriggerType triggerType, @Nullable EquipmentSlot actionHand, @Nullable Location source, @Nullable Entity target, @Nullable Location targetLocation, @Nullable AttackMetadata attack, @Nullable PlayerMetadata caster) {
        this.playerData = Objects.requireNonNull(playerData);
        this.triggerType = Objects.requireNonNull(triggerType);
        this.actionHand = Objects.requireNonNullElse(actionHand, EquipmentSlot.MAIN_HAND);
        this.source = Objects.requireNonNullElse(source, playerData.getPlayer().getLocation());
        this.target = target;
        this.targetLocation = targetLocation;
        this.attack = attack;
        this.caster = caster;
    }

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
        this((PlayerMetadata) attack.getAttacker(), TriggerType.API, target, attack);
    }

    @Deprecated
    public TriggerMetadata(@NotNull PlayerMetadata caster) {
        this(caster.getData(), TriggerType.API, EquipmentSlot.MAIN_HAND, null, null, null, null, caster);
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

    @Nullable
    public Entity getTarget() {
        return target;
    }

    @Nullable
    public Location getTargetLocation() {
        return targetLocation;
    }

    @Nullable
    public AttackMetadata getAttack() {
        return attack;
    }

    @NotNull
    public PlayerMetadata getCachedPlayerMetadata() {
        if (caster == null) caster = playerData.getStatMap().cache(actionHand);
        return caster;
    }

    /**
     * Called when casting a skill. This transfers all the useful
     * information into a skill metadata
     *
     * @param cast Skill being cast
     * @return Skill cast information containing all the previous information
     */
    @NotNull
    public SkillMetadata toSkillMetadata(Skill cast) {
        return new SkillMetadata(triggerType, cast, getCachedPlayerMetadata(), new VariableList(VariableScope.SKILL), source, targetLocation, target, null, attack);
    }
}
