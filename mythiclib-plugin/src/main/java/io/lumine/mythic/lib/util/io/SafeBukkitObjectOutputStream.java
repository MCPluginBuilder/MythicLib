package io.lumine.mythic.lib.util.io;

import io.lumine.mythic.lib.util.annotation.BackwardsCompatibility;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.IOException;
import java.io.OutputStream;

@BackwardsCompatibility(version = "1.20.4")
public class SafeBukkitObjectOutputStream extends BukkitObjectOutputStream {
    public SafeBukkitObjectOutputStream(OutputStream out) throws IOException, SecurityException {
        super(out);
    }

    public static boolean USE_NBT_API = false;

    public void safeWriteItemStack(ItemStack item) throws IOException {
        if (USE_NBT_API) writeObject(SafeItemSerializer.nbtSerialize(item));
        else writeObject(item);
    }
}
