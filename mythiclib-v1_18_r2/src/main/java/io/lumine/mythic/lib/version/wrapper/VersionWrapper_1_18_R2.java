package io.lumine.mythic.lib.version.wrapper;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTCompound;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.util.NBTTypeHelper;
import io.lumine.mythic.lib.util.lang3.NotImplementedException;
import io.lumine.mythic.lib.version.OreDrops;
import io.lumine.mythic.lib.version.VInventoryView;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.craftbukkit.v1_18_R2.CraftSound;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.*;

public class VersionWrapper_1_18_R2 implements VersionWrapper {
    private final Set<Material> generatorOutputs = new HashSet<>();

    public VersionWrapper_1_18_R2() {
        generatorOutputs.add(Material.COBBLESTONE);
        generatorOutputs.add(Material.OBSIDIAN);
        generatorOutputs.add(Material.BASALT);
    }

    @Override
    public String getBiomeName(Biome biome) {
        return biome.name();
    }

    @Override
    public String getSoundName(Sound sound) {
        return sound.name();
    }

    @Override
    public Object getProfile(SkullMeta meta) {
        try {
            final Field profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            final Object profile = profileField.get(meta);
            profileField.setAccessible(false);
            return profile;
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            throw new IllegalArgumentException("Could not fetch skull profile:" + exception.getMessage());
        }
    }

    @Override
    public void setProfile(SkullMeta meta, Object object) {
        try {
            final Field profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, object);
            profileField.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            throw new IllegalArgumentException("Could not apply skull profile:" + exception.getMessage());
        }
    }

    @Override
    public Object newProfile(UUID uniqueId, String textureValue) {
        final GameProfile profile = new GameProfile(uniqueId, PLAYER_PROFILE_NAME);
        profile.getProperties().put("textures", new Property("textures", textureValue));
        return profile;
    }

    @Override
    public boolean isGeneratorOutput(Material material) {
        return generatorOutputs.contains(material);
    }

    @Override
    public boolean isHelmet(Material material) {
        return material.getEquipmentSlot() == EquipmentSlot.HEAD;
    }

    private static final OreDrops
            IRON_ORE = new OreDrops(Material.IRON_INGOT),
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

    @Override
    public int getFoodRestored(ItemStack item) {
        return CraftItemStack.asNMSCopy(item).getItem().getFoodProperties().getNutrition();
    }

    @Override
    public float getSaturationRestored(ItemStack item) {
        return CraftItemStack.asNMSCopy(item).getItem().getFoodProperties().getSaturationModifier();
    }

    @Override
    public void sendJson(Player player, String message) {
        clientboundChatPacket(player, message, ChatType.CHAT);
    }

    @Override
    public void sendActionBarRaw(Player player, String message) {
        clientboundChatPacket(player, message, ChatType.GAME_INFO);
    }

    private void clientboundChatPacket(Player player, String jsonMessage, ChatType chatType) {
        ((CraftPlayer) player).getHandle().connection.send(new ClientboundChatPacket(net.minecraft.network.chat.Component.Serializer.fromJson(jsonMessage), chatType, UUID.randomUUID()));
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

    private ServerPlayer toNMS(Player player) {
        return ((CraftPlayer) player).getHandle();
    }

    private class AnvilContainer extends AnvilMenu {
        public AnvilContainer(Player player) {
            super(getNextContainerId(player), ((CraftPlayer) player).getHandle().getInventory(),
                    ContainerLevelAccess.create(((CraftWorld) player.getWorld()).getHandle(), new BlockPos(0, 0, 0)));
            this.checkReachable = false;
            setTitle(new TextComponent("Repair & Name"));
        }
    }

    @Override
    public NBTItem getNBTItem(ItemStack item) {
        return new CraftNBTItem(item);
    }

    public static class CraftNBTItem extends NBTItem {
        private final net.minecraft.world.item.ItemStack nms;
        private final CompoundTag compound;

        public CraftNBTItem(ItemStack item) {
            super(item);

            nms = CraftItemStack.asNMSCopy(item);
            compound = nms.hasTag() ? nms.getTag() : new CompoundTag();
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
            nms.setTag(compound);
            return CraftItemStack.asBukkitCopy(nms);
        }

        @Override
        public int getTypeId(String path) {
            return compound.get(path).getId();
        }

        @Override
        public void setCanMine(Collection<Material> blocks) {
            throw new NotImplementedException("Not supported in <1.21");
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

    @Override
    public void playArmAnimation(Player player) {
        ServerPlayer p = ((CraftPlayer) player).getHandle();
        ServerGamePacketListenerImpl connection = p.connection;
        ClientboundAnimatePacket armSwing = new ClientboundAnimatePacket(p, 0);
        connection.send(armSwing);
        connection.handleAnimate(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
    }

    @Override
    public Sound getBlockPlaceSound(Block block) {
        ServerLevel nmsWorld = ((CraftWorld) block.getWorld()).getHandle();
        BlockState state = nmsWorld.getBlockState(new BlockPos(block.getX(), block.getY(), block.getZ()));
        return CraftSound.getBukkit(state.getSoundType().getPlaceSound());
    }

    @Override
    public String getSkullValue(Block block) {
        SkullBlockEntity skull = (SkullBlockEntity) ((CraftWorld) block.getWorld()).getHandle()
                .getBlockEntity(new BlockPos(block.getX(), block.getY(), block.getZ()));
        if (skull.getOwnerProfile() == null) return "";
        return skull.getOwnerProfile().getProperties().get("textures").iterator().next().getValue();
    }

    @Override
    public void setSkullValue(Block block, String value) {
        SkullBlockEntity skull = (SkullBlockEntity) ((CraftWorld) block.getWorld()).getHandle()
                .getBlockEntity(new BlockPos(block.getX(), block.getY(), block.getZ()));
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", value));
        skull.setOwner(profile);
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
        return type == EntityType.SKELETON || type == EntityType.STRAY || type == EntityType.WITHER_SKELETON || type == EntityType.ZOMBIE || type == EntityType.DROWNED || type == EntityType.HUSK || type.name()
                .equals("PIG_ZOMBIE") || type == EntityType.ZOMBIE_VILLAGER || type == EntityType.PHANTOM || type == EntityType.WITHER || type == EntityType.SKELETON_HORSE || type == EntityType.ZOMBIE_HORSE;
    }

    @Override
    public void setUUID(Player player, UUID uniqueId) {
        if (player.getUniqueId().equals(uniqueId)) return;

        // Update UUID inside of game profile
        final ServerPlayer handle = ((CraftPlayer) player).getHandle();
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
        return ((CraftPlayer) player).getProfile();
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
