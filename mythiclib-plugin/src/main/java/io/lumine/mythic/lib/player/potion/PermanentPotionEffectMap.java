package io.lumine.mythic.lib.player.potion;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.player.modifier.ModifierMap;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nullable;
import java.util.*;

public class PermanentPotionEffectMap extends ModifierMap<PermanentPotionEffect> {
    private final List<PotionEffect> bukkitEffectCache = new ArrayList<>();

    public PermanentPotionEffectMap(MMOPlayerData playerData) {
        super(playerData);
    }

    @Override
    public PermanentPotionEffect addModifier(PermanentPotionEffect effect) {
        final @Nullable PermanentPotionEffect previous = super.addModifier(effect);
        resolvePermanentEffects();
        return previous;
    }

    @Override
    public PermanentPotionEffect removeModifier(UUID uniqueId) {
        final @Nullable PermanentPotionEffect removed = super.removeModifier(uniqueId);
        resolvePermanentEffects();
        return removed;
    }

    public void applyPermanentPotionEffects() {
        bukkitEffectCache.forEach(effect -> playerData.getPlayer().addPotionEffect(effect));
    }

    // TODO make it not reset everything everytime
    // TODO there's probably an algorithmically better solution
    private void resolvePermanentEffects() {

        // Resolve highest levels
        Map<PotionEffectType, Integer> highestLevels = new HashMap<>();
        for (PermanentPotionEffect entry : this.modifiers.values())
            highestLevels.merge(entry.getEffect(), entry.getAmplifier(), Integer::max);

        // Cache Bukkit potion effects
        bukkitEffectCache.clear();
        highestLevels.forEach((type, amplifier) -> bukkitEffectCache.add(new PotionEffect(type, UtilityMethods.getPermanentEffectDuration(type), amplifier)));
    }
}
