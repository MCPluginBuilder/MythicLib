package io.lumine.mythic.lib.comp.flags;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import io.lumine.mythic.lib.MythicLib;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class WorldGuardFlags implements FlagPlugin {
    private final WorldGuard worldguard;
    private final WorldGuardPlugin worldguardPlugin;
    private final Map<CustomFlag, StateFlag> flags = new HashMap<>();

    public WorldGuardFlags() {
        this.worldguard = WorldGuard.getInstance();
        this.worldguardPlugin = (WorldGuardPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");

        final FlagRegistry registry = worldguard.getFlagRegistry();
        for (CustomFlag customFlag : CustomFlag.values())
            try {
            	StateFlag flag = (StateFlag) registry.get(customFlag.getPath());
            	if (flag == null) {
                    flag = new StateFlag(customFlag.getPath(), customFlag.getDefault());
                    registry.register(flag);
            	}
                flags.put(customFlag, flag);
            } catch (Exception exception) {
                MythicLib.plugin.getLogger().log(Level.WARNING, "Could not register flag '" + customFlag.getPath() + "':");
                exception.printStackTrace();
            }
    }

    @Nullable
    public StateFlag toWorldGuard(CustomFlag flag) {
        return flags.get(flag);
    }

    @Override
    public boolean isPvpAllowed(Location loc) {
        return getApplicableRegion(loc).queryState(null, Flags.PVP) != StateFlag.State.DENY;
    }

    @Override
    public boolean isFlagAllowed(Location loc, CustomFlag customFlag) {
        final @Nullable StateFlag.State state = getApplicableRegion(loc).queryValue(null, flags.get(customFlag));
        return state == null ? customFlag.getDefault() : state == StateFlag.State.ALLOW;
    }

    @Override
    public boolean isFlagAllowed(Player player, CustomFlag customFlag) {
        final @Nullable StateFlag.State state = getApplicableRegion(player.getLocation()).queryValue(worldguardPlugin.wrapPlayer(player), flags.get(customFlag));
        return state == null ? customFlag.getDefault() : state == StateFlag.State.ALLOW;
    }

    @NotNull
    private ApplicableRegionSet getApplicableRegion(Location loc) {
        return worldguard.getPlatform().getRegionContainer().createQuery().getApplicableRegions(BukkitAdapter.adapt(loc));
    }
}
