package io.lumine.mythic.lib.profile.handler;

import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;

import java.util.List;

public interface ProfileHandler extends Listener {

    public void onStartup();

    public List<NamespacedKey> collectModules();
}
