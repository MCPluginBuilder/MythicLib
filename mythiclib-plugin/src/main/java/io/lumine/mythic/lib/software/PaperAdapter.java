package io.lumine.mythic.lib.software;

import io.lumine.mythic.lib.MythicLib;
import org.bukkit.Bukkit;

public class PaperAdapter {

    public static void init(MythicLib plugin) {
        Bukkit.getPluginManager().registerEvents(new PaperListener(), plugin);
    }
}
