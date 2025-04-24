package io.lumine.mythic.lib.player.particle;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.modifier.PlayerModifier;
import io.lumine.mythic.lib.util.Closeable;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public abstract class ParticleEffect extends PlayerModifier implements Closeable {
    protected final ParticleInformation particle;

    @Nullable(value = "not null when registered")
    protected Player player;

    @Nullable(value = "not null when started")
    private BukkitTask runningTask;

    public ParticleEffect(String key, ParticleInformation particle) {
        super(key, EquipmentSlot.OTHER, ModifierSource.OTHER);

        this.particle = particle;
    }

    public ParticleEffect(ConfigObject obj) {
        super(obj.getString("key"), EquipmentSlot.OTHER, ModifierSource.OTHER);

        particle = new ParticleInformation(obj.getObject("particle"));
    }

    @NotNull
    public ParticleInformation getParticle() {
        return particle;
    }

    //region Bukkit Task

    public boolean isStarted() {
        return runningTask != null;
    }

    @NotNull
    public ParticleEffect start() {
        if (runningTask != null) return this;

        Validate.notNull(player, "Player cannot be null");
        runningTask = Bukkit.getScheduler().runTaskTimer(MythicLib.plugin, this::tick, 0, getType().getPeriod());
        return this;
    }

    public void stop() {
        if (runningTask == null) return;

        runningTask.cancel();
        runningTask = null;
    }

    //endregion

    protected double resolveModifier(Map<String, Double> modifiers, String path) {
        return modifiers.getOrDefault(path, getType().getDefaultModifierValue(path));
    }

    /**
     * What the particle effect actually does
     */
    public abstract void tick();

    public abstract ParticleEffectType getType();

    //region Modifier

    public void bindPlayer(@NotNull Player player) {
        this.player = player;
    }

    @Override
    public void register(MMOPlayerData playerData) {
        playerData.getParticleEffectMap().addModifier(this);
    }

    @Override
    public void unregister(MMOPlayerData playerData) {
        playerData.getParticleEffectMap().removeModifier(getUniqueId());
    }

    @Override
    public void close() {
        stop();
    }

    //endregion

    @NotNull
    public static ParticleEffect fromConfig(@NotNull ConfigObject obj) {
        ParticleEffectType type = ParticleEffectType.get(UtilityMethods.enumName(obj.getString("particle-effect")));
        return type.getParser().apply(obj);
    }
}
