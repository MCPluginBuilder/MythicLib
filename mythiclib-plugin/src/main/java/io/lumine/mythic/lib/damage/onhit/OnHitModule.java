package io.lumine.mythic.lib.damage.onhit;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.event.OnHitEffectEvent;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.script.variable.def.EventVariable;
import io.lumine.mythic.lib.skill.SimpleSkill;
import io.lumine.mythic.lib.skill.trigger.TriggerMetadata;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import io.lumine.mythic.lib.util.FileUtils;
import io.lumine.mythic.lib.util.Lazy;
import io.lumine.mythic.lib.util.config.YamlFile;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

public class OnHitModule {

    /**
     * Using a linked hash map to preserve order
     * provided by the user in the config file
     */
    private final Map<String, OnHitEffect> types = new LinkedHashMap<>();

    private boolean enabled = false;
    private final Listener listener = new CustomListener();

    public void reload() {

        types.clear();

        // [Backwards compatibility] Also copy scripts/on_hit_effects.yml
        if (!new YamlFile("on_hit_effects").exists()) {
            FileUtils.copyDefaultFile(MythicLib.plugin, "on_hit_effects.yml");
            FileUtils.copyDefaultFile(MythicLib.plugin, "script/on_hit_effects.yml");
        }

        FileUtils.copyDefaultFile(MythicLib.plugin, "on_hit_effects.yml");

        // Load on-hit effects from config
        final var config = new YamlFile("on_hit_effects").getContent();
        for (var key : config.getKeys(false))
            try {
                final var effect = new OnHitEffect(config.getConfigurationSection(key));
                registerType(effect);
            } catch (Exception exception) {
                MythicLib.plugin.getLogger().log(Level.WARNING, "Could not load on-hit effect '" + key + "': " + exception.getMessage());
            }

        // Enable or disable
        if (this.enabled && types.isEmpty()) disable();
        else if (!this.enabled && !types.isEmpty()) enable();
    }

    private void disable() {
        Validate.isTrue(this.enabled, "On-hit effect module is already disabled");

        this.enabled = false;
        HandlerList.unregisterAll(this.listener);
    }

    private void enable() {
        Validate.isTrue(!this.enabled, "On-hit effect module is already enabled");

        this.enabled = true;
        Bukkit.getPluginManager().registerEvents(this.listener, MythicLib.plugin);
    }

    public void postload() {
        if (!enabled) return;

        for (var type : types.values()) type.getPostLoadAction().performAction();
    }

    @NotNull
    public OnHitEffect getOnHitEffect(String id) {
        return Objects.requireNonNull(types.get(id), "No on-hit effect with ID '" + id + "'");
    }

    public void registerType(@NotNull OnHitEffect type) {
        types.put(type.getId(), type);
    }

    private class CustomListener implements Listener {

        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        public void onHitAttackEffects(PlayerAttackEvent event) {

            final var playerData = event.getAttacker().getData();

            // Use a lazy value for performance
            // Only transform it to a raw value after cooldown check
            final var lazySkillMeta = Lazy.of(() -> {
                final var temp = new TriggerMetadata(event, TriggerType.ATTACK).toSkillMetadata(SimpleSkill.EMPTY);
                // TODO add field to skillMetadata
                temp.getVariableList().registerVariable(new EventVariable("source_event", event));
                return temp;
            });

            for (var effect : OnHitModule.this.types.values()) {

                // Predamage script
                if (effect.preAttack() != null && !effect.preAttack().cast(lazySkillMeta.get())) continue;

                // Check cooldown
                if (effect.hasCooldown() && playerData.getCooldownMap().isOnCooldown(effect)) continue;

                // Roll chance
                if (effect.getRoll() != null && Math.random() > effect.getRoll().evaluate(lazySkillMeta)) continue;

                // Call Bukkit Event, check for cancellation
                if (!effect.skipsEvent()) {
                    final var bukkitEvent = new OnHitEffectEvent(playerData, effect, event.toBukkit());
                    Bukkit.getPluginManager().callEvent(bukkitEvent);
                    if (bukkitEvent.isCancelled()) continue;
                }

                // Apply cooldown
                if (effect.hasCooldown())
                    playerData.getCooldownMap().applyCooldown(effect, effect.getCooldown().evaluate(lazySkillMeta));

                // Run script
                effect.onAttack().cast(lazySkillMeta.get());
            }
        }
    }
}
