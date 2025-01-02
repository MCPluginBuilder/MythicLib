package io.lumine.mythic.lib.listener.option;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.phoenixdevt.profiles.event.PlayerIdDispatchEvent;
import fr.phoenixdevt.profiles.event.ProfileSelectedEvent;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.util.lang3.Validate;
import io.lumine.mythic.lib.version.Attributes;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FixAttributeModifiers implements Listener {
    private final List<Attribute> attributes = new ArrayList<>();
    private final int targetRevId;

    public FixAttributeModifiers(ConfigurationSection config) {
        targetRevId = config.getInt("rev_id");
        Validate.isTrue(targetRevId >= 0, "Revision ID must be positive");

        // List of attributes to reset
        final List<String> _attributes = config.getStringList("attributes");
        if (_attributes.isEmpty()) attributes.addAll(Attributes.getAll());
        else for (String str : _attributes)
            attributes.add(Attributes.fromName(UtilityMethods.enumName(str)));

        try {
            // Will fail if Profile-API is not implemented
            Class.forName("fr.phoenixdevt.profiles.event.ProfileSelectedEvent");
            Bukkit.getPluginManager().registerEvents(new LegacyProfiles(), MythicLib.plugin);
        } catch (Exception exception) {
            Bukkit.getPluginManager().registerEvents(this, MythicLib.plugin);
        }
    }

    private static final String PROFILELESS_KEY = "Profileless";

    @EventHandler(priority = EventPriority.LOWEST)
    public void a(PlayerJoinEvent event) {
        checkForRevid(event.getPlayer(), PROFILELESS_KEY);
    }

    private static final NamespacedKey TAG_PATH = NamespacedKey.fromString("attr_mods_reset", MythicLib.plugin);

    private void checkForRevid(@NotNull Player player, @NotNull String key) {

        if (!player.hasPlayedBefore()) {
            JsonObject json = new JsonObject();
            json.addProperty(key, targetRevId);
            player.getPersistentDataContainer().set(TAG_PATH, PersistentDataType.STRING, json.toString());
            return;
        }

        final String jsonFormat = player.getPersistentDataContainer().get(TAG_PATH, PersistentDataType.STRING);
        final JsonObject json = jsonFormat == null ? new JsonObject() : JsonParser.parseString(jsonFormat).getAsJsonObject();

        final int revid = json.has(key) ? json.get(key).getAsInt() : 0;
        if (revid < targetRevId) {
            json.addProperty(key, targetRevId);
            player.getPersistentDataContainer().set(TAG_PATH, PersistentDataType.STRING, json.toString());

            resetAttributeModifiers(player);
        }
    }

    private void resetAttributeModifiers(@NotNull Player player) {
        for (Attribute attribute : attributes) {
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance != null) for (AttributeModifier modifier : new ArrayList<>(instance.getModifiers()))
                instance.removeModifier(modifier);
        }
    }

    class LegacyProfiles implements Listener {

        @EventHandler(priority = EventPriority.MONITOR)
        public void onProfileSelect(ProfileSelectedEvent event) {
            checkForRevid(event.getPlayer(), event.getProfile().getUniqueId().toString());
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onIdDispatch(PlayerIdDispatchEvent event) {
            checkForRevid(event.getPlayer(), event.getFakeId() == null ? PROFILELESS_KEY : event.getFakeId().toString());
        }
    }
}
