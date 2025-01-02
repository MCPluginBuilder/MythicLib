package io.lumine.mythic.lib.version.wrapper;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTCompound;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.util.NBTTypeHelper;
import io.lumine.mythic.lib.version.OreDrops;
import io.lumine.mythic.lib.version.ServerVersion;
import io.lumine.mythic.lib.version.VInventoryView;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.AdventureModePredicate;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.Validate;
import org.bukkit.*;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.craftbukkit.v1_21_R2.CraftSound;
import org.bukkit.craftbukkit.v1_21_R2.block.CraftBlockType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class VersionWrapper_Reflection implements VersionWrapper {
    private final Set<Material> generatorOutputs = new HashSet<>();

    // Reflection stuff
    private final ServerVersion version;
    private final Method _CraftWorld_getHandle, _CraftPlayer_getHandle, _CraftPlayer_getProfile, _CraftItemStack_asNMSCopy, _CraftItemStack_asBukkitCopy;

    public VersionWrapper_Reflection(ServerVersion version) throws NoSuchMethodException, ClassNotFoundException {
        generatorOutputs.add(Material.COBBLESTONE);
        generatorOutputs.add(Material.OBSIDIAN);
        generatorOutputs.add(Material.BASALT);

        this.version = version;

        // Classes
        final Class<?> _CraftWorld = obcClass("CraftWorld");
        final Class<?> _CraftPlayer = obcClass("entity.CraftPlayer");
        final Class<?> _CraftItemStack = obcClass("inventory.CraftItemStack");

        // Methods
        _CraftWorld_getHandle = _CraftWorld.getDeclaredMethod("getHandle");
        _CraftPlayer_getHandle = _CraftPlayer.getDeclaredMethod("getHandle");
        _CraftPlayer_getProfile = _CraftPlayer.getDeclaredMethod("getProfile");
        _CraftItemStack_asNMSCopy = _CraftItemStack.getDeclaredMethod("asNMSCopy", ItemStack.class);
        _CraftItemStack_asBukkitCopy = _CraftItemStack.getDeclaredMethod("asBukkitCopy", net.minecraft.world.item.ItemStack.class);
    }

    private Class<?> obcClass(String obcClassPath) throws ClassNotFoundException {

        // Paper 1.20.5+
        if (version.isAbove(1, 20, 5) && version.isPaper())
            return Class.forName("org.bukkit.craftbukkit." + obcClassPath);

        // Spigot || Paper <1.20.5
        return Class.forName("org.bukkit.craftbukkit." + version.getCraftBukkitVersion() + "." + obcClassPath);
    }

    @Override
    public PlayerProfile getProfile(SkullMeta meta) {
        return meta.getOwnerProfile();
    }

    @Override
    public void setProfile(SkullMeta meta, Object object) {
        meta.setOwnerProfile(object == null ? null : (PlayerProfile) object);
    }

    @Override
    public PlayerProfile newProfile(UUID uniqueId, String textureValue) {
        final PlayerProfile profile = Bukkit.getServer().createPlayerProfile(uniqueId, PLAYER_PROFILE_NAME);
        final String stringUrl = textureValue.startsWith("http") ? textureValue : extractUrl(new String(Base64.getDecoder().decode(textureValue)));
        final URL url;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException exception) {
            throw new RuntimeException("Could not create new player profile: " + exception.getMessage());
        }
        profile.getTextures().setSkin(url);
        return profile;
    }

    private static final String URL_PREFIX = "\"url\":\"";
    private static final String URL_SUFFIX = "\"";

    private String extractUrl(String str) {
        int start = str.indexOf(URL_PREFIX);
        Validate.isTrue(start >= 0, "Could not find prefix in decoded skull value");
        start += URL_PREFIX.length();
        final int end = str.indexOf(URL_SUFFIX, start);
        return str.substring(start, end);
    }

    @Override
    public boolean isGeneratorOutput(Material material) {
        return generatorOutputs.contains(material);
    }

    @Override
    public boolean isHelmet(Material material) {
        return material.getEquipmentSlot() == EquipmentSlot.HEAD;
    }

    private static final OreDrops IRON_ORE = new OreDrops(Material.IRON_INGOT),
            GOLD_ORE = new OreDrops(Material.GOLD_INGOT),
            COPPER_ORE = new OreDrops(Material.COPPER_INGOT, 2, 5),
            ANCIENT_DEBRIS = new OreDrops(Material.NETHERITE_SCRAP);

    @Override
    public OreDrops getOreDrops(Material material) {
        switch (material) {
            case IRON_ORE:
            case DEEPSLATE_IRON_ORE:
                return IRON_ORE;
            case GOLD_ORE:
            case DEEPSLATE_GOLD_ORE:
                return GOLD_ORE;
            case COPPER_ORE:
            case DEEPSLATE_COPPER_ORE:
                return COPPER_ORE;
            case ANCIENT_DEBRIS:
                return ANCIENT_DEBRIS;
            default:
                return null;
        }
    }

    @Override
    public float getAttackCooldown(Player player) {
        return player.getAttackCooldown();
    }

    private net.minecraft.world.item.ItemStack _CraftItemStack_asNMSCopy(ItemStack item) {
        try {
            return (net.minecraft.world.item.ItemStack) _CraftItemStack_asNMSCopy.invoke(null, item);
        } catch (Exception exception) {
            throw new RuntimeException("Reflection error", exception);
        }
    }

    @Override
    public int getFoodRestored(ItemStack item) {
        return _CraftItemStack_asNMSCopy(item).get(DataComponents.FOOD).nutrition();
    }

    @Override
    public float getSaturationRestored(ItemStack item) {
        return _CraftItemStack_asNMSCopy(item).get(DataComponents.FOOD).saturation();
    }

    /**
     * The {@link ComponentSerializer#parse(String)} will throw out an error if there
     * are any error with the given message. Since the 'message' parameter can be changed
     * by the user it's best to catch any exception and add an error message.
     * <p>
     * The stack trace is printed out to help the developer locate the issue with the message
     * format.
     */
    @Override
    public void sendJson(Player player, String message) {
        try {
            player.spigot().sendMessage(ChatMessageType.CHAT, ComponentSerializer.parse(message));
        } catch (RuntimeException exception) {
            MythicLib.plugin.getLogger().log(Level.WARNING, "Could not parse raw message sent to player. Make sure it has the right syntax");
            exception.printStackTrace();
        }
    }

    @Override
    public void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(message));
    }

    @Override
    public void sendActionBarRaw(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, ComponentSerializer.parse(message));
    }

    @Override
    public int getNextContainerId(Player player) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void handleInventoryCloseEvent(Player player) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void sendPacketOpenWindow(Player player, int containerId) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void sendPacketCloseWindow(Player player, int containerId) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void setActiveContainerDefault(Player player) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void setActiveContainer(Player player, Object container) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void setActiveContainerId(Object container, int containerId) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void addActiveContainerSlotListener(Object container, Player player) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public Inventory toBukkitInventory(Object container) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public Object newContainerAnvil(Player player) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public NBTItem getNBTItem(ItemStack item) {
        return new CraftNBTItem(item);
    }

    public class CraftNBTItem extends NBTItem {
        private final net.minecraft.world.item.ItemStack nms;
        private final CompoundTag compound;

        public CraftNBTItem(ItemStack item) {
            super(item);

            nms = _CraftItemStack_asNMSCopy(item);
            final CustomData customDataTag = nms.get(DataComponents.CUSTOM_DATA);
            compound = customDataTag == null ? new CompoundTag() : customDataTag.getUnsafe(); // F*ck
        }

        @Override
        public Object get(String path) {
            return compound.get(path);
        }

        @Override
        public String getString(String path) {
            return compound.getString(path);
        }

        @Override
        public boolean hasTag(String path) {
            return compound.contains(path);
        }

        @Override
        public boolean getBoolean(String path) {
            return compound.getBoolean(path);
        }

        @Override
        public double getDouble(String path) {
            return compound.getDouble(path);
        }

        @Override
        public int getInteger(String path) {
            return compound.getInt(path);
        }

        @Override
        public NBTCompound getNBTCompound(String path) {
            return new CraftNBTCompound(this, path);
        }

        @Override
        public NBTItem addTag(List<ItemTag> tags) {
            tags.forEach(tag -> {
                if (tag.getValue() instanceof Boolean) compound.putBoolean(tag.getPath(), (boolean) tag.getValue());
                else if (tag.getValue() instanceof Double) compound.putDouble(tag.getPath(), (double) tag.getValue());
                else if (tag.getValue() instanceof String) compound.putString(tag.getPath(), (String) tag.getValue());
                else if (tag.getValue() instanceof Integer) compound.putInt(tag.getPath(), (int) tag.getValue());
                else if (tag.getValue() instanceof List<?>) {
                    ListTag tagList = new ListTag();
                    for (Object s : (List<?>) tag.getValue())
                        if (s instanceof String) tagList.add(StringTag.valueOf((String) s));
                    compound.put(tag.getPath(), tagList);
                }
            });
            return this;
        }

        @Override
        public NBTItem removeTag(String... paths) {
            for (String path : paths)
                compound.remove(path);
            return this;
        }

        @Override
        public Set<String> getTags() {
            return compound.getAllKeys();
        }

        @Override
        public ItemStack toItem() {
            nms.set(DataComponents.CUSTOM_DATA, CustomData.of(compound));
            try {
                return (ItemStack) _CraftItemStack_asBukkitCopy.invoke(null, nms);
            } catch (Exception exception) {
                throw new RuntimeException("Reflection error", exception);
            }
        }

        @Override
        public int getTypeId(String path) {
            return compound.get(path).getId();
        }

        @Override
        public void setCanMine(Collection<Material> blocks) {
            List<net.minecraft.world.level.block.Block> nmsBlocks = blocks.stream().map(CraftBlockType::bukkitToMinecraft).collect(Collectors.toList());
            List<BlockPredicate> list = new ArrayList<>();
            // First argument is not needed
            list.add(BlockPredicate.Builder.block().of(null, nmsBlocks).build());
            nms.set(DataComponents.CAN_BREAK, new AdventureModePredicate(list, false));
        }
    }

    private static class CraftNBTCompound extends NBTCompound {
        private final CompoundTag compound;

        public CraftNBTCompound(CraftNBTItem item, String path) {
            super();
            compound = (item.hasTag(path) && NBTTypeHelper.COMPOUND.is(item.getTypeId(path))) ? item.compound.getCompound(path) : new CompoundTag();
        }

        public CraftNBTCompound(CraftNBTCompound comp, String path) {
            super();
            compound = (comp.hasTag(path) && NBTTypeHelper.COMPOUND.is(comp.getTypeId(path))) ? comp.compound.getCompound(path) : new CompoundTag();
        }

        @Override
        public boolean hasTag(String path) {
            return compound.contains(path);
        }

        @Override
        public Object get(String path) {
            return compound.get(path);
        }

        @Override
        public NBTCompound getNBTCompound(String path) {
            return new CraftNBTCompound(this, path);
        }

        @Override
        public String getString(String path) {
            return compound.getString(path);
        }

        @Override
        public boolean getBoolean(String path) {
            return compound.getBoolean(path);
        }

        @Override
        public double getDouble(String path) {
            return compound.getDouble(path);
        }

        @Override
        public int getInteger(String path) {
            return compound.getInt(path);
        }

        @Override
        public Set<String> getTags() {
            return compound.getAllKeys();
        }

        @Override
        public int getTypeId(String path) {
            return compound.get(path).getId();
        }
    }

    private ServerPlayer _CraftPlayer_getHandle(Player player) {
        try {
            return (ServerPlayer) _CraftPlayer_getHandle.invoke(player);
        } catch (Exception exception) {
            throw new RuntimeException("Reflection error", exception);
        }
    }

    @Override
    public void playArmAnimation(Player player) {
        ServerPlayer p = _CraftPlayer_getHandle(player);
        ServerGamePacketListenerImpl connection = p.connection;
        ClientboundAnimatePacket armSwing = new ClientboundAnimatePacket(p, 0);
        connection.send(armSwing);
        connection.handleAnimate(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
    }

    private ServerLevel _CraftWorld_getHandle(World world) {
        try {
            return (ServerLevel) _CraftWorld_getHandle.invoke(world);
        } catch (Exception exception) {
            throw new RuntimeException("Reflection error", exception);
        }
    }

    @Override
    public Sound getBlockPlaceSound(Block block) {
        ServerLevel nmsWorld = _CraftWorld_getHandle(block.getWorld());
        BlockState state = nmsWorld.getBlockState(new BlockPos(block.getX(), block.getY(), block.getZ()));
        return CraftSound.minecraftToBukkit(state.getSoundType().getPlaceSound());
    }

    @Override
    public String getSkullValue(Block block) {
        ServerLevel nmsWorld = _CraftWorld_getHandle(block.getWorld());
        SkullBlockEntity skull = (SkullBlockEntity) nmsWorld.getBlockEntity(new BlockPos(block.getX(), block.getY(), block.getZ()));
        if (skull.getOwnerProfile() == null) return "";
        return skull.getOwnerProfile()
                .gameProfile()
                .getProperties()
                .get("textures").iterator().next().value();
    }

    @Override
    public void setSkullValue(Block block, String value) {
        ServerLevel nmsWorld = _CraftWorld_getHandle(block.getWorld());
        SkullBlockEntity skull = (SkullBlockEntity) nmsWorld.getBlockEntity(new BlockPos(block.getX(), block.getY(), block.getZ()));
        GameProfile profile = new GameProfile(UUID.randomUUID(), PLAYER_PROFILE_NAME);
        profile.getProperties().put("textures", new Property("textures", value));
        skull.setOwner(new ResolvableProfile(profile));
        skull.setChanged();
    }

    @Override
    public FurnaceRecipe getFurnaceRecipe(String path, ItemStack item, Material material, float exp, int cook) {
        return new FurnaceRecipe(new NamespacedKey(MythicLib.inst(), "mmoitems_furnace_" + path), item, material, exp, cook);
    }

    @Override
    public Enchantment getEnchantmentFromString(String s) {
        return Enchantment.getByKey(NamespacedKey.minecraft(s));
    }

    @Override
    public FurnaceRecipe getFurnaceRecipe(NamespacedKey key, ItemStack item, Material material, float exp, int cook) {
        return new FurnaceRecipe(key, item, material, exp, cook);
    }

    @Override
    public boolean isCropFullyGrown(Block block) {
        if (block.getBlockData() instanceof Ageable) {
            Ageable ageable = (Ageable) block.getBlockData();
            return ageable.getAge() == ageable.getMaximumAge();
        }
        return false;
    }

    @Override
    public boolean isUndead(Entity entity) {
        EntityType type = entity.getType();
        return type == EntityType.SKELETON || type == EntityType.STRAY || type == EntityType.WITHER_SKELETON || type == EntityType.ZOMBIE || type == EntityType.DROWNED || type == EntityType.HUSK || type.name().equals("PIG_ZOMBIE") || type == EntityType.ZOMBIE_VILLAGER || type == EntityType.PHANTOM || type == EntityType.WITHER || type == EntityType.SKELETON_HORSE || type == EntityType.ZOMBIE_HORSE;
    }

    @Override
    public void setUUID(Player player, UUID uniqueId) {
        if (player.getUniqueId().equals(uniqueId)) return;

        // Update UUID inside of game profile
        final ServerPlayer handle = _CraftPlayer_getHandle(player);
        final GameProfile gameProfile = handle.getGameProfile();
        try {
            final Field _id = gameProfile.getClass().getDeclaredField("id");
            _id.setAccessible(true);
            _id.set(gameProfile, uniqueId);
            _id.setAccessible(false);
        } catch (Exception exception) {
            throw new RuntimeException("Could not update player UUID", exception);
        }

        handle.setUUID(uniqueId);
    }

    @Override
    public GameProfile getGameProfile(Player player) {
        try {
            return (GameProfile) _CraftPlayer_getProfile.invoke(player);
        } catch (Exception exception) {
            throw new RuntimeException("Reflection error", exception);
        }
    }

    @Override
    public AttributeModifier newAttributeModifier(NamespacedKey key, double amount, AttributeModifier.Operation operation) {
        return new AttributeModifier(key, amount, operation, EquipmentSlotGroup.ANY);
    }

    @Override
    public boolean matches(AttributeModifier modifier, NamespacedKey key) {
        return modifier.getKey().equals(key);
    }

    private static class InventoryViewImpl implements VInventoryView {
        private final InventoryView view;

        InventoryViewImpl(InventoryView view) {
            this.view = view;
        }

        @Override
        public String getTitle() {
            return view.getTitle();
        }

        @Override
        public InventoryType getType() {
            return view.getType();
        }

        @Override
        public Inventory getTopInventory() {
            return view.getTopInventory();
        }


        @Override
        public Inventory getBottomInventory() {
            return view.getBottomInventory();
        }

        @Override
        public void setCursor(ItemStack actualCursor) {
            view.setCursor(actualCursor);
        }

        @Override
        public HumanEntity getPlayer() {
            return view.getPlayer();
        }

        @Override
        public void close() {
            view.close();
        }
    }

    @Override
    public VInventoryView getView(InventoryEvent event) {
        return new InventoryViewImpl(event.getView());
    }

    @Override
    public VInventoryView getOpenInventory(Player player) {
        return new InventoryViewImpl(player.getOpenInventory());
    }

    @Override
    public InventoryClickEvent newInventoryClickEvent(VInventoryView view, InventoryType.SlotType type, int slot, ClickType click, InventoryAction action) {
        return new InventoryClickEvent(((InventoryViewImpl) view).view, type, slot, click, action);
    }
}