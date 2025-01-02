package io.lumine.mythic.lib;

import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.api.condition.RegionCondition;
import io.lumine.mythic.lib.api.condition.type.MMOCondition;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.comp.interaction.InteractionType;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.util.Tasks;
import io.lumine.mythic.lib.util.annotation.BackwardsCompatibility;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import io.lumine.mythic.lib.version.Attributes;
import io.lumine.mythic.lib.version.VInventoryView;
import io.lumine.mythic.lib.version.VParticle;
import io.lumine.mythic.lib.version.VersionUtils;
import io.lumine.mythic.lib.version.wrapper.VersionWrapper;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class UtilityMethods {

    public static Location readLocation(@NotNull ConfigObject config) {
        return new Location(Bukkit.getWorld(config.getString("world")), config.getDouble("x"), config.getDouble("y"), config.getDouble("z"), (float) config.getDouble("yaw"), (float) config.getDouble("pitch"));
    }

    public static Vector safeNormalize(@NotNull Vector vec) {
        return safeNormalize(vec, vec);
    }

    public static Vector safeNormalize(@NotNull Vector vec, @NotNull Vector defaultValue) {
        final double normSquared = vec.lengthSquared();
        if (normSquared == 0) return defaultValue;
        return vec.multiply(1d / Math.sqrt(normSquared));
    }

    public static void forcePotionEffect(LivingEntity entity, PotionEffectType type, double duration, int amplifier) {
        entity.removePotionEffect(type);
        entity.addPotionEffect(new PotionEffect(type, (int) (duration * 20), amplifier));
    }

    @NotNull
    public static Pattern internalPlaceholderPattern(char start, char end) {
        return Pattern.compile(start + "[^&|!=" + start + end + "]*?" + end);
    }

    private static final Listener PRIVATE_LISTENER = new Listener() {
    };

    public static <T extends Event> void registerEvent(@NotNull Class<T> eventClass,
                                                       @NotNull EventPriority priority,
                                                       @NotNull Consumer<T> executor) {
        registerEvent(eventClass, PRIVATE_LISTENER, priority, executor, MythicLib.plugin, false);
    }

    /**
     * This is sometimes used when Bukkit has a hard time finding
     * the handler list of a class extending Event.
     */
    public static <T extends Event> void registerEvent(@NotNull Class<T> eventClass,
                                                       @NotNull Listener listener,
                                                       @NotNull EventPriority priority,
                                                       @NotNull Consumer<T> executor,
                                                       @NotNull Plugin plugin,
                                                       boolean ignoreCancelled) {
        Bukkit.getPluginManager().registerEvent(eventClass, listener, priority, (listener2, event) -> executor.accept((T) event), plugin, ignoreCancelled);
    }

    /**
     * NOT FINAL CODE.
     * THIS WILL BE MASSIVELY REWORKED VERY SOON!
     */
    public static MMOCondition getCondition(String input) {
        MMOLineConfig config = new MMOLineConfig(input);
        String key = config.getKey().toLowerCase();
        switch (key) {
            case "region":
                if (!Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) return null;
                return new RegionCondition(config);
        }

        return null;
    }

    public static boolean isInvalidated(@NotNull PlayerMetadata caster) {
        return isInvalidated(caster.getData());
    }

    public static boolean isInvalidated(@NotNull MMOPlayerData playerData) {
        return !playerData.isOnline() || isInvalidated(playerData.getPlayer());
    }

    public static boolean isInvalidated(@NotNull Player player) {
        return !player.isOnline() || player.isDead();
    }

    @Nullable
    public static ItemStack getHandItem(@NotNull LivingEntity entity, @NotNull EquipmentSlot hand) {
        switch (hand) {
            case MAIN_HAND:
                return entity.getEquipment().getItemInMainHand();
            case OFF_HAND:
                return entity.getEquipment().getItemInOffHand();
            default:
                throw new IllegalArgumentException("Must provide a hand slot");
        }
    }

    @Nullable
    public static ItemStack getHandItem(@NotNull LivingEntity entity, @NotNull org.bukkit.inventory.EquipmentSlot hand) {
        switch (hand) {
            case HAND:
                return entity.getEquipment().getItemInMainHand();
            case OFF_HAND:
                return entity.getEquipment().getItemInOffHand();
            default:
                throw new IllegalArgumentException("Must provide a hand slot");
        }
    }

    @Deprecated
    public static void setTextureValue(@NotNull ItemMeta meta, @NotNull String textureValue) {
        if (meta instanceof SkullMeta) setTextureValue((SkullMeta) meta, textureValue, UUID.randomUUID());
    }

    public static void setTextureValue(@NotNull SkullMeta meta, @NotNull String textureValue) {
        setTextureValue(meta, textureValue, UUID.randomUUID());
    }

    public static void setTextureValue(@NotNull SkullMeta meta, @NotNull String textureValue, @NotNull UUID uniqueId) {
        final Object profile = VersionWrapper.get().newProfile(uniqueId, textureValue);
        VersionWrapper.get().setProfile(meta, profile);
    }

    @Deprecated
    public static boolean isFakeEvent(@NotNull EntityDamageEvent event) {
        return isFake(event);
    }

    public static boolean isFake(@NotNull Event event) {
        return MythicLib.plugin.getFakeEvents().isFake(event);
    }

    @NotNull
    @Deprecated
    public static Runnable serverThreadCatch(@NotNull Plugin plugin, @NotNull Runnable runnable) {
        return () -> {
            try {
                runnable.run();
            } catch (Throwable throwable) {
                Bukkit.getScheduler().runTask(plugin, () -> throwable.printStackTrace());
            }
        };
    }

    public static boolean isAir(@Nullable ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }

    private final static char[] DELAY_CHARACTERS = {'m', 'h', 'd', 'm', 'y'};
    private final static long[] DELAY_AMOUNTS = {60, 60 * 60, 60 * 60 * 24, 60 * 60 * 24 * 30, 60 * 60 * 24 * 30 * 365};

    public static String formatDelay(long millis) {

        if (millis < 1000 * 60) return "1m";

        String format = "";
        for (int j = DELAY_CHARACTERS.length - 1; j >= 0; j--) {
            long divisor = DELAY_AMOUNTS[j] * 1000;
            if (millis < divisor) continue;

            format = (millis / divisor) + DELAY_CHARACTERS[j] + " " + format;
            millis = millis % divisor;
        }

        return format;
    }

    private static final int PTS_PER_BLOCK = 10;

    public static void drawVector(Vector vec, Location source, Color color) {

        final double step = 1d / ((double) PTS_PER_BLOCK) / vec.length();
        for (double d = 0; d < 1; d += step) {
            Location inter = source.clone().add(vec.clone().multiply(d));
            inter.getWorld().spawnParticle(VParticle.REDSTONE.get(), inter, 0, new Particle.DustOptions(color, .6f));
        }
    }

    /**
     * @param loc Where we are looking for nearby entities
     * @return List of all entities surrounding a location. This method loops
     *         through the 9 surrounding chunks and collect all entities from
     *         them. This list can be cached and used multiple times in the same
     *         tick for projectile based spells which need to run entity
     *         checkups
     */
    public static List<Entity> getNearbyChunkEntities(Location loc) {
        final List<Entity> entities = new ArrayList<>();

        int cx = loc.getChunk().getX();
        int cz = loc.getChunk().getZ();

        for (int x = -1; x < 2; x++)
            for (int z = -1; z < 2; z++)
                entities.addAll(Arrays.asList(loc.getWorld().getChunkAt(cx + x, cz + z).getEntities()));

        return entities;
    }

    /**
     * Interaction type is set to OFFENSE_SKILL by default. No bounding box checks
     *
     * @param source Player targeting the entity
     * @param target The entity being hit
     * @return If the entity can be damaged, by a specific player, at a specific spot
     */
    public static boolean canTarget(Player source, Entity target) {
        return canTarget(source, null, target, InteractionType.OFFENSE_SKILL);
    }

    /**
     * Interaction type is set to OFFENSE_SKILL by default.
     *
     * @param source Player targeting the entity
     * @param loc    If the given location is not null, this method checks if this
     *               location is inside the bounding box of the entity hit
     * @param target The entity being hit
     * @return If the entity can be damaged, by a specific player, at a specific spot
     */
    public static boolean canTarget(Player source, Location loc, Entity target) {
        return canTarget(source, loc, target, InteractionType.OFFENSE_SKILL);
    }

    /**
     * No bounding box checks
     *
     * @param source      Player targeting the entity
     * @param target      The entity being hit
     * @param interaction Type of interaction
     * @return If the entity can be damaged, by a specific player, at a specific spot
     */
    public static boolean canTarget(Player source, Entity target, InteractionType interaction) {
        return canTarget(source, null, target, interaction);
    }

    private static final double BOUNDING_BOX_EXPANSION = .2;

    /**
     * @param source      Player targeting the entity
     * @param loc         If the given location is not null, this method checks if this
     *                    location is inside the bounding box of the entity hit
     * @param target      The entity being hit
     * @param interaction Type of interaction
     * @return If the entity can be damaged, by a specific player, at a specific spot
     */
    public static boolean canTarget(@Nullable Player source, @Nullable Location loc, @NotNull Entity target, @NotNull InteractionType interaction) {

        // Check for bounding box
        // Small computations first
        if (loc != null && !target.getBoundingBox().expand(BOUNDING_BOX_EXPANSION).contains(loc.toVector()))
            return false;

        // Interaction type check
        if (!MythicLib.plugin.getEntities().canInteract(source, target, interaction)) return false;

        return true;
    }

    @NotNull
    public static <T> T resolveField(@NotNull Function<String, T> resolver, @NotNull String... candidates) {
        return resolveField(resolver, null, candidates);
    }

    @NotNull
    public static <T> T resolveField(@NotNull Function<String, T> resolver, @Nullable Supplier<T> defaultValue, @NotNull String... candidates) {

        // Try all candidates
        for (String candidate : candidates)
            try {
                return Objects.requireNonNull(resolver.apply(candidate), "Null supplied value");
            } catch (Throwable throwable) {
                // Ignore & try next candidate
            }

        // Default value if any
        if (defaultValue != null) return Objects.requireNonNull(defaultValue.get(), "Null supplied default value");
     
        // Error otherwise
        throw new RuntimeException("Could not find enum field given candidates " + Arrays.asList(candidates));
    }

    public static double getPlayerDefaultBaseValue(@NotNull Attribute attribute, @Nullable AttributeInstance instance) {
        final Double found = MythicLib.plugin.getStats().getPlayerDefaultBaseValue(attribute);
        return found != null ? found : (instance != null ? instance.getDefaultValue() : 0);
    }

    /**
     * Equivalent of String#formatted(String... args)
     * which is not implemented yet in Java 8
     */
    @NotNull
    public static String format(@NotNull String input, @NotNull Object... args) {
        return new Formatter().format(input, args).toString();
    }

    private static final String[] PREVIOUS_ATTRIBUTE_MODIFIER_NAMES = {"mmolib.", "mmoitems.", "mythiclib."};

    /**
     * Method called on login to flush old modifiers which
     * paths used back in ML ~1.3 `mmolib.` and `mmoitems.` where
     * back when stats were not centralizing in MythicLib.
     * `mythiclib.` is for 1.21 as attribute names now start with `mythiclib:`
     */
    @BackwardsCompatibility(version = "1.3")
    public static void flushOldModifiers(@NotNull Player player) {
        for (Attribute attribute : Attributes.getAll()) {
            final AttributeInstance ins = player.getAttribute(attribute);
            if (ins == null) continue;
            for (AttributeModifier mod : ins.getModifiers())
                for (String prev : PREVIOUS_ATTRIBUTE_MODIFIER_NAMES)
                    if (mod.getName().startsWith(prev)) ins.removeModifier(mod);
        }
    }

    public static String substringBetween(final String str, final String open, final String close) {
        Validate.notNull(str);
        Validate.notNull(open);
        Validate.notNull(close);

        final int start = str.indexOf(open);
        if (start != -1) {
            final int end = str.indexOf(close, start + open.length());
            if (end != -1) {
                return str.substring(start + open.length(), end);
            }
        }
        return null;
    }

    /**
     * This method can be used to check if the player is a NPC. This
     * method checks for the "NPC" tag in the entity metadata which is
     * implemented by Citizens and Sentinels.
     * <p>
     * Other NPC plugins are not guaranteed to follow this convention
     * however, so it if always safer to simply check if the MMOPlayerData
     * of the player has been loaded or not, through {@link MMOPlayerData#getOrNull(UUID)}
     *
     * @param entity Some (fake?) player
     * @return If the player is fake
     */
    public static boolean isRealPlayer(Entity entity) {
        return entity instanceof Player && !entity.hasMetadata("NPC");
    }

    public static boolean isMetaItem(@Nullable ItemStack item) {
        return item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName();
    }

    /**
     * @param player Player to heal
     * @param heal   Heal amount
     *               <br>
     *               Negative values are just ignored
     */
    public static void heal(@NotNull LivingEntity player, double heal) {
        heal(player, heal, false);
    }

    /**
     * @param player         Player to heal
     * @param heal           Heal amount
     * @param allowNegatives If passing a negative health value will damage the entity x)
     *                       <br>
     *                       If <code>false</code>, negative values are just ignored
     */
    public static void heal(@NotNull LivingEntity player, double heal, boolean allowNegatives) {
        if (heal > 0 || allowNegatives)
            player.setHealth(Math.min(player.getAttribute(Attributes.MAX_HEALTH).getValue(), player.getHealth() + heal));
    }

    public static void closeOpenViewsOfType(Class<?> inventoryHolderClass) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            final VInventoryView view = VersionUtils.getOpen(online);
            if (inventoryHolderClass.isInstance(view.getTopInventory().getHolder())) view.close();
        }
    }

    private static final Random RANDOM = new Random();

    /**
     * Super useful to display enum names like DIAMOND_SWORD in chat
     *
     * @param input String with lower cases and spaces only
     * @return Same string with capital letters at the beginning of each word.
     */
    public static String caseOnWords(String input) {
        StringBuilder builder = new StringBuilder(input);
        boolean isLastSpace = true;
        for (int i = 0; i < builder.length(); i++) {
            char ch = builder.charAt(i);
            if (isLastSpace && ch >= 'a' && ch <= 'z') {
                builder.setCharAt(i, (char) (ch + ('A' - 'a')));
                isLastSpace = false;
            } else isLastSpace = ch == ' ';
        }
        return builder.toString();
    }

    public static void dropItemNaturally(@NotNull Location loc, @NotNull ItemStack stack) {
        double xs = (RANDOM.nextFloat() * 0.5F) + 0.25D;
        double ys = (RANDOM.nextFloat() * 0.5F) + 0.25D;
        double zs = (RANDOM.nextFloat() * 0.5F) + 0.25D;
        loc.getWorld().dropItem(loc.clone().add(xs, ys, zs), stack);
    }

    /**
     * Used to find players in chunks around some location. This is
     * used when displaying individual holograms to a list of players.
     *
     * @param loc Target location
     * @return Players in chunks around the location
     */
    @Deprecated
    public static List<Player> getNearbyPlayers(Location loc) {
        final List<Player> players = new ArrayList<>();

        final int cx = loc.getChunk().getX(), cz = loc.getChunk().getZ();

        for (int x = -1; x < 2; x++)
            for (int z = -1; z < 2; z++)
                for (Entity target : loc.getWorld().getChunkAt(cx + x, cz + z).getEntities())
                    if (target instanceof Player) players.add((Player) target);

        return players;
    }

    public static void loadDefaultFile(String path, String name) {
        final String newPath = path.isEmpty() ? "" : "/" + path;
        final File folder = new File(MythicLib.plugin.getDataFolder() + newPath);
        if (!folder.exists()) folder.mkdir();

        final File file = new File(MythicLib.plugin.getDataFolder() + newPath, name);
        if (!file.exists()) try {
            Files.copy(MythicLib.plugin.getResource("default/" + (path.isEmpty() ? "" : path + "/") + name), file.getAbsoluteFile().toPath());
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * The 'vanished' meta data should be supported by vanish plugins
     * to let all the plugins knows when a player is vanished.
     *
     * @return If a given player is vanished or not
     */
    public static boolean isVanished(Player player) {
        for (MetadataValue meta : player.getMetadata("vanished"))
            if (meta.asBoolean()) return true;
        return false;
    }

    /**
     * @return Upper case string, with spaces and - replaced by _
     */
    public static String enumName(String str) {
        return str.toUpperCase().replace("-", "_").replace(" ", "_");
    }

    @Deprecated
    public static <T> Consumer<T> sync(@NotNull Plugin plugin, @NotNull Consumer<T> syncTask) {
        return Tasks.sync(plugin, syncTask);
    }

    public static String ymlName(String str) {
        return str.toLowerCase().replace("_", "-").replace(" ", "-");
    }

    /**
     * Useful when dealing with Pvp stuff. If a VANILLA attack is due to
     * a player, this method will return the damage source ie the player.
     *
     * @param event Some damage event
     * @return The player, if this event is due to him. It is the player which
     *         is taken into account when PvP is toggled on.
     */
    @Nullable
    public static Player getPlayerDamager(EntityDamageByEntityEvent event) {
        if (isRealPlayer(event.getDamager())) return (Player) event.getDamager();

        if (event.getDamager() instanceof Projectile) {
            final ProjectileSource shooter = ((Projectile) event.getDamager()).getShooter();
            if (shooter instanceof Entity && isRealPlayer((Entity) shooter)) return (Player) shooter;
        }

        return null;
    }

    @BackwardsCompatibility(version = "1.20.5")
    @NotNull
    public static Enchantment findEnchant(@NotNull String id) {

        // By key
        id = id.toLowerCase().replace("-", "_");
        Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(id));
        if (enchant != null) return enchant;

        // By name
        id = id.toUpperCase();
        enchant = Enchantment.getByName(id);
        if (enchant != null) return enchant;

        throw new IllegalArgumentException("Could not find enchant called '" + id + "'");
    }

    public static double[] getYawPitch(Vector axis) {
        final double _2PI = 6.283185307179586D;
        final double x = axis.getX();
        final double z = axis.getZ();

        if (x == 0 && z == 0) return new double[]{0, axis.getY() > 0 ? -90 : 90};
        else {
            final double theta = Math.atan2(-x, z);
            final double yaw = (float) Math.toDegrees((theta + _2PI) % _2PI);
            final double xz = Math.sqrt(x * x + z * z);
            final double pitch = (float) Math.toDegrees(Math.atan(-axis.getY() / xz));
            return new double[]{yaw, pitch};
        }
    }

    public static Vector rotate(Vector rotated, Vector axis) {
        double[] yawPitch = getYawPitch(axis);
        return rotate(rotated, Math.toRadians(yawPitch[0]), Math.toRadians(yawPitch[1]));
    }

    public static Vector rotate(Vector rotated, double yaw, double pitch) {
        return rotAxisY(rotAxisX(rotated, pitch), -yaw);
    }

    private static Vector rotAxisX(Vector rotated, double angle) {
        double y = rotated.getY() * Math.cos(angle) - rotated.getZ() * Math.sin(angle);
        double z = rotated.getY() * Math.sin(angle) + rotated.getZ() * Math.cos(angle);
        return rotated.setY(y).setZ(z);
    }

    private static Vector rotAxisY(Vector rotated, double angle) {
        double x = rotated.getX() * Math.cos(angle) + rotated.getZ() * Math.sin(angle);
        double z = rotated.getX() * -Math.sin(angle) + rotated.getZ() * Math.cos(angle);
        return rotated.setX(x).setZ(z);
    }

    public static double getAltitude(Entity entity) {
        return getAltitude(entity.getLocation());
    }

    public static double getAltitude(Location loc) {
        final Location moving = loc.clone();
        while (!moving.getBlock().getType().isSolid()) moving.add(0, -1, 0);

        return loc.getY() - moving.getBlockY() - 1;
    }

    private static final Map<String, String> DEBUG_COLOR_PREFIX = new HashMap<>();

    static {
        DEBUG_COLOR_PREFIX.put("MythicLib", "§a");
        DEBUG_COLOR_PREFIX.put("MMOItems", "§c");
        DEBUG_COLOR_PREFIX.put("MMOCore", "§6");
        DEBUG_COLOR_PREFIX.put("RPGInventory", "§e");
    }

    /**
     * Sends a debug message. All plugins depending on MythicLib must use this
     * function to send debug message, which is more convenient for users.
     * MMOInventory has its own option, because it's standalone.
     *
     * @param plugin  Plugin that needs debug
     * @param prefix  What's being debugged
     * @param message Debug message
     */
    public static void debug(@NotNull JavaPlugin plugin, @Nullable String prefix, @NotNull String message) {
        Validate.notNull(plugin, "Plugin cannot be null");
        Validate.notNull(message, "Message cannot be null");

        final String colorPrefix = DEBUG_COLOR_PREFIX.getOrDefault(plugin.getName(), "");

        if (MythicLib.plugin.getMMOConfig().debugMode)
            plugin.getLogger().log(Level.INFO, colorPrefix + "[Debug" + (prefix == null ? "" : ": " + prefix) + "] " + message);
    }

    private static final int[] NEGATIVE_SPACE_AMOUNTS = {1, 2, 3, 4, 5, 6, 7, 8, 16, 32, 64, 128, 256, 512, 1024};

    /**
     * Uses character convention from https://www.spigotmc.org/threads/negative-space-font-resource-pack.440952/
     * Differs from the one given by https://github.com/AmberWat/NegativeSpaceFont
     *
     * @param size Target size in pixels of negative space
     * @return String containing negative font with given size
     */
    @NotNull
    public static String getFontSpace(int size) {
        Validate.isTrue(size < 2048 && size > -2048, "Size must be between -2050 and 2050");
        if (size == 0) return "";

        // Determine base char
        final int BASE_CHAR = size < 0 ? 0xf801 : 0xf821;
        if (size < 0) size = -size;

        final StringBuilder built = new StringBuilder();
        for (int i = 0; i < NEGATIVE_SPACE_AMOUNTS.length; i++) {
            final int index = NEGATIVE_SPACE_AMOUNTS.length - 1 - i;
            final int providedSize = NEGATIVE_SPACE_AMOUNTS[index];
            if (size >= providedSize) {
                size -= providedSize;
                built.append((char) (BASE_CHAR + index));
            }
        }

        return built.toString();
    }
}
