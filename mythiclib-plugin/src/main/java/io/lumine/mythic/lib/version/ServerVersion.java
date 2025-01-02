package io.lumine.mythic.lib.version;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.util.annotation.BackwardsCompatibility;
import io.lumine.mythic.lib.util.lang3.Validate;
import io.lumine.mythic.lib.version.wrapper.VersionWrapper;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.logging.Level;

public class ServerVersion {
    private final String craftBukkitVersion;
    private final int revNumber;
    private final int[] bukkitVersion;
    private final VersionWrapper versionWrapper;
    private final boolean paper;

    private static final int MAXIMUM_INDEX = 3;

    @Deprecated
    public ServerVersion(Class<?> ignored) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        this();
    }

    public ServerVersion() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        // Version numbers
        final String[] bukkitSplit = Bukkit.getServer().getBukkitVersion().split("\\-")[0].split("\\."); // ["1", "20", "4"]
        bukkitVersion = new int[Math.min(MAXIMUM_INDEX, bukkitSplit.length)];
        for (int i = 0; i < bukkitVersion.length; i++)
            bukkitVersion[i] = Integer.parseInt(bukkitSplit[i]);

        // Compute rev number
        revNumber = findRevisionNumber();
        craftBukkitVersion = craftBukkitVersion(revNumber); // "v1_20_R4"

        VersionWrapper found;
        try {
            found = (VersionWrapper) Class.forName("io.lumine.mythic.lib.version.wrapper.VersionWrapper_" + craftBukkitVersion.substring(1)).getDeclaredConstructor().newInstance();
        } catch (Exception exception) {
            MythicLib.plugin.getLogger().log(Level.WARNING, "Non-natively supported Spigot version detected, trying reflection-based compatibility mode");
            found = (VersionWrapper) Class.forName("io.lumine.mythic.lib.version.wrapper.VersionWrapper_Reflection").getDeclaredConstructor(ServerVersion.class).newInstance(this);
        }
        this.versionWrapper = found;

        // Running Paper?
        boolean isPaper = false;
        try {
            // Any other works, just the shortest I could find.
            Class.forName("com.destroystokyo.paper.ParticleBuilder");
            isPaper = true;
        } catch (ClassNotFoundException ignored) {
            // Ignored
        }
        this.paper = isPaper;

        // Validate all mappings
        try {
            //Attributes.getAll(); static code wont run
            VEnchantment.values();
            VEntityType.values();
            VMaterial.values();
            VParticle.values();
            VPotionEffectType.values();
            Validate.notNull(Sounds.ENTITY_ENDERMAN_HURT, "Error with sounds");
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw new RuntimeException("Compatibility error: " + throwable.getMessage());
        }
    }

    @NotNull
    private String craftBukkitVersion(int revNumber) {
        return "v" + bukkitVersion[0] + "_" + bukkitVersion[1] + "_R" + revNumber;
    }

    private static final int MAXIMUM_REVISION_NUMBER = 10;
    private static final String CLASS_NAME_USED = "CraftServer";

    @BackwardsCompatibility(version = "1.20.5")
    private int findRevisionNumber() {

        // Spigot || Paper <1.20.5
        try {
            final Class<?> bukkitServerClass = Bukkit.getServer().getClass();
            final String rev = bukkitServerClass.getPackage().getName().replace(".", ",").split(",")[3]; // "1_20_R4"
            return Integer.parseInt(rev.split("_")[2].replaceAll("[^0-9]", ""));
        } catch (Throwable throwable) {
            // Ignored
        }

        // Spigot 1.20.5+
        for (int revNumber = 1; revNumber < MAXIMUM_REVISION_NUMBER; revNumber++)
            try {
                final String candidate = craftBukkitVersion(revNumber);
                Class.forName("org.bukkit.craftbukkit." + candidate + "." + CLASS_NAME_USED);
                return revNumber;
            } catch (Throwable throwable) {
                // Ignored
            }

        // Assume no need for the revision number (Paper 1.20.5+)
        return 0;
    }

    public boolean isPaper() {
        return paper;
    }

    /**
     * This is the most useful function when dealing with compatibility. Since
     * plugin features are, most of the time, only registered when the server
     * version is found to be above a certain threshold.
     *
     * @param version Provided Minecraft version
     * @return True if server version is either equal to or above provided version.
     */
    public boolean isAbove(int... version) {
        Validate.isTrue(version.length >= 1 && version.length <= MAXIMUM_INDEX, "Provide at least 1 integer and at most " + MAXIMUM_INDEX);

        final int maxLength = Math.min(MAXIMUM_INDEX, Math.max(version.length, bukkitVersion.length));
        for (int i = 0; i < maxLength; i++) {
            final int server = i >= bukkitVersion.length ? 0 : bukkitVersion[i];
            final int provided = i >= version.length ? 0 : version[i];
            if (server != provided) return server > provided;
        }

        return true;
    }

    public boolean isUnder(int... version) {
        return !isAbove(version);
    }

    @NotNull
    public String getCraftBukkitVersion() {
        return craftBukkitVersion;
    }

    public int getRevisionNumber() {
        return revNumber;
    }

    public int[] getBukkitVersion() {
        return bukkitVersion;
    }

    @NotNull
    public VersionWrapper getWrapper() {
        return versionWrapper;
    }

    @Override
    public String toString() {
        return "ServerVersion{" +
                "revision='" + craftBukkitVersion + '\'' +
                ", revisionNumber=" + revNumber +
                ", integers=" + Arrays.toString(bukkitVersion) +
                ", paper=" + paper +
                '}';
    }

    @Deprecated
    public String getRevision() {
        return getCraftBukkitVersion();
    }

    @Deprecated
    public int[] toNumbers() {
        return bukkitVersion;
    }

    @Deprecated
    public int[] getIntegers() {
        return getBukkitVersion();
    }

    @Deprecated
    public boolean isStrictlyHigher(int... version) {
        Validate.isTrue(version.length >= 1 && version.length <= MAXIMUM_INDEX, "Provide at least 1 integer and at most " + MAXIMUM_INDEX);

        final int maxLength = Math.min(MAXIMUM_INDEX, Math.max(version.length, bukkitVersion.length));
        for (int i = 0; i < maxLength; i++) {
            final int server = i >= bukkitVersion.length ? 0 : bukkitVersion[i];
            final int provided = i >= version.length ? 0 : version[i];
            if (server != provided) return server > provided;
        }

        return false;
    }

    @Deprecated
    public boolean isBelowOrEqual(int... version) {
        return !isStrictlyHigher(version);
    }
}
