package io.lumine.mythic.lib.player.particle;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.player.modifier.ModifierMap;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ParticleEffectMap extends ModifierMap<ParticleEffect> {
    public ParticleEffectMap(MMOPlayerData playerData) {
        super(playerData);
    }

    @Override
    public @Nullable ParticleEffect addModifier(ParticleEffect modifier) {

        // Throws an error otherwise
        modifier.bindPlayer(playerData.getPlayer());

        if (modifier.getType().hasPriority()) {
            this.modifiers.values().forEach(ParticleEffect::stop); // If priority, cancel others
            modifier.start(); // Start this one
        } else if (this.modifiers.values().stream().noneMatch(effect -> effect.getType().hasPriority()))
            modifier.start(); // If none are running with priority, start this one

        return super.addModifier(modifier);
    }

    @Override
    public @Nullable ParticleEffect removeModifier(UUID uuid) {
        ParticleEffect removed = super.removeModifier(uuid);

        // If removed one that has priority, cast one
        if (removed != null && removed.getType().hasPriority()) startOneAgain();

        return removed;
    }

    private void startOneAgain() {

        // Start only one with priority
        for (ParticleEffect remaining : this.modifiers.values())
            if (remaining.getType().hasPriority()) {
                remaining.start();
                return;
            }

        // Start all of them
        this.modifiers.values().forEach(ParticleEffect::start);
    }
}
