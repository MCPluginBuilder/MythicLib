package io.lumine.mythic.lib.damage.mitigation;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.event.AttackEvent;
import io.lumine.mythic.lib.api.event.DamageMitigationEvent;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
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
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

public class MitigationModule {

    /**
     * Using a linked hash map to preserve order
     * provided by the user in the config file
     */
    private final Map<String, MitigationType> types = new LinkedHashMap<>();

    private boolean enabled = false;
    private final Listener listener = new CustomListener();

    public void reload() {

        types.clear();

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
                registerType(type);
            } catch (Exception exception) {
                MythicLib.plugin.getLogger().log(Level.WARNING, "Could not load mitigation type '" + key + "': " + exception.getMessage());
            }

        // Enable or disable
        if (this.enabled && types.isEmpty()) disable();
        else if (!this.enabled && !types.isEmpty()) enable();
    }

    private void disable() {
        Validate.isTrue(this.enabled, "Damage Mitigation module is already disabled");

        this.enabled = false;
        HandlerList.unregisterAll(this.listener);
    }

    private void enable() {
        Validate.isTrue(!this.enabled, "Damage Mitigation module is already enabled");

        this.enabled = true;
        Bukkit.getPluginManager().registerEvents(this.listener, MythicLib.plugin);
    }

    public void postload() {
        if (!enabled) return;

        for (var type : types.values()) type.getPostLoadAction().performAction();
    }

    @NotNull
    public MitigationType getMitigationType(String id) {
        return Objects.requireNonNull(types.get(id), "No mitigation type with ID '" + id + "'");
    }

    public void registerType(@NotNull MitigationType type) {
        types.put(type.getId(), type);
    }

    private class CustomListener implements Listener {

        @EventHandler
        public void applyMitigationTypes(@NotNull AttackEvent event) {

            final var playerData = MMOPlayerData.getOrNull(event.getEntity());
            if (playerData == null) return;

            // Use a lazy value for performance
            // Only transform it to a raw value after cooldown check
            final var lazySkillMeta = Lazy.of(() -> {
                final var attackerProvider = event.getAttack().getAttacker();
                final var attacker = attackerProvider != null ? attackerProvider.getEntity() : null;
                final var temp = new TriggerMetadata(playerData, TriggerType.DAMAGED, EquipmentSlot.MAIN_HAND, playerData.getPlayer().getLocation(), attacker, null, event.getAttack(), null).toSkillMetadata(SimpleSkill.EMPTY);
                // TODO add field to skillMetadata
                temp.getVariableList().registerVariable(new EventVariable("source_event", event));
                return temp;
            });

            for (var type : MitigationModule.this.types.values()) {

                // Pre-damage script
                if (type.preDamage() != null && !type.preDamage().cast(lazySkillMeta.get())) continue;

                // Check cooldown
                if (type.hasCooldown() && playerData.getCooldownMap().isOnCooldown(type)) continue;

                // Roll chance
                if (type.getRoll() != null && Math.random() > type.getRoll().evaluate(lazySkillMeta)) continue;

                // Call Bukkit Event, check for cancellation
                // [Backwards compatibility] Use previously defined events
                final DamageMitigationEvent bukkitEvent;
                if (type.asLegacy() != null)
                    bukkitEvent = type.asLegacy().generateLegacyEvent(playerData, event.toBukkit(), type);
                else bukkitEvent = new DamageMitigationEvent(playerData, type, event.toBukkit());
                Bukkit.getPluginManager().callEvent(bukkitEvent);
                if (bukkitEvent.isCancelled()) continue;

                // Apply cooldown
                if (type.hasCooldown())
                    playerData.getCooldownMap().applyCooldown(type, type.getCooldown().evaluate(lazySkillMeta));

                // Run script
                type.onDamage().cast(lazySkillMeta.get());
            }
        }
    }
}
