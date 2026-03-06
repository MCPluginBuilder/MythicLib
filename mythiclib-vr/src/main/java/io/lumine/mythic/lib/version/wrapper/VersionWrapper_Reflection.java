package io.lumine.mythic.lib.version.wrapper;

import com.mojang.authlib.GameProfile;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTCompound;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.version.OreDrops;
import io.lumine.mythic.lib.version.ServerVersion;
import io.lumine.mythic.lib.version.VInventoryView;
import io.lumine.mythic.lib.version.impl.ModernGameProfileWrapper;
import io.lumine.mythic.lib.version.impl.ModernInventoryViewImpl;
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
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.block.data.Ageable;
import org.bukkit.craftbukkit.v1_21_R6.CraftWorld;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class VersionWrapper_Reflection implements VersionWrapper, ModernGameProfileWrapper {
    private final Set<Material> generatorOutputs = new HashSet<>();

    // Reflection stuff
    private final ServerVersion version;
    private final Method _CraftWorld_getHandle, _CraftPlayer_getHandle, _CraftItemStack_asNMSCopy, _CraftItemStack_asBukkitCopy, _CraftSound_minecraftToBukkit;
    private final Function<Material, net.minecraft.world.level.block.Block> _CraftBlockType_bukkitToMinecraft;

    public VersionWrapper_Reflection(ServerVersion version) throws NoSuchMethodException, ClassNotFoundException {
        generatorOutputs.add(Material.COBBLESTONE);
        generatorOutputs.add(Material.OBSIDIAN);
        generatorOutputs.add(Material.BASALT);

        this.version = version;

        // Classes
        var _CraftWorld = obcClass("CraftWorld");
        var _CraftPlayer = obcClass("entity.CraftPlayer");
        var _CraftItemStack = obcClass("inventory.CraftItemStack");
        var _CraftSound = obcClass("CraftSound");
        var _CraftBlockType = obcClass("block.CraftBlockType");

        // Methods
        _CraftWorld_getHandle = _CraftWorld.getDeclaredMethod("getHandle");
        _CraftPlayer_getHandle = _CraftPlayer.getDeclaredMethod("getHandle");
        _CraftItemStack_asNMSCopy = _CraftItemStack.getDeclaredMethod("asNMSCopy", ItemStack.class);
        _CraftItemStack_asBukkitCopy = _CraftItemStack.getDeclaredMethod("asBukkitCopy", net.minecraft.world.item.ItemStack.class);
        _CraftSound_minecraftToBukkit = _CraftSound.getDeclaredMethod("minecraftToBukkit", net.minecraft.sounds.SoundEvent.class);

        // Lambdas
        final var _CraftBlockType_bukkitToMinecraft = _CraftBlockType.getDeclaredMethod("bukkitToMinecraft", Material.class);
        this._CraftBlockType_bukkitToMinecraft = material -> {
            try {
                return (net.minecraft.world.level.block.Block) _CraftBlockType_bukkitToMinecraft.invoke(null, material);
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        };
    }

    private Class<?> obcClass(String obcClassPath) throws ClassNotFoundException {

        // Paper 1.20.5+
        if (version.isAbove(1, 20, 5) && version.isPaper())
            return Class.forName("org.bukkit.craftbukkit." + obcClassPath);

        // Spigot || Paper <1.20.5
        return Class.forName("org.bukkit.craftbukkit." + version.getCraftBukkitVersion() + "." + obcClassPath);
    }

    @Override
    public boolean isGeneratorOutput(Material material) {
        return generatorOutputs.contains(material);
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
            compound = customDataTag == null ? new CompoundTag() : customDataTag.copyTag();
        }

        @Override
        public Object get(String path) {
            return compound.get(path);
        }

        @Override
        public String getString(String path) {
            return compound.getStringOr(path, "");
        }

        @Override
        public boolean hasTag(String path) {
            return compound.contains(path);
        }

        @Override
        public boolean getBoolean(String path) {
            return compound.getBooleanOr(path, false);
        }

        @Override
        public double getDouble(String path) {
            return compound.getDoubleOr(path, 0);
        }

        @Override
        public int getInteger(String path) {
            return compound.getIntOr(path, 0);
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
        public NBTItem setDouble(String path, double value) {
            compound.putDouble(path, value);
            return this;
        }

        @Override
        public NBTItem setBoolean(String path, boolean value) {
            compound.putBoolean(path, value);
            return this;
        }

        @Override
        public NBTItem setInteger(String path, int value) {
            compound.putInt(path, value);
            return this;
        }

        @Override
        public NBTItem setString(String path, String value) {
            compound.putString(path, value);
            return this;
        }

        @Override
        public void setCanMine(Collection<Material> blocks) {
            List<net.minecraft.world.level.block.Block> nmsBlocks = blocks.stream().map(_CraftBlockType_bukkitToMinecraft).collect(Collectors.toList());
            List<BlockPredicate> list = new ArrayList<>();
            // First argument is not needed
            list.add(BlockPredicate.Builder.block().of(null, nmsBlocks).build());
            nms.set(DataComponents.CAN_BREAK, new AdventureModePredicate(list));
        }

        @Override
        public NBTItem removeTag(String... paths) {
            for (String path : paths)
                compound.remove(path);
            return this;
        }

        @Override
        public Set<String> getTags() {
            return compound.keySet();
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
    }

    private static class CraftNBTCompound extends NBTCompound {
        private final CompoundTag compound;

        public CraftNBTCompound(CraftNBTItem item, String path) {
            super();

            compound = item.compound.getCompoundOrEmpty(path);
        }

        public CraftNBTCompound(CraftNBTCompound comp, String path) {
            super();

            compound = comp.compound.getCompoundOrEmpty(path);
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
            return compound.getStringOr(path, "");
        }

        @Override
        public boolean getBoolean(String path) {
            return compound.getBooleanOr(path, false);
        }

        @Override
        public double getDouble(String path) {
            return compound.getDoubleOr(path, 0);
        }

        @Override
        public int getInteger(String path) {
            return compound.getIntOr(path, 0);
        }

        @Override
        public Set<String> getTags() {
            return compound.keySet();
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
        try {
            return (Sound) _CraftSound_minecraftToBukkit.invoke(null, state.getSoundType().getPlaceSound());
        } catch (Exception exception) {
            throw new RuntimeException("Reflection error", exception);
        }
    }

    @Override
    public String getSkullValue(Block block) {
        SkullBlockEntity skull = (SkullBlockEntity) ((CraftWorld) block.getWorld()).getHandle().getBlockEntity(new BlockPos(block.getX(), block.getY(), block.getZ()));
        if (skull.getOwnerProfile() == null) return "";
        return skull.getOwnerProfile().partialProfile().properties().get("textures").iterator().next().value();
    }

    @Override
    public void setSkullValue(Block block, String textureValue) {
        final var state = (Skull) block.getState();
        state.setOwnerProfile(newProfile(UtilityMethods.uniqueIdFromString(textureValue), textureValue).bukkit);
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
    public AttributeModifier newAttributeModifier(NamespacedKey key, double amount, AttributeModifier.Operation operation) {
        return new AttributeModifier(key, amount, operation, EquipmentSlotGroup.ANY);
    }

    @Override
    public boolean matches(AttributeModifier modifier, NamespacedKey key) {
        return modifier.getKey().equals(key);
    }

    @Override
    public VInventoryView getView(InventoryEvent event) {
        return new ModernInventoryViewImpl(event.getView());
    }

    @Override
    public VInventoryView getOpenInventory(Player player) {
        return new ModernInventoryViewImpl(player.getOpenInventory());
    }

    @Override
    public InventoryClickEvent newInventoryClickEvent(VInventoryView view, InventoryType.SlotType type, int slot, ClickType click, InventoryAction action) {
        return new InventoryClickEvent(((ModernInventoryViewImpl) view).view, type, slot, click, action);
    }
}