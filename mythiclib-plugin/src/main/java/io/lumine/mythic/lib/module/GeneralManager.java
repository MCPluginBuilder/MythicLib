package io.lumine.mythic.lib.module;

import io.lumine.mythic.lib.util.annotation.NotUsed;

/**
 * The main problem is loading managers on startup with the right order.
 * Managers have different behaviours on plugin startup and reload.
 * <p>
 * On startup, they usually preload object references and temporarily
 * save configuration files before loading objects further.
 *
 * @see io.lumine.mythic.lib.util.PreloadedObject
 */
@NotUsed
public abstract class GeneralManager extends Module {
    public GeneralManager(MMOPluginImpl plugin) {
        super(plugin);
    }
}
