package io.lumine.mythic.lib.damage.onhit;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.event.OnHitEffectEvent;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.module.ListenerToggle;
import io.lumine.mythic.lib.module.Module;
import io.lumine.mythic.lib.module.ModuleInfo;
import io.lumine.mythic.lib.module.ModuleListener;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.FileUtils;
import io.lumine.mythic.lib.util.config.YamlFile;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

@ModuleInfo(key = "on_hit_effects")
public class OnHitModule extends Module {

    /**
     * Using a linked hash map to preserve order
     * provided by the user in the config file
     */
    private final Map<String, OnHitEffect> registry = new LinkedHashMap<>();

    @ModuleListener
    final ListenerToggle mainListener = new ListenerToggle(this, InternalListener::new);

    public OnHitModule(MythicLib plugin) {
        super(plugin);
    }

    @Override
    protected void onReset() {
        registry.clear();
    }

    @Override
    protected void onReload() {

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
                registry.put(effect.getId(), effect);
            } catch (Exception exception) {
                MythicLib.plugin.getLogger().log(Level.WARNING, "Could not load on-hit effect '" + key + "': " + exception.getMessage());
            }

        // Enable or disable listener
        this.mainListener.toggle(!registry.isEmpty());
    }

    @NotNull
    public OnHitEffect getOnHitEffect(String id) {
        return Objects.requireNonNull(registry.get(id), "No on-hit effect with ID '" + id + "'");
    }

    private class InternalListener implements Listener {

        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        public void onHitAttackEffects(PlayerAttackEvent event) {

            final var playerData = event.getAttacker().getData();

            // Use a lazy value for performance
            // Only transform it to a raw value after cooldown check
            final var lazySkillMeta = SkillMetadata.lazyOf(event);

            for (var effect : OnHitModule.this.registry.values()) {

                // Predamage script
                if (effect.preAttack() != null && !effect.preAttack().cast(lazySkillMeta.get()).isSuccessful()) continue;

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
