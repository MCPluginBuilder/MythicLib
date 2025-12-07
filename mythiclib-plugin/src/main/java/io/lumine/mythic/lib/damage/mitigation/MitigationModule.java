package io.lumine.mythic.lib.damage.mitigation;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.event.AttackEvent;
import io.lumine.mythic.lib.api.event.DamageMitigationEvent;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.module.ListenerToggle;
import io.lumine.mythic.lib.module.Module;
import io.lumine.mythic.lib.module.ModuleInfo;
import io.lumine.mythic.lib.module.ModuleListener;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.FileUtils;
import io.lumine.mythic.lib.util.Lazy;
import io.lumine.mythic.lib.util.config.YamlFile;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

@ModuleInfo(key = "damage_mitigation")
public class MitigationModule extends Module {

    /**
     * Using a linked hash map to preserve order
     * provided by the user in the config file
     */
    private final Map<String, MitigationType> registry = new LinkedHashMap<>();

    @ModuleListener
    final ListenerToggle mainListener = new ListenerToggle(this, InternalListener::new);

    public MitigationModule(MythicLib plugin) {
        super(plugin);
    }

    @Override
    protected void onReset() {
        registry.clear();
    }

    @Override
    protected void onReload() {

        // [Backwards compatibility] Also copy scripts/mitigation_types.yml
        if (!new YamlFile("mitigation_types").exists()) {
            FileUtils.copyDefaultFile(MythicLib.plugin, "mitigation_types.yml");
            FileUtils.copyDefaultFile(MythicLib.plugin, "script/mitigation_types.yml");
        }

        FileUtils.copyDefaultFile(MythicLib.plugin, "mitigation_types.yml");

        // Load mitigation types from config
        final var config = new YamlFile("mitigation_types").getContent();
        for (var key : config.getKeys(false))
            try {
                final var type = new MitigationType(config.getConfigurationSection(key));
                registry.put(type.getId(), type);
            } catch (Exception exception) {
                MythicLib.plugin.getLogger().log(Level.WARNING, "Could not load mitigation type '" + key + "': " + exception.getMessage());
            }

        // Enable or disable listener
        mainListener.toggle(!registry.isEmpty());
    }

    @NotNull
    public MitigationType getMitigationType(String id) {
        return Objects.requireNonNull(registry.get(id), "No mitigation type with ID '" + id + "'");
    }

    private class InternalListener implements Listener {

        @EventHandler
        public void applyMitigationTypes(@NotNull AttackEvent event) {

            final var playerData = MMOPlayerData.getOrNull(event.getEntity());
            if (playerData == null) return;

            // Use a lazy value for performance
            // Only transform it to a raw value after cooldown check
            final var lazySkillMeta = Lazy.of(() -> {
                final var attackerProvider = event.getAttack().getAttacker();
                final var attacker = attackerProvider != null ? attackerProvider.getEntity() : null;
                return SkillMetadata.of(playerData, EquipmentSlot.MAIN_HAND, playerData.getPlayer().getLocation(), attacker, null, event.getAttack(), null, event);
            });

            for (var type : MitigationModule.this.registry.values()) {

                // Pre-damage script
                if (type.preDamage() != null && !type.preDamage().cast(lazySkillMeta.get()).isSuccessful()) continue;

                // Check cooldown
                if (type.hasCooldown() && playerData.getCooldownMap().isOnCooldown(type)) continue;

                // Roll chance
                if (type.getRoll() != null && Math.random() > type.getRoll().evaluate(lazySkillMeta)) continue;

                // Call Bukkit Event, check for cancellation
                // [Backwards compatibility] Use previously defined events
                if (!type.skipsEvent()) {
                    final DamageMitigationEvent bukkitEvent;
                    if (type.asLegacy() != null)
                        bukkitEvent = type.asLegacy().generateLegacyEvent(playerData, event.toBukkit(), type);
                    else bukkitEvent = new DamageMitigationEvent(playerData, type, event.toBukkit());
                    Bukkit.getPluginManager().callEvent(bukkitEvent);
                    if (bukkitEvent.isCancelled()) continue;
                }

                // Apply cooldown
                if (type.hasCooldown())
                    playerData.getCooldownMap().applyCooldown(type, type.getCooldown().evaluate(lazySkillMeta));

                // Run script
                type.onDamage().cast(lazySkillMeta.get());
            }
        }
    }
}
