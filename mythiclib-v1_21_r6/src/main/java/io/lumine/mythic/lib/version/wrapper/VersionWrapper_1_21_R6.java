package io.lumine.mythic.lib.version.wrapper;

import com.google.common.base.Preconditions;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTCompound;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.version.OreDrops;
import io.lumine.mythic.lib.version.VInventoryView;
import io.lumine.mythic.lib.version.impl.ModernGameProfileWrapper;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.AdventureModePredicate;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.block.data.Ageable;
import org.bukkit.craftbukkit.v1_21_R6.CraftSound;
import org.bukkit.craftbukkit.v1_21_R6.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R6.block.CraftBlockType;
import org.bukkit.craftbukkit.v1_21_R6.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_21_R6.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_21_R6.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class VersionWrapper_1_21_R6 implements VersionWrapper, ModernGameProfileWrapper {

    @Override
    public boolean damage(LivingEntity targetBukkit, double amount, Entity source) {
        var target = ((CraftLivingEntity) targetBukkit).getHandle();
        var level = target.level();
        if (!(level instanceof ServerLevel)) return false;

        Preconditions.checkState(!target.generation, "Cannot damage entity during world generation");
        DamageSource reason;
        if (source instanceof HumanEntity) {
            reason = target.damageSources().playerAttack(((CraftHumanEntity) source).getHandle());
        } else if (source instanceof LivingEntity) {
            reason = target.damageSources().mobAttack(((CraftLivingEntity) source).getHandle());
        } else {
            reason = target.damageSources().generic();
        }

        return target.hurtServer((ServerLevel) level, reason, (float) amount);
    }

    @Override
    public boolean isGeneratorOutput(Material material) {
        switch (material) {
            case COBBLESTONE:
            case OBSIDIAN:
            case BASALT:
                return true;
            default:
                return false;
        }
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
        return CraftItemStack.asNMSCopy(item).get(DataComponents.FOOD).nutrition();
    }

    @Override
    public float getSaturationRestored(ItemStack item) {
        return CraftItemStack.asNMSCopy(item).get(DataComponents.FOOD).saturation();
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
    public NBTItem getNBTItem(ItemStack item) {
        return new CraftNBTItem(item);
    }

    public static class CraftNBTItem extends NBTItem {
        private final net.minecraft.world.item.ItemStack nms;
        private final CompoundTag compound;

        public CraftNBTItem(ItemStack item) {
            super(item);

            nms = CraftItemStack.asNMSCopy(item);
            final CustomData customDataTag = nms.get(DataComponents.CUSTOM_DATA);
            compound = customDataTag == null ? new CompoundTag() : customDataTag.copyTag(); // F*ck
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
            List<net.minecraft.world.level.block.Block> nmsBlocks = blocks.stream().map(CraftBlockType::bukkitToMinecraft).collect(Collectors.toList());
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
            return CraftItemStack.asBukkitCopy(nms);
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

    @Override
    public void playArmAnimation(Player player) {
        player.swingMainHand();
    }

    @Override
    public Sound getBlockPlaceSound(Block block) {
        ServerLevel nmsWorld = ((CraftWorld) block.getWorld()).getHandle();
        BlockState state = nmsWorld.getBlockState(new BlockPos(block.getX(), block.getY(), block.getZ()));
        return CraftSound.minecraftToBukkit(state.getSoundType().getPlaceSound());
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