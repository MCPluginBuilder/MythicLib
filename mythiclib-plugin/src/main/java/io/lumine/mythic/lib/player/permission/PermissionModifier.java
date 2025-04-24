package io.lumine.mythic.lib.player.permission;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.modifier.PlayerModifier;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PermissionModifier extends PlayerModifier {
    private final String permission;

    public PermissionModifier(@NotNull String key, @NotNull String permission, @NotNull EquipmentSlot slot, @NotNull ModifierSource source) {
        super(key, slot, source);

        this.permission = permission;
    }

    public PermissionModifier(@NotNull UUID uniqueId, @NotNull String key, @NotNull String permission, @NotNull EquipmentSlot slot, @NotNull ModifierSource source) {
        super(uniqueId, key, slot, source);

        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }

    @Override
    public void register(@NotNull MMOPlayerData playerData) {
        playerData.getPermissionMap().addModifier(this);
    }

    @Override
    public void unregister(@NotNull MMOPlayerData playerData) {
        playerData.getPermissionMap().removeModifier(getUniqueId());
    }

    @NotNull
    public static PermissionModifier fromConfig(@NotNull ConfigObject configObject) {
        throw new RuntimeException("Not implemented");
    }
}
