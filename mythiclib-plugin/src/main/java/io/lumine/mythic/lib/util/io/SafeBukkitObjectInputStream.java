package io.lumine.mythic.lib.util.io;

import io.lumine.mythic.lib.util.annotation.BackwardsCompatibility;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;

import java.io.IOException;
import java.io.InputStream;

@SuppressWarnings("deprecation")
@BackwardsCompatibility(version = "1.20.4")
public class SafeBukkitObjectInputStream extends BukkitObjectInputStream {
    public SafeBukkitObjectInputStream(InputStream in) throws IOException, SecurityException {
        super(in);
    }

    public ItemStack safeReadItemStack() throws IOException, ClassNotFoundException {
        Object read = readObject();

        // Direct itemstack
        if (read instanceof ItemStack) return (ItemStack) read;

            // Safe read from NBT. 1.20.4 backwards compatibility
        else if (read instanceof String) return SafeItemSerializer.nbtDeserialize((String) read);

            // Type not recognized
        else throw new IllegalStateException("Cannot read item stack from " + read.getClass());
    }
}
