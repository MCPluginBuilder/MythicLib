package io.lumine.mythic.lib.player.permission;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.player.modifier.ModifierMap;
import org.bukkit.permissions.PermissionAttachment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PermissionMap extends ModifierMap<PermissionModifier> {

    @Nullable(value = "null if not used")
    private PermissionAttachment attachment;

    public PermissionMap(MMOPlayerData playerData) {
        super(playerData);
    }

    /**
     * If plugins are not using permissions, simply don't
     * instantiate a useless permission attachment
     */
    @NotNull
    private PermissionAttachment attachment() {
        if (attachment != null) return attachment;
        return attachment = getPlayerData().getPlayer().addAttachment(MythicLib.plugin);
    }

    public void flushAttachment() {
        attachment = null;
    }

    @Override
    public @Nullable PermissionModifier addModifier(PermissionModifier modifier) {
        PermissionModifier removed = super.addModifier(modifier);
        if (removed != null) take(removed.getPermission()); // Take previous permission
        give(modifier.getPermission()); // Give permission
        return removed;
    }

    @Override
    public @Nullable PermissionModifier removeModifier(UUID uuid) {
        PermissionModifier removed = super.removeModifier(uuid);
        if (removed != null) take(removed.getPermission());
        return removed;
    }

    private void give(@NotNull String permission) {
        attachment().setPermission(permission, true);
    }

    private void take(@NotNull String permission) {
        attachment().unsetPermission(permission);
    }
}
