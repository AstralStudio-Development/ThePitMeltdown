package cn.charlotte.pit.util.inventory;

import cn.charlotte.pit.UtilKt;
import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.data.sub.PerkData;
import cn.charlotte.pit.data.sub.PlayerInv;
import cn.charlotte.pit.util.PlayerUtil;
import cn.charlotte.pit.util.chat.CC;
import cn.charlotte.pit.util.item.ItemBuilder;
import cn.charlotte.pit.util.item.ItemUtil;
import net.minecraft.server.v1_8_R3.*; // Keep NMS imports
// import org.bson.internal.Base64; // Remove BSON Base64
import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack; // Keep NMS dependency
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;

/**
 * @Author: EmptyIrony
 * @Date: 2020/12/30 23:40
 */
public class InventoryUtil {
    private static final Random random = new Random();
    // Cache common static items used in supplyItems
    private static final ItemStack AIR_ITEM = new ItemStack(Material.AIR); // Cache AIR
    private static final ItemStack WHITE_STAINED_GLASS_PANE = new ItemBuilder(Material.STAINED_GLASS_PANE).data(0).name("&f").build();
    private static final ItemStack GRAY_STAINED_GLASS_PANE = new ItemBuilder(Material.STAINED_GLASS_PANE).data(7).name("&8").build();

    public static int getInventoryEmptySlots(ItemStack[] itemStacks) {
        int slot = 0;
        if (itemStacks == null) return 0;
        for (ItemStack itemStack : itemStacks) {
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                slot++;
            }
        }
        return slot;
    }

    public static int getInventoryFilledSlots(ItemStack[] itemStacks) {
        int slot = 0;
        if (itemStacks == null) return 0;
        for (ItemStack itemStack : itemStacks) {
            if (itemStack != null && itemStack.getType() != Material.AIR) {
                slot++;
            }
        }
        return slot;
    }

    public static int getAmountOfItem(Player player, ItemStack item) {
        int amount = 0;
        if (player == null || item == null) return 0;
        PlayerInventory inv = player.getInventory();
        for (int i = 0; i < 36; i++) {
            ItemStack currentItem = inv.getItem(i);
            if (currentItem != null && currentItem.isSimilar(item)) {
                amount += currentItem.getAmount();
            }
        }
        return amount;
    }

    public static int getAmountOfItem(Player player, String internalName) {
        int amount = 0;
        if (player == null || internalName == null) return 0;
        PlayerInventory inv = player.getInventory();
        for (int i = 0; i < 36; i++) {
            ItemStack itemStack = inv.getItem(i);
            String currentInternalName = ItemUtil.getInternalName(itemStack);
            if (currentInternalName != null && currentInternalName.equals(internalName)) {
                amount += itemStack.getAmount();
            }
        }
        return amount;
    }

    public static boolean removeItem(Player player, ItemStack item, Integer amount) {
        if (player == null || item == null || amount <= 0) return false;
        if (getAmountOfItem(player, item) < amount) return false;

        int removed = 0;
        PlayerInventory inv = player.getInventory();
        ItemStack[] contents = inv.getContents();

        for (int i = 0; i < 36 && removed < amount; i++) {
            ItemStack currentItem = contents[i];
            if (currentItem != null && currentItem.isSimilar(item)) {
                int canRemove = Math.min(amount - removed, currentItem.getAmount());
                removed += canRemove;
                if (canRemove == currentItem.getAmount()) {
                    inv.setItem(i, null);
                } else {
                    currentItem.setAmount(currentItem.getAmount() - canRemove);
                }
            }
        }
        return removed >= amount;
    }

    public static boolean removeItem(Player player, String internalName, Integer amount) {
        if (player == null || internalName == null || amount <= 0) return false;
        if (getAmountOfItem(player, internalName) < amount) return false;

        int removed = 0;
        PlayerInventory inv = player.getInventory();
        ItemStack[] contents = inv.getContents();

        for (int i = 0; i < 36 && removed < amount; i++) {
            ItemStack currentItem = contents[i];
            String currentInternalName = ItemUtil.getInternalName(currentItem);
            if (currentInternalName != null && currentInternalName.equals(internalName)) {
                int canRemove = Math.min(amount - removed, currentItem.getAmount());
                removed += canRemove;
                if (canRemove == currentItem.getAmount()) {
                    inv.setItem(i, null);
                } else {
                    currentItem.setAmount(currentItem.getAmount() - canRemove);
                }
            }
        }
        return removed >= amount;
    }

    public static void removeItemWithInternalName(Player player, String name) {
        if (player == null || name == null) {
            return;
        }
        PlayerInventory inventory = player.getInventory();

        ItemStack cursorItem = player.getItemOnCursor();
        if (name.equals(ItemUtil.getInternalName(cursorItem))) {
            player.setItemOnCursor(null);
        }

        if (name.equals(ItemUtil.getInternalName(inventory.getHelmet()))) {
            inventory.setHelmet(null);
        }
        if (name.equals(ItemUtil.getInternalName(inventory.getChestplate()))) {
            inventory.setChestplate(null);
        }
        if (name.equals(ItemUtil.getInternalName(inventory.getLeggings()))) {
            inventory.setLeggings(null);
        }
        if (name.equals(ItemUtil.getInternalName(inventory.getBoots()))) {
            inventory.setBoots(null);
        }

        ItemStack[] contents = inventory.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack itemStack = contents[i];
            if (name.equals(ItemUtil.getInternalName(itemStack))) {
                inventory.setItem(i, null);
            }
        }
    }

    public static ItemStack[] fixInventoryOrder(ItemStack[] source) {
        if (source == null) return new ItemStack[36];
        if (source.length < 36) {
            return new ItemStack[36];
        }
        ItemStack[] fixed = new ItemStack[36];
        System.arraycopy(source, 0, fixed, 27, 9);
        System.arraycopy(source, 9, fixed, 0, 27);
        return fixed;
    }

    public static PlayerInv playerInventoryFromPlayer(Player player) {
        if (player == null) return null;
        final PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        if (profile != null && profile.isTempInvUsing()) {
            return profile.getInventory();
        }

        PlayerInv inv = new PlayerInv();
        ItemStack[] contents = player.getInventory().getContents();
        ItemStack[] armorContents = player.getInventory().getArmorContents();
        inv.setContents(contents == null ? new ItemStack[36] : contents.clone());
        inv.setArmorContents(armorContents == null ? new ItemStack[4] : armorContents.clone());
        return inv;
    }

    public static String itemsToString(ItemStack[] items) {
        if (items == null) return "null";

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < items.length; ++i) {
            builder.append(i).append("#").append(serializeItemStack(items[i]));
            if (i < items.length - 1) {
                builder.append(";");
            }
        }
        return builder.toString();
    }

    public static ItemStack[] stringToItems(String in) {
        if (in == null || in.isEmpty() || in.equalsIgnoreCase("unset") || in.equalsIgnoreCase("null") || in.equalsIgnoreCase("'null'")) {
            return new ItemStack[36];
        }

        String[] itemStrings = in.split(";");
        int highestIndex = -1;
        ItemStack[] tempContents = new ItemStack[itemStrings.length];
        for (String s : itemStrings) {
            String[] parts = s.split("#", 2);
            if (parts.length >= 1) {
                try {
                    int slot = Integer.parseInt(parts[0]);
                    if (slot > highestIndex) highestIndex = slot;
                    if (parts.length == 2 && !parts[1].isEmpty()) {
                        tempContents[slot] = deserializeItemStack(parts[1]);
                    } else {
                        tempContents[slot] = null;
                    }
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    Bukkit.getLogger().warning("[InventoryUtil] Malformed item entry in stringToItems: " + s);
                }
            }
        }

        ItemStack[] contents = new ItemStack[highestIndex + 1];
        System.arraycopy(tempContents, 0, contents, 0, Math.min(tempContents.length, contents.length));

        return contents;
    }

    public static String playerInvToString(PlayerInv inv) {
        if (inv == null) return "null";

        StringBuilder builder = new StringBuilder();
        ItemStack[] armor = inv.getArmorContents();
        if (armor == null) armor = new ItemStack[4];

        for (int i = 0; i < armor.length; i++) {
            builder.append(serializeItemStack(armor[i]));
            if (i < armor.length - 1) {
                builder.append(";");
            }
        }

        builder.append("|");

        ItemStack[] contents = inv.getContents();
        if (contents == null) contents = new ItemStack[36];
        for (int i = 0; i < contents.length; ++i) {
            builder.append(i).append("#").append(serializeItemStack(contents[i]));
            if (i < contents.length - 1) {
                builder.append(";");
            }
        }
        return builder.toString();
    }

    public static String playerInventoryToString(PlayerInventory inv) {
        if (inv == null) return "null";
        PlayerInv tempInv = new PlayerInv();
        ItemStack[] armor = inv.getArmorContents();
        ItemStack[] contents = inv.getContents();
        tempInv.setArmorContents(armor == null ? null : armor.clone());
        tempInv.setContents(contents == null ? null : contents.clone());
        return playerInvToString(tempInv);
    }

    public static PlayerInv playerInventoryFromString(String in) {
        if (in == null || in.isEmpty() || in.equalsIgnoreCase("unset") || in.equalsIgnoreCase("null") || in.equalsIgnoreCase("'null'")) {
            return null;
        }

        String[] parts = in.split("\\|", 2);
        if (parts.length != 2) {
            Bukkit.getLogger().warning("[InventoryUtil] Invalid player inventory string format (missing '|'): " + in);
            return null;
        }

        PlayerInv playerInv = new PlayerInv();

        String[] armorStrings = parts[0].split(";");
        ItemStack[] armor = new ItemStack[4];
        for (int i = 0; i < armor.length; i++) {
            if (i < armorStrings.length) {
                armor[i] = deserializeItemStack(armorStrings[i]);
            } else {
                armor[i] = null;
            }
        }
        playerInv.setArmorContents(armor);

        ItemStack[] contents = stringToItems(parts[1]);
        if (contents == null) {
            contents = new ItemStack[36];
        } else if (contents.length < 36) {
            ItemStack[] resizedContents = new ItemStack[36];
            System.arraycopy(contents, 0, resizedContents, 0, contents.length);
            contents = resizedContents;
        }
        playerInv.setContents(contents);

        return playerInv;
    }

    public static String inventoryToString(Inventory inv) {
        if (inv == null) return "null";
        StringBuilder builder = new StringBuilder();
        builder.append(inv.getSize()).append("|");
        ItemStack[] contents = inv.getContents();
        for (int i = 0; i < contents.length; ++i) {
            builder.append(i).append("#").append(serializeItemStack(contents[i]));
            if (i < contents.length - 1) {
                builder.append(";");
            }
        }
        return builder.toString();
    }

    public static Inventory inventoryFromString(String in) {
        if (in == null || in.isEmpty() || in.equalsIgnoreCase("unset") || in.equalsIgnoreCase("null") || in.equalsIgnoreCase("'null'")) {
            return null;
        }
        String[] parts = in.split("\\|", 2);
        if (parts.length != 2) {
            Bukkit.getLogger().warning("[InventoryUtil] Invalid generic inventory string format (missing '|'): " + in);
            return null;
        }
        int size;
        try {
            size = Integer.parseInt(parts[0]);
        } catch (NumberFormatException e) {
            Bukkit.getLogger().warning("[InventoryUtil] Invalid size in generic inventory string: " + parts[0]);
            return null;
        }

        Inventory inv = Bukkit.createInventory(null, size, "Deserialized Inventory");
        ItemStack[] contents = stringToItems(parts[1]);

        if (contents != null) {
            for (int i = 0; i < contents.length; i++) {
                if (i < size) {
                    inv.setItem(i, contents[i]);
                }
            }
        }
        return inv;
    }

    public static String serializeItemStack(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return "null";
        }
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
            nmsStack.save(nbtTagCompound);
            NBTCompressedStreamTools.a(nbtTagCompound, outputStream);
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (IOException | SecurityException | IllegalArgumentException e) {
            Bukkit.getLogger().log(Level.SEVERE, "[InventoryUtil] Error serializing ItemStack", e);
            return "null";
        }
    }

    public static ItemStack deserializeItemStack(String itemStackString) {
        if (itemStackString == null || itemStackString.isEmpty() || itemStackString.equalsIgnoreCase("null")) {
            return null;
        }
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(itemStackString);
            if (decodedBytes == null || decodedBytes.length == 0) {
                return null;
            }
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(decodedBytes)) {
                NBTTagCompound nbtTagCompound = NBTCompressedStreamTools.a(inputStream);
                net.minecraft.server.v1_8_R3.ItemStack nmsStack = net.minecraft.server.v1_8_R3.ItemStack.createStack(nbtTagCompound);
                return CraftItemStack.asBukkitCopy(nmsStack);
            } catch (IOException e) {
                Bukkit.getLogger().log(Level.SEVERE, "[InventoryUtil] IOException deserializing ItemStack from bytes", e);
                return null;
            }
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().log(Level.SEVERE, "[InventoryUtil] Error decoding Base64 string for ItemStack", e);
            return null;
        }
    }

    public static boolean isInvFull(Player player) {
        return player.getInventory().firstEmpty() == -1;
    }

    public static void supplyItems(Player player) {
        int arrowAmount = 0;
        boolean swordFound = false;
        boolean bowFound = false;
        int arrowSlot = 8;
        PlayerProfile playerProfile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        boolean miner = false;
        for (Map.Entry<Integer, PerkData> entry : playerProfile.getChosePerk().entrySet()) {
            if (entry.getValue().getPerkInternalName().equals("Miner")) {
                miner = true;
                break;
            }
        }
        int cobblestone = 0;

        int illegalItems = 0;

        int slot = 0;
        for (ItemStack item : player.getInventory()) {
            if (item == null) {
                slot++;
                continue;
            }
            if (ItemUtil.isIllegalItem(item)) {
                player.getInventory().remove(item);
                illegalItems++;
                slot++;
                continue;
            }

            //fixme: why remove the default item (sword & bow slot will reset)

            if (ItemUtil.isDefaultItem(item)) {
                //player.getInventory().remove(item);

                net.minecraft.server.v1_8_R3.ItemStack nmsItem = UtilKt.reflectGetNmsItem(item);
                if (nmsItem != null) {
                    Item itemType = nmsItem.getItem();
                    if (itemType instanceof ItemArmor) {
                        player.getInventory().remove(item);
                    }
                    if (itemType instanceof ItemSword && swordFound) {
                        player.getInventory().remove(item);
                        swordFound = false;
                    }
                    if (itemType instanceof ItemBow && bowFound) {
                        player.getInventory().remove(item);
                        swordFound = false;
                    }
                }
            }


            if (ItemUtil.isRemovedOnJoin(item)) {
                player.getInventory().remove(item);
                slot++;
                continue;
            }

            if ("default_sword".equals(ItemUtil.getInternalName(item))) {
                swordFound = true;
            } else if ("default_bow".equals(ItemUtil.getInternalName(item))) {
                bowFound = true;
            } else if (item.getType() == Material.ARROW) {
                arrowAmount += item.getAmount();
            }
            if (item.getType() == Material.COBBLESTONE) {
                cobblestone += item.getAmount();
            }
            slot++;
        }


        if (miner) {
            if (cobblestone < 32) {
                player.getInventory().addItem(new ItemBuilder(Material.COBBLESTONE).deathDrop(true).amount(32 - cobblestone).canDrop(false).canSaveToEnderChest(false).internalName("perk_miner").build());
            }
        }

        PlayerInventory inventory = player.getInventory();
        if (ItemUtil.isIllegalItem(inventory.getHelmet()) || ItemUtil.isRemovedOnJoin(inventory.getHelmet())) {
            inventory.setHelmet(null);
            illegalItems++;
        }
        if (ItemUtil.isIllegalItem(inventory.getChestplate()) || ItemUtil.isRemovedOnJoin(inventory.getChestplate())) {
            inventory.setChestplate(null);
            illegalItems++;
        }
        if (ItemUtil.isIllegalItem(inventory.getLeggings()) || ItemUtil.isRemovedOnJoin(inventory.getLeggings())) {
            inventory.setLeggings(null);
            illegalItems++;
        }
        if (ItemUtil.isIllegalItem(inventory.getBoots()) || ItemUtil.isRemovedOnJoin(inventory.getBoots())) {
            inventory.setBoots(null);
            illegalItems++;
        }

        if (illegalItems > 0) {
            player.sendMessage(CC.translate("&c我们从您的背包中找到 &e" + illegalItems + "&c 个异常物品，已从中移除,抱歉!"));
        }

        if (playerProfile.getPlayerOption().isOutfit()) {
            ItemStack air = new ItemStack(Material.AIR);
            if (ItemUtil.isDefaultItem(player.getInventory().getHelmet())) {
                player.getInventory().setHelmet(air);
            }
            if (ItemUtil.isDefaultItem(player.getInventory().getChestplate())) {
                player.getInventory().setChestplate(air);
            }
            if (ItemUtil.isDefaultItem(player.getInventory().getLeggings())) {
                player.getInventory().setLeggings(air);
            }
            if (ItemUtil.isDefaultItem(player.getInventory().getBoots())) {
                player.getInventory().setBoots(air);
            }

            if (!swordFound) {
                player.getInventory()
                        .addItem(new ItemBuilder(Material.IRON_SWORD).internalName("default_sword").defaultItem().canDrop(false).canSaveToEnderChest(false).buildWithUnbreakable());
            }
            if (!bowFound) {
                player.getInventory()
                        .addItem(new ItemBuilder(Material.BOW).internalName("default_bow").defaultItem().canDrop(false).canSaveToEnderChest(false).buildWithUnbreakable());
            }
        }
        /*
        player.getInventory()
                .remove(Material.ARROW);
        InventoryUtil.addInvReverse(player.getInventory(),
                (new ItemBuilder(Material.ARROW).internalName("default_arrow").defaultItem().canDrop(false).canSaveToEnderChest(false).amount(32).build()));

         */

        int maxArrow = 32 + Math.max(0, PlayerUtil.getPlayerUnlockedPerkLevel(player, "arrow_armory_perk") * 8);
        if (arrowAmount > 0 && arrowAmount <= maxArrow) {
            if (arrowAmount != maxArrow) {
                ItemBuilder arrowBuilder = new ItemBuilder(Material.ARROW).internalName("default_arrow").defaultItem().canDrop(false).canSaveToEnderChest(false);
                player.getInventory().addItem(
                        arrowBuilder.amount(maxArrow - arrowAmount).build()
                );
            }
        } else {
            if (arrowAmount > maxArrow) {
                player.getInventory().remove(Material.ARROW);
            }
            ItemBuilder arrowBuilder = new ItemBuilder(Material.ARROW).internalName("default_arrow").defaultItem().canDrop(false).canSaveToEnderChest(false);
            InventoryUtil.addInvReverse(player.getInventory(),
                    (arrowBuilder.amount(maxArrow).build()));
        }

        if (playerProfile.getPlayerOption().isOutfit()) {
            int ironArmorSlot = random.nextInt(3);

            if (player.getInventory().getChestplate() == null || ItemUtil.isDefaultItem(player.getInventory().getChestplate())) {
                if (ironArmorSlot == 0) {
                    player.getInventory().setChestplate(new ItemBuilder(Material.IRON_CHESTPLATE).defaultItem().internalName("default_armor").canDrop(false).canSaveToEnderChest(true).buildWithUnbreakable());
                } else {
                    player.getInventory().setChestplate(new ItemBuilder(Material.CHAINMAIL_CHESTPLATE).defaultItem().internalName("default_armor").canDrop(false).canSaveToEnderChest(true).buildWithUnbreakable());
                }
            }
            if (player.getInventory().getLeggings() == null || ItemUtil.isDefaultItem(player.getInventory().getLeggings())) {
                if (ironArmorSlot == 1) {
                    player.getInventory().setLeggings(new ItemBuilder(Material.IRON_LEGGINGS).defaultItem().internalName("default_armor").canDrop(false).canSaveToEnderChest(true).buildWithUnbreakable());
                } else {
                    player.getInventory().setLeggings(new ItemBuilder(Material.CHAINMAIL_LEGGINGS).defaultItem().internalName("default_armor").canDrop(false).canSaveToEnderChest(true).buildWithUnbreakable());
                }
            }
            if (player.getInventory().getBoots() == null || ItemUtil.isDefaultItem(player.getInventory().getBoots())) {
                if (ironArmorSlot == 2) {
                    player.getInventory().setBoots(new ItemBuilder(Material.IRON_BOOTS).defaultItem().internalName("default_armor").canDrop(false).canSaveToEnderChest(true).buildWithUnbreakable());
                } else {
                    player.getInventory().setBoots(new ItemBuilder(Material.CHAINMAIL_BOOTS).defaultItem().internalName("default_armor").canDrop(false).canSaveToEnderChest(true).buildWithUnbreakable());
                }
            }
        }
    }

    public static int getArmorSlot(Material material) {
        if (material == null) return -1;
        String name = material.name();
        if (name.endsWith("_HELMET")) return 3;
        if (name.endsWith("_CHESTPLATE")) return 2;
        if (name.endsWith("_LEGGINGS")) return 1;
        if (name.endsWith("_BOOTS")) return 0;
        return -1;
    }

    private static int firstPartial(PlayerInventory playerInv, ItemStack item) {
        if (item == null) {
            return -1;
        }
        ItemStack[] inventory = playerInv.getContents();
        int limit = item.getMaxStackSize();
        for (int i = 0; i < inventory.length; ++i) {
            ItemStack cItem = inventory[i];
            if (cItem != null && cItem.getAmount() < limit && cItem.isSimilar(item)) {
                return i;
            }
        }
        return -1;
    }

    private static int firstEmpty(PlayerInventory playerInv) {
        ItemStack[] inventory = playerInv.getContents();
        for (int i = 0; i < inventory.length; ++i) {
            if (inventory[i] == null) {
                return i;
            }
        }
        return -1;
    }

    public static void addInvReverse(PlayerInventory inventory, ItemStack item) {
        if (inventory == null || item == null || item.getAmount() <= 0) {
            return;
        }

        int amountToAdd = item.getAmount();
        int maxStackSize = item.getMaxStackSize();

        for (int i = 8; i >= 0; --i) {
            ItemStack current = inventory.getItem(i);
            if (current != null && current.getAmount() < maxStackSize && current.isSimilar(item)) {
                int canAdd = Math.min(amountToAdd, maxStackSize - current.getAmount());
                current.setAmount(current.getAmount() + canAdd);
                amountToAdd -= canAdd;
                if (amountToAdd <= 0) return;
            }
        }

        for (int i = 35; i >= 9; --i) {
            ItemStack current = inventory.getItem(i);
            if (current != null && current.getAmount() < maxStackSize && current.isSimilar(item)) {
                int canAdd = Math.min(amountToAdd, maxStackSize - current.getAmount());
                current.setAmount(current.getAmount() + canAdd);
                amountToAdd -= canAdd;
                if (amountToAdd <= 0) return;
            }
        }

        if (amountToAdd > 0) {
            for (int i = 8; i >= 0; --i) {
                if (inventory.getItem(i) == null) {
                    int canAdd = Math.min(amountToAdd, maxStackSize);
                    ItemStack newItem = item.clone();
                    newItem.setAmount(canAdd);
                    inventory.setItem(i, newItem);
                    amountToAdd -= canAdd;
                    if (amountToAdd <= 0) return;
                }
            }
        }

        if (amountToAdd > 0) {
            for (int i = 35; i >= 9; --i) {
                if (inventory.getItem(i) == null) {
                    int canAdd = Math.min(amountToAdd, maxStackSize);
                    ItemStack newItem = item.clone();
                    newItem.setAmount(canAdd);
                    inventory.setItem(i, newItem);
                    amountToAdd -= canAdd;
                    if (amountToAdd <= 0) return;
                }
            }
        }
    }

    public static boolean isInvFull(PlayerInventory inventory) {
        return firstEmpty(inventory) == -1;
    }
}
