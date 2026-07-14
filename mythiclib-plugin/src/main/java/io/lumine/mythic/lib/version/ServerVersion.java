package io.lumine.mythic.lib.version;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.util.lang3.Validate;
import io.lumine.mythic.lib.version.wrapper.VersionWrapper;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ServerVersion {
    private final int[] bukkitVersion;
    private final VersionWrapper versionWrapper;
    private final NMSVersion nmsVersion;
    private final boolean paper;

    private static final int MAXIMUM_VERSION_SIZE = 3;

    public ServerVersion(JavaPlugin ignore) {

        // Running Paper?
        // ===================================================================
        boolean isPaper = false;
        try {
            // Any other works, just the shortest I could find.
            // TODO use one that actually makes sense, or look for papermc package or something
            Class.forName("com.destroystokyo.paper.ParticleBuilder");
            isPaper = true;
        } catch (ClassNotFoundException ignored) {
            // Ignored
        }
        this.paper = isPaper;

        // Version numbers
        // ===================================================================
        final String[] bukkitSplit = Bukkit.getServer().getBukkitVersion().split("-")[0].split("\\."); // ["1", "20", "4"]
        bukkitVersion = new int[Math.min(MAXIMUM_VERSION_SIZE, bukkitSplit.length)];
        for (int i = 0; i < bukkitVersion.length; i++) {
            // 26.2 dev builds of Paper have a weird version syntax containing "build"
            if (bukkitSplit[i].equals("build")) break;
            bukkitVersion[i] = Integer.parseInt(bukkitSplit[i]);
        }

        // Try version mapping directly
        // ===================================================================
        try {
            this.nmsVersion = NMSVersion.resolve(this);
            this.versionWrapper = this.nmsVersion.instantiateWrapper();
        } catch (Exception exception) {
            throw new RuntimeException("Error while initializing version wrapper", exception);
        }
    }

    public void validateMappings() {
        try {
            //Attributes.getAll(); static code wont run
            VEnchantment.values();
            VEntityType.values();
            VMaterial.values();
            VParticle.values();
            VPotionEffectType.values();
            Validate.notNull(Sounds.ENTITY_ENDERMAN_HURT, "Error with sounds");
        } catch (Exception throwable) {
            throw new RuntimeException("Compatibility error", throwable);
        }
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
        Validate.isTrue(version.length >= 1 && version.length <= MAXIMUM_VERSION_SIZE, "Provide at least 1 integer and at most " + MAXIMUM_VERSION_SIZE);

        final int maxLength = Math.min(MAXIMUM_VERSION_SIZE, Math.max(version.length, bukkitVersion.length));
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

    public int[] getBukkitVersion() {
        return bukkitVersion;
    }

    @NotNull
    public VersionWrapper getWrapper() {
        return versionWrapper;
    }

    @Override
    public String toString() {
        var bukkitVersionStr = Arrays.stream(this.bukkitVersion).mapToObj(String::valueOf).collect(Collectors.joining("."));
        return bukkitVersionStr + " (" + this.nmsVersion.name() + ")";
    }

    //region Static methods

    @NotNull
    public static ServerVersion get() {
        return MythicLib.plugin.getVersion();
    }

    //endregion

    //region Deprecated

    @Deprecated
    private String craftBukkitVersion(int revNumber) {
        return "v" + bukkitVersion[0] + "_" + bukkitVersion[1] + "_R" + revNumber;
    }

    @Deprecated
    private static final int MAXIMUM_REVISION_NUMBER = 10;
    @Deprecated
    private static final String CLASS_NAME_USED = "CraftServer";

    @Deprecated
    private int findRevisionNumber() {
        //@BackwardsCompatibility(version = "1.20.5")

        // Spigot || Paper <1.20.5
        try {
            final Class<?> bukkitServerClass = Bukkit.getServer().getClass();
            final String rev = bukkitServerClass.getPackage().getName().replace(".", ",").split(",")[3]; // "1_20_R4"
            return Integer.parseInt(rev.split("_")[2].replaceAll("[^0-9]", ""));
        } catch (Exception throwable) {
            // Ignored
        }

        // Spigot 1.20.5+
        for (int revNumber = 1; revNumber < MAXIMUM_REVISION_NUMBER; revNumber++)
            try {
                final String candidate = craftBukkitVersion(revNumber);
                Class.forName("org.bukkit.craftbukkit." + candidate + "." + CLASS_NAME_USED);
                return revNumber;
            } catch (Exception throwable) {
                // Ignored
            }

        // Assume no need for the revision number (Paper 1.20.5+)
        return 0;
    }

    @Deprecated
    public int getRevisionNumber() {
        return findRevisionNumber();
    }

    @Deprecated
    public String getRevision() {
        return getCraftBukkitVersion();
    }

    @Deprecated
    public String getCraftBukkitVersion() {
        return craftBukkitVersion(getRevisionNumber());
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
        Validate.isTrue(version.length >= 1 && version.length <= MAXIMUM_VERSION_SIZE, "Provide at least 1 integer and at most " + MAXIMUM_VERSION_SIZE);

        final int maxLength = Math.min(MAXIMUM_VERSION_SIZE, Math.max(version.length, bukkitVersion.length));
        for (int i = 0; i < maxLength; i++) {
            final int server = i >= bukkitVersion.length ? 0 : bukkitVersion[i];
            final int provided = i >= version.length ? 0 : version[i];
            if (server != provided) return server > provided;
        }

        return false;
    }

    @Deprecated
    public ServerVersion(Class<?> ignored) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        this();
    }

    @Deprecated
    public ServerVersion() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        this(MythicLib.plugin);
    }

    @Deprecated
    public boolean isBelowOrEqual(int... version) {
        return !isStrictlyHigher(version);
    }

    //endregion

    /**
     * Maps Bukkit versions to their respective NMS version.
     * <p>
     * 26.1.2+ Spigot and Paper builds have issues with their
     * NMS version number, so the easiest now is to hardcode
     * all the required mappings.
     */
    private enum NMSVersion {

        v26_1_2(26, 1, 2),
        v26_1(26, 1),

        // 1.21
        // Source: https://www.spigotmc.org/wiki/spigot-nms-and-minecraft-versions-1-21/
        v1_21_R7(1, 21, 11),
        v1_21_R6(1, 21, 9),
        v1_21_R5(1, 21, 6),
        v1_21_R4(1, 21, 5),
        v1_21_R3(1, 21, 4),
        v1_21_R2(1, 21, 2),
        v1_21_R1(1, 21),

        // 1.16-1.20
        // Source: https://www.spigotmc.org/wiki/spigot-nms-and-minecraft-versions-1-16/
        v1_20_R4(1, 20, 5),
        v1_20_R3(1, 20, 3),
        v1_20_R2(1, 20, 2),
        v1_20_R1(1, 20),

        v1_19_R3(1, 19, 4),
        v1_19_R2(1, 19, 3),
        v1_19_R1(1, 19),

        v1_18_R2(1, 18, 2),
        v1_18_R1(1, 18),

        v1_17_R1(1, 17),

        v1_16_R3(1, 16, 4),
        v1_16_R2(1, 16, 2),
        v1_16_R1(1, 16),

        // 1.15-1.14
        // Source: https://www.spigotmc.org/wiki/spigot-nms-and-minecraft-versions-1-10-1-15/
        v1_15_R1(1, 15),

        v1_14_R1(1, 14),

        ;

        final String suffix;
        final int[] versionNumbers;

        /**
         * @param versionNumbers Earliest version that uses this NMS mapping
         */
        NMSVersion(int... versionNumbers) {
            this.suffix = name().substring(1);
            this.versionNumbers = versionNumbers;
        }

        @NotNull
        public static ServerVersion.NMSVersion getLatest() {
            return NMSVersion.values()[0];
        }

        public VersionWrapper instantiateWrapper() throws ReflectiveOperationException {
            var className = "io.lumine.mythic.lib.version.wrapper.VersionWrapper_" + this.suffix;
            var obj = Class.forName(className).getDeclaredConstructor().newInstance();
            return (VersionWrapper) obj;
        }

        @NotNull
        public static ServerVersion.NMSVersion resolve(ServerVersion serverVersion) {

            // Try every version
            for (var candidate : NMSVersion.values())
                if (serverVersion.isAbove(candidate.versionNumbers))
                    return candidate;

            // This should never happen, latest version should always match
            throw new IllegalArgumentException("Internal error, no matching version");
        }
    }
}
