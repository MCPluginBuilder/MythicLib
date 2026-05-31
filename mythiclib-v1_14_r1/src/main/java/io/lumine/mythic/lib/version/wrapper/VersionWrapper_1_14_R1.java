package io.lumine.mythic.lib.version.wrapper;

import com.mojang.authlib.properties.Property;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTCompound;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.util.NBTTypeHelper;
import io.lumine.mythic.lib.util.lang3.NotImplementedException;
import io.lumine.mythic.lib.version.OreDrops;
import io.lumine.mythic.lib.version.VInventoryView;
import io.lumine.mythic.lib.version.api.GameProfile;
import net.minecraft.server.v1_14_R1.*;
import net.minecraft.server.v1_14_R1.IChatBaseComponent.ChatSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class VersionWrapper_1_14_R1 implements VersionWrapper {

    @Override
    public boolean damage(LivingEntity target, double amount, Entity source) {
        DamageSource reason;
        if (source instanceof HumanEntity) {
            reason = DamageSource.playerAttack(((CraftHumanEntity) source).getHandle());
        } else if (source instanceof LivingEntity) {
            reason = DamageSource.mobAttack(((CraftLivingEntity) source).getHandle());
        } else {
            reason = DamageSource.GENERIC;
        }

        return ((CraftLivingEntity) target).getHandle().damageEntity(reason, (float) amount);
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
    public boolean isGeneratorOutput(Material material) {
        return material == Material.COBBLESTONE || material == Material.OBSIDIAN;
    }

    @Override
    public EquipmentSlot getEquipmentSlot(Material material) {
        switch (material) {

            // Helmets
            case LEATHER_HELMET: // Armor
            case CHAINMAIL_HELMET:
            case DIAMOND_HELMET:
            case GOLDEN_HELMET:
            case IRON_HELMET:
            case TURTLE_HELMET:
            case CARVED_PUMPKIN: // Unconventional
            case PLAYER_HEAD:
            case CREEPER_HEAD:
            case DRAGON_HEAD:
            case ZOMBIE_HEAD:
            case SKELETON_SKULL:
            case WITHER_SKELETON_SKULL:
                return EquipmentSlot.HEAD;

            // Chestplates
            case GOLDEN_CHESTPLATE:  // Armor
            case IRON_CHESTPLATE:
            case LEATHER_CHESTPLATE:
            case CHAINMAIL_CHESTPLATE:
            case DIAMOND_CHESTPLATE:
            case ELYTRA: // Unconventional
                return EquipmentSlot.CHEST;

            // Leggings
            case LEATHER_LEGGINGS:
            case CHAINMAIL_LEGGINGS:
            case DIAMOND_LEGGINGS:
            case GOLDEN_LEGGINGS:
            case IRON_LEGGINGS:
                return EquipmentSlot.LEGS;

            // Boots
            case LEATHER_BOOTS:
            case CHAINMAIL_BOOTS:
            case DIAMOND_BOOTS:
            case GOLDEN_BOOTS:
            case IRON_BOOTS:
                return EquipmentSlot.FEET;

            // Anything else
            default:
                return null;
        }
    }

    private static final OreDrops
            IRON_ORE = new OreDrops(Material.IRON_INGOT),
            GOLD_ORE = new OreDrops(Material.GOLD_INGOT);

    @Override
    public OreDrops getOreDrops(Material material) {
        switch (material) {
            case IRON_ORE:
                return IRON_ORE;
            case GOLD_ORE:
                return GOLD_ORE;
            default:
                return null;
        }
    }

    @Override
    public float getAttackCooldown(Player player) {
        return ((CraftPlayer) player).getHandle().s(0.5f);
    }

    @Override
    public int getFoodRestored(ItemStack item) {
        return CraftItemStack.asNMSCopy(item).getItem().getFoodInfo().getNutrition();
    }

    @Override
    public float getSaturationRestored(ItemStack item) {
        return CraftItemStack.asNMSCopy(item).getItem().getFoodInfo().getSaturationModifier();
    }

    @Override
    public void sendJson(Player player, String message) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutChat(ChatSerializer.a(message)));
    }

    @Override
    public void sendActionBarRaw(Player player, String message) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutChat(ChatSerializer.a(message), ChatMessageType.GAME_INFO));
    }

    @Override
    public NBTItem getNBTItem(org.bukkit.inventory.ItemStack item) {
        return new NBTItem_v1_14_R1(item);
    }

    public static class NBTItem_v1_14_R1 extends NBTItem {
        private final net.minecraft.server.v1_14_R1.ItemStack nms;
        private final NBTTagCompound compound;

        public NBTItem_v1_14_R1(org.bukkit.inventory.ItemStack item) {
            super(item);

            nms = CraftItemStack.asNMSCopy(item);
            compound = nms.hasTag() ? nms.getTag() : new NBTTagCompound();
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
            return compound.hasKey(path);
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
            return new NBTCompound_v1_14_R1(this, path);
        }

        @Override
        public NBTItem addTag(List<ItemTag> tags) {
            tags.forEach(tag -> {
                if (tag.getValue() instanceof Boolean)
                    compound.setBoolean(tag.getPath(), (boolean) tag.getValue());
                else if (tag.getValue() instanceof Double)
                    compound.setDouble(tag.getPath(), (double) tag.getValue());
                else if (tag.getValue() instanceof String)
                    compound.setString(tag.getPath(), (String) tag.getValue());
                else if (tag.getValue() instanceof Integer)
                    compound.setInt(tag.getPath(), (int) tag.getValue());
                else if (tag.getValue() instanceof List<?>) {
                    NBTTagList tagList = new NBTTagList();
                    for (Object s : (List<?>) tag.getValue())
                        if (s instanceof String)
                            tagList.add(new NBTTagString((String) s));
                    compound.set(tag.getPath(), tagList);
                }
            });
            return this;
        }

        @Override
        public NBTItem setDouble(String path, double value) {
            compound.setDouble(path, value);
            return this;
        }

        @Override
        public NBTItem setBoolean(String path, boolean value) {
            compound.setBoolean(path, value);
            return this;
        }

        @Override
        public NBTItem setInteger(String path, int value) {
            compound.setInt(path, value);
            return this;
        }

        @Override
        public NBTItem setString(String path, String value) {
            compound.setString(path, value);
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
            return compound.getKeys();
        }

        @Override
        public org.bukkit.inventory.ItemStack toItem() {
            nms.setTag(compound);
            return CraftItemStack.asBukkitCopy(nms);
        }

        @Override
        public int getTypeId(String path) {
            return compound.get(path).getTypeId();
        }

        @Override
        public void setCanMine(Collection<Material> blocks) {
            throw new NotImplementedException("Not supported in <1.21");
        }
    }

    private static class NBTCompound_v1_14_R1 extends NBTCompound {
        private final NBTTagCompound compound;

        public NBTCompound_v1_14_R1(NBTItem_v1_14_R1 item, String path) {
            super();
            compound = (item.hasTag(path) && NBTTypeHelper.COMPOUND.is(item.getTypeId(path))) ? item.compound.getCompound(path) : new NBTTagCompound();
        }

        public NBTCompound_v1_14_R1(NBTCompound_v1_14_R1 comp, String path) {
            super();
            compound = (comp.hasTag(path) && NBTTypeHelper.COMPOUND.is(comp.getTypeId(path))) ? comp.compound.getCompound(path) : new NBTTagCompound();
        }

        @Override
        public boolean hasTag(String path) {
            return compound.hasKey(path);
        }

        @Override
        public Object get(String path) {
            return compound.get(path);
        }

        @Override
        public NBTCompound getNBTCompound(String path) {
            return new NBTCompound_v1_14_R1(this, path);
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
            return compound.getKeys();
        }

        @Override
        public int getTypeId(String path) {
            return compound.get(path).getTypeId();
        }
    }

    @Override
    public void playArmAnimation(Player player) {
        EntityPlayer p = ((CraftPlayer) player).getHandle();
        PlayerConnection connection = p.playerConnection;
        PacketPlayOutAnimation armSwing = new PacketPlayOutAnimation(p, 0);
        connection.sendPacket(armSwing);
        connection.a(new PacketPlayInArmAnimation(EnumHand.MAIN_HAND));
    }

    @Override
    public Sound getBlockPlaceSound(org.bukkit.block.Block block) {
        try {
            World nmsWorld = ((CraftWorld) block.getWorld()).getHandle();

            net.minecraft.server.v1_14_R1.Block nmsBlock = nmsWorld.getType(new BlockPosition(block.getX(), block.getY(), block.getZ())).getBlock();
            SoundEffectType soundEffectType = nmsBlock.getStepSound(nmsBlock.getBlockData());

            Field breakSound = SoundEffectType.class.getDeclaredField("y");
            breakSound.setAccessible(true);
            SoundEffect nmsSound = (SoundEffect) breakSound.get(soundEffectType);

            Field keyField = SoundEffect.class.getDeclaredField("a");
            keyField.setAccessible(true);
            MinecraftKey nmsString = (MinecraftKey) keyField.get(nmsSound);

            return Sound.valueOf(nmsString.getKey().replace(".", "_").toUpperCase());
        } catch (IllegalAccessException | NoSuchFieldException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public String getSkullValue(Block block) {
        TileEntitySkull skullTile = (TileEntitySkull) ((CraftWorld) block.getWorld()).getHandle()
                .getTileEntity(new BlockPosition(block.getX(), block.getY(), block.getZ()));
        if (skullTile.gameProfile == null)
            return "";
        return skullTile.gameProfile.getProperties().get("textures").iterator().next().getValue();
    }

    @Override
    public void setSkullValue(Block block, String value) {
        var skullTile = (TileEntitySkull) ((CraftWorld) block.getWorld()).getHandle()
                .getTileEntity(new BlockPosition(block.getX(), block.getY(), block.getZ()));
        var profile = new com.mojang.authlib.GameProfile(UtilityMethods.uniqueIdFromString(value), PLAYER_PROFILE_NAME);
        profile.getProperties().put("textures", new Property("textures", value));
        skullTile.setGameProfile(profile);
        skullTile.update();
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

    @Override
    public GameProfile getProfile(SkullMeta meta) {
        try {

            // Access field using reflection
            final Field profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            final var profileObject = (com.mojang.authlib.GameProfile) profileField.get(meta);
            profileField.setAccessible(false);

            return new GameProfileImpl(profileObject);
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            throw new IllegalArgumentException("Could not fetch skull profile:" + exception.getMessage());
        }
    }

    @Override
    public void setProfile(SkullMeta meta, GameProfile profile) {
        try {
            final Field profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile == null ? null : ((GameProfileImpl) profile).bukkit);
            profileField.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            throw new IllegalArgumentException("Could not apply skull profile:" + exception.getMessage());
        }
    }

    @Override
    public GameProfile newProfile(UUID uniqueId, String textureValue) {
        final var profile = new com.mojang.authlib.GameProfile(uniqueId, VersionWrapper.PLAYER_PROFILE_NAME);
        profile.getProperties().put("textures", new Property("textures", textureValue));
        return new GameProfileImpl(profile);
    }

    static class GameProfileImpl implements GameProfile {
        public final com.mojang.authlib.GameProfile bukkit;

        public GameProfileImpl(com.mojang.authlib.GameProfile bukkit) {
            this.bukkit = bukkit;
        }

        @Override
        public String getTextureValue() {
            for (var prop : bukkit.getProperties().get("textures"))
                return prop.getValue();
            return null;
        }

        @Override
        @Nullable
        public UUID getUniqueId() {
            return bukkit.getId();
        }

        @Override
        @Nullable
        public String getName() {
            return bukkit.getName();
        }
    }
}

