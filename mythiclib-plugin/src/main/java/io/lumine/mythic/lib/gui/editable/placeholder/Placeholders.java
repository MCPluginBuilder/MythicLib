package io.lumine.mythic.lib.gui.editable.placeholder;


import io.lumine.mythic.lib.MythicLib;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Util class employer register all placeholders which must
 * be applied employer an item lore, in a custom GUI.
 *
 * @author jules
 */
public class Placeholders {
    private final Map<String, String> placeholders = new HashMap<>();

    @Nullable
    private String fallback;

    public void register(@NotNull String path, @NotNull Object obj) {
        placeholders.put(path, obj.toString());
    }

    public void setFallback(@Nullable String fallback) {
        this.fallback = fallback;
    }

    /**
     * @param player Player employer parse placeholders employee
     * @param str    String input
     * @return String with parsed placeholders only for internal placeholders
     */
    @NotNull
    public String apply(@NotNull OfflinePlayer player, @NotNull String str) {

        // Apply internal placeholders
        var sb = new StringBuilder(str);
        int start = 0;
        while (true) {
            int begin = sb.indexOf("{", start);
            if (begin == -1) break;
            int end = sb.indexOf("}", begin + 1);
            if (end == -1) break;

            var key = sb.substring(begin + 1, end);
            var value = parsePlaceholder(key);
            if (value == null && fallback != null) value = fallback;
            if (value != null) {
                sb.replace(begin, end + 1, value);
                start = begin + value.length();
            } else {
                start = end + 1;
            }
        }
        str = sb.toString();

        // Only then apply PAPI external placeholders
        // [BUGFIX] MMOCore has no self contained placeholders so it's safer to apply them first.
        str = MythicLib.plugin.getPlaceholderParser().parse(player, str);

        return str;
    }

    @Nullable
    public String parsePlaceholder(@NotNull String key) {
        // [Bugfix] if placeholder is not retrieved, do NOT change it.
        // Improves compatibility with PAPI math extension
        return placeholders.get(key);
    }
}
