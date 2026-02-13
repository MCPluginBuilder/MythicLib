package io.lumine.mythic.lib.command.mythiclib.mythiclib.debug;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTCompound;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.util.NBTTypeHelper;
import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class NBTCommand extends CommandTreeNode {
    public NBTCommand(CommandTreeNode parent) {
        super(parent, "nbt");
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {

        if (!(sender instanceof Player)) {
            return explorer.fail("This command is only for players");
        }

        Player player = (Player) sender;
        JsonObject inventory = new JsonObject();
        if (isValid(player.getEquipment().getItemInMainHand()))
            inventory.add("mainhand", fromNBT(NBTItem.get(player.getEquipment().getItemInMainHand())));
        if (isValid(player.getEquipment().getItemInOffHand()))
            inventory.add("offhand", fromNBT(NBTItem.get(player.getEquipment().getItemInOffHand())));
        if (isValid(player.getEquipment().getHelmet()))
            inventory.add("helmet", fromNBT(NBTItem.get(player.getEquipment().getHelmet())));
        if (isValid(player.getEquipment().getChestplate()))
            inventory.add("chest", fromNBT(NBTItem.get(player.getEquipment().getChestplate())));
        if (isValid(player.getEquipment().getLeggings()))
            inventory.add("legs", fromNBT(NBTItem.get(player.getEquipment().getLeggings())));
        if (isValid(player.getEquipment().getBoots()))
            inventory.add("boots", fromNBT(NBTItem.get(player.getEquipment().getBoots())));

        if (inventory.size() == 0) explorer.fail("No NBT items found");
        //TODO else { explorer.verbose(upload(MythicLib.plugin.getJson().toString(inventory))); }

        return explorer.fail("Command currently disabled");
    }

    private boolean isValid(ItemStack stack) {
        return stack != null && stack.getType() != Material.AIR;
    }

    private JsonObject fromCompound(NBTCompound compound) {
        JsonObject data = new JsonObject();
        for (String tag : compound.getTags()) {
            final int typeId = compound.getTypeId(tag);
            if (NBTTypeHelper.COMPOUND.is(typeId))
                data.add(tag, fromCompound(compound.getNBTCompound(tag)));
            else {
                JsonObject nbtData = new JsonObject();
                switch (typeId) {
                    case 0:
                        data.addProperty(tag, "END");
                        break;
                    case 1:
                        nbtData.addProperty("byte", (byte) compound.get(tag));
                        nbtData.addProperty("boolean", ((byte) compound.get(tag)) == 1);
                        break;
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                        nbtData.addProperty("value", (Number) compound.get(tag));
                        break;
                    case 7:
                    case 11:
                    case 12:
                        nbtData.addProperty("array", compound.get(tag).toString());
                        break;
                    case 8:
                        nbtData.addProperty("string", compound.getString(tag));
                        break;
                    case 9:
                        try {
                            JsonElement json = MythicLib.plugin.getGson().fromJson(compound.get(tag).toString(), JsonElement.class);
                            if (json.isJsonNull()) nbtData.add("null_list", json.getAsJsonNull());
                            else if (json.isJsonPrimitive()) nbtData.add("primitive_list", json.getAsJsonPrimitive());
                            else if (json.isJsonArray()) nbtData.add("array_list", json.getAsJsonArray());
                            else if (json.isJsonObject()) nbtData.add("object_list", json.getAsJsonObject());
                        } catch (Exception e) {
                            nbtData.addProperty("unparsable_list", compound.get(tag).toString());
                        }
                        break;
                    default:
                        nbtData.addProperty("unknown", compound.get(tag).toString());
                }
                if (nbtData.size() != 0) {
                    nbtData.addProperty("typeid", typeId);
                    data.add(tag, nbtData);
                }
            }
        }
        return data;
    }

    private JsonObject fromNBT(NBTItem nbt) {
        JsonObject data = new JsonObject();
        for (String tag : nbt.getTags()) {
            final int typeId = nbt.getTypeId(tag);
            if (NBTTypeHelper.COMPOUND.is(typeId))
                data.add(tag, fromCompound(nbt.getNBTCompound(tag)));
            else {
                JsonObject nbtData = new JsonObject();
                switch (typeId) {
                    case 0:
                        data.addProperty(tag, "END");
                        break;
                    case 1:
                        nbtData.addProperty("byte", nbt.get(tag).toString());
                        nbtData.addProperty("boolean", nbt.getBoolean(tag));
                        break;
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                        nbtData.addProperty("value", nbt.get(tag).toString());
                        break;
                    case 7:
                    case 11:
                    case 12:
                        nbtData.addProperty("array", nbt.get(tag).toString());
                        break;
                    case 8:
                        try {
                            JsonElement json = MythicLib.plugin.getGson().fromJson(nbt.get(tag).toString(), JsonElement.class);
                            if (json.isJsonNull()) nbtData.add("null_string", json.getAsJsonNull());
                            else if (json.isJsonPrimitive()) nbtData.add("string", json.getAsJsonPrimitive());
                            else if (json.isJsonArray()) nbtData.add("string_list", json.getAsJsonArray());
                            else if (json.isJsonObject()) nbtData.add("string_object", json.getAsJsonObject());
                        } catch (Exception e) {
                            nbtData.addProperty("string", nbt.getString(tag));
                        }
                        break;
                    case 9:
                        try {
                            JsonElement json = MythicLib.plugin.getGson().fromJson(nbt.get(tag).toString(), JsonElement.class);
                            if (json.isJsonNull()) nbtData.add("null_list", json.getAsJsonNull());
                            else if (json.isJsonPrimitive()) nbtData.add("primitive_list", json.getAsJsonPrimitive());
                            else if (json.isJsonArray()) nbtData.add("array_list", json.getAsJsonArray());
                            else if (json.isJsonObject()) nbtData.add("object_list", json.getAsJsonObject());
                        } catch (Exception e) {
                            nbtData.addProperty("unparsable_list", nbt.get(tag).toString());
                        }
                        break;
                    default:
                        nbtData.addProperty("unknown", nbt.get(tag).toString());
                }
                if (nbtData.size() != 0) {
                    nbtData.addProperty("typeid", typeId);
                    data.add(tag, nbtData);
                }
            }
        }
        return data;
    }
}
