package io.lumine.mythic.lib.player.modifier;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import io.lumine.mythic.lib.player.particle.ParticleEffect;
import io.lumine.mythic.lib.player.permission.PermissionModifier;
import io.lumine.mythic.lib.player.potion.PermanentPotionEffect;
import io.lumine.mythic.lib.player.skill.PassiveSkill;
import io.lumine.mythic.lib.player.skillmod.SkillModifier;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

/**
 * Player modifiers were defined in 1.2 as a generalization of
 * stat modifiers. These can be any property that may be
 * temporarily assigned to a player including the following:
 * - stat modifier
 * - potion effect
 * - any triggered skill
 * - skill modifier
 *
 * @author Jules
 */
public abstract class PlayerModifier {

    /**
     * Similarly to Bukkit's attribute modifiers, player modifiers have a unique
     * ID for differentiation. However, it is easier to check the source plugin
     * of the modifier using {@link #getKey()}
     */
    private final UUID uniqueId;

    private final ModifierSource source;
    private final EquipmentSlot slot;

    /**
     * Identifier given to skills to differentiate between them.
     * Every plugin like MMOItems has a key to be able to manipulate
     * the triggers that were registered on the player at any time.
     * <p>
     * Unlike the UUID, this key is NOT ALWAYS unique in the case
     * of modifier instances.
     */
    private final String key;

    public PlayerModifier(@NotNull String key, @NotNull EquipmentSlot slot, @NotNull ModifierSource source) {
        this(UUID.randomUUID(), key, slot, source);
    }

    public PlayerModifier(@NotNull UUID uniqueId, @NotNull String key, @NotNull EquipmentSlot slot, @NotNull ModifierSource source) {
        this.uniqueId = uniqueId;
        this.key = key;
        this.slot = slot;
        this.source = source;
    }

    @NotNull
    public UUID getUniqueId() {
        return uniqueId;
    }

    /**
     * Modifier keys are NOT unique, they help the developer
     * to easily filter modifiers added by their plugin.
     * UUIDs are unique though and are used for internal mapping.
     *
     * @return The key of the modifier.
     */
    @NotNull
    public String getKey() {
        return key;
    }

    @NotNull
    public EquipmentSlot getSlot() {
        return slot;
    }

    @NotNull
    public ModifierSource getSource() {
        return source;
    }

    public abstract void register(@NotNull MMOPlayerData playerData);

    public abstract void unregister(@NotNull MMOPlayerData playerData);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerModifier that = (PlayerModifier) o;
        return uniqueId.equals(that.uniqueId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueId);
    }

    private static final Map<String, Function<ConfigObject, PlayerModifier>> BY_KEY = new HashMap<>();

    public static void registerPlayerModifierType(@NotNull String key, @NotNull Function<ConfigObject, PlayerModifier> resolver, String... aliases) {
        Validate.notNull(key, "Key cannot be null");
        Validate.notNull(resolver, "Resolver cannot be null");

        BY_KEY.put(key, resolver);
        for (String alias : aliases) {
            Validate.notNull(alias, "Alias cannot be null");
            BY_KEY.put(alias, resolver);
        }
    }

    static {
        registerPlayerModifierType("particle_effect", ParticleEffect::fromConfig, "particle", "particles");
        registerPlayerModifierType("potion_effect", PermanentPotionEffect::fromConfig, "potion", "potioneffect", "pot");
        registerPlayerModifierType("stat", StatModifier::new, "stats", "mmostat");
        registerPlayerModifierType("skill", PassiveSkill::fromConfig, "ability", "passive_skill", "passive");
        registerPlayerModifierType("skill_modifier", SkillModifier::fromConfig, "skill_mod", "skillmod", "skillmodifier");
        registerPlayerModifierType("permission", PermissionModifier::fromConfig, "perm", "perm_node", "permnode");
    }

    @NotNull
    public static PlayerModifier from(@NotNull ConfigObject config) {
        Validate.notNull(config, "Config cannot be null");

        // Find player modifier type
        String configKey = config.getKey();
        if (configKey == null) configKey = config.getString("type");

        Function<ConfigObject, PlayerModifier> found = BY_KEY.get(configKey);
        Validate.notNull(found, String.format("Could not match player modifier type to %s", configKey));
        return found.apply(config);
    }
}
