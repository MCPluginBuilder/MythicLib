package io.lumine.mythic.lib.player.potion;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.player.modifier.ModifierMap;
import io.lumine.mythic.lib.version.VPotionEffectType;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Deprecated
public class PermanentPotionEffectMap extends ModifierMap<PermanentPotionEffect> {
    private final Map<PotionEffectType, Integer> maxAmplifier = new HashMap<>();

    public PermanentPotionEffectMap(MMOPlayerData playerData) {
        super(playerData);
    }

    @Override
    public PermanentPotionEffect addModifier(PermanentPotionEffect effect) {
        final @Nullable PermanentPotionEffect prev = super.addModifier(effect);

        // Update cached map
        PotionEffectType key = effect.getEffect();
        maxAmplifier.put(key, Math.max(maxAmplifier.getOrDefault(key, -1), effect.getAmplifier()));

        return prev;
    }

    @Override
    public PermanentPotionEffect removeModifier(UUID uniqueId) {
        final @Nullable PermanentPotionEffect removed = super.removeModifier(uniqueId);

        // Recalculate max
        if (removed != null)
            updateHighestLevel(removed.getEffect());
        return removed;
    }

    public void applyPermanentEffects() {
        Validate.isTrue(getPlayerData().isOnline(), "Player is offline");

        final Player player = getPlayerData().getPlayer();
        maxAmplifier.forEach((type, level) -> {
            int currentAmplifier = player.hasPotionEffect(type) ? player.getPotionEffect(type).getAmplifier() : -1;
            if (level >= currentAmplifier)
                player.addPotionEffect(new PotionEffect(type, getEffectDuration(type), level, false, false));
        });
    }

    /**
     * Takes all the modifiers into account to calculate the highest
     * potion effect level i.e the level the player should have.
     *
     * @param type Potion effect type
     * @return Highest level of potion effect or -1 if none
     */
    public int getHighestLevel(PotionEffectType type) {
        int amplifier = -1;

        for (PermanentPotionEffect perm : getModifiers())
            if (perm.getEffect() == type)
                amplifier = Math.max(amplifier, perm.getAmplifier());

        return amplifier;
    }

    private void updateHighestLevel(PotionEffectType type) {
        int max = getHighestLevel(type);
        if (max == -1)
            maxAmplifier.remove(type);
        else
            maxAmplifier.put(type, max);
    }

    /**
     * The last 5 seconds of nausea are useless, night vision flashes in the
     * last 10 seconds, blindness takes a few seconds to decay as well, and
     * there can be small server lags. It's best to apply a specific duration
     * for every type of permanent effect.
     *
     * @param type Potion effect type
     * @return The duration that MythicLib should be using to give player
     *         "permanent" potion effects, depending on the potion effect type
     */
    private int getEffectDuration(PotionEffectType type) {
        return type.equals(PotionEffectType.NIGHT_VISION)
                || type.equals(VPotionEffectType.NAUSEA.get())
                ? 260 :
                type.equals(PotionEffectType.BLINDNESS) ? 140 : 80;
    }
}
