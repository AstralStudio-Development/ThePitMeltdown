package cn.charlotte.pit.util.menu;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.util.chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Objects;

public abstract class Menu {

    public static final Map<UUID, Menu> currentlyOpenedMenus = new ConcurrentHashMap<>();
    private Map<Integer, Button> buttons = new HashMap<>();
    private Map<Integer, Button> lastButtons = new HashMap<>();
    private String lastTitle = null;
    private boolean autoUpdate = false;
    private boolean updateAfterClick = true;
    private boolean closedByMenu = false;
    private boolean placeholder = false;
    private Button placeholderButton = Button.placeholder(Material.STAINED_GLASS_PANE, (byte) 15, " ");

    private ItemStack createItemStack(Player player, Button button) {
        ItemStack item = button.getButtonItem(player);
        return (item == null || item.getType() == Material.AIR) ? null : item;
    }

    private void performInitialOpen(Player player, int size, String title) {
        Inventory inventory = Bukkit.createInventory(player, size, title);
        currentlyOpenedMenus.put(player.getUniqueId(), this);

        populateButtons(player, inventory, this.buttons);

        if (this.isPlaceholder()) {
            fillPlaceholders(player, inventory, size);
        }

        player.openInventory(inventory);
        this.onOpen(player);
        this.setClosedByMenu(false);
    }

    private void populateButtons(Player player, Inventory inventory, Map<Integer, Button> buttonsToSet) {
        for (Map.Entry<Integer, Button> buttonEntry : buttonsToSet.entrySet()) {
            int slot = buttonEntry.getKey();
            if (slot >= 0 && slot < inventory.getSize()) {
                inventory.setItem(slot, createItemStack(player, buttonEntry.getValue()));
            } else {
                ThePit.getInstance().getLogger().warning("[Menu] Button defined for slot " + slot + " which is out of bounds for size " + inventory.getSize() + " in menu: " + this.getClass().getSimpleName());
            }
        }
    }

    private void fillPlaceholders(Player player, Inventory inventory, int size) {
        for (int index = 0; index < size; index++) {
            if (inventory.getItem(index) == null && this.buttons.get(index) == null) {
                inventory.setItem(index, this.placeholderButton.getButtonItem(player));
            }
        }
    }

    public void openMenu(final Player player) {
        if (player == null) return;
        final UUID playerUUID = player.getUniqueId();

        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(ThePit.getInstance(), () -> openMenu(player));
            return;
        }

        try {
            Map<Integer, Button> newButtons = this.getButtons(player);
            String newTitle = CC.translate(this.getTitle(player));
            if (newTitle.length() > 32) {
                newTitle = newTitle.substring(0, 32);
            }
            int newSize = this.getSize() == -1 ? this.size(newButtons) : this.getSize();

            Menu currentlyOpen = currentlyOpenedMenus.get(playerUUID);
            Inventory openInventory = player.getOpenInventory().getTopInventory();

            if (currentlyOpen == this && openInventory != null && openInventory.getSize() == newSize && Objects.equals(openInventory.getTitle(), newTitle)) {
                updateInventoryIncrementally(player, openInventory, newButtons, newTitle);
                player.updateInventory();
            } else {
                if (currentlyOpen != null) {
                    currentlyOpen.setClosedByMenu(true);
                }
                this.buttons = newButtons;
                this.lastButtons = new HashMap<>(newButtons);
                this.lastTitle = newTitle;
                performInitialOpen(player, newSize, newTitle);
            }

        } catch (Exception e) {
            CC.printError(player, e);
            if (currentlyOpenedMenus.get(playerUUID) == this) {
                currentlyOpenedMenus.remove(playerUUID);
            }
            player.closeInventory();
        }
    }

    private void updateInventoryIncrementally(Player player, Inventory inventory, Map<Integer, Button> newButtons, String newTitle) {
        Map<Integer, Button> currentButtons = new HashMap<>(newButtons);

        if (!Objects.equals(inventory.getTitle(), newTitle)) {
            ThePit.getInstance().getLogger().warning("[Menu] Title mismatch during incremental update for " + player.getName());
        }

        for (Map.Entry<Integer, Button> entry : currentButtons.entrySet()) {
            int slot = entry.getKey();
            Button newButton = entry.getValue();
            Button lastButton = lastButtons.get(slot);

            if (slot >= 0 && slot < inventory.getSize()) {
                ItemStack newItem = createItemStack(player, newButton);
                ItemStack currentItem = inventory.getItem(slot);

                boolean changed = !Objects.equals(newButton, lastButton) || !Objects.equals(newItem, currentItem);

                if (changed) {
                    inventory.setItem(slot, newItem);
                }
            }
        }

        for (Integer slot : lastButtons.keySet()) {
            if (!currentButtons.containsKey(slot)) {
                if (slot >= 0 && slot < inventory.getSize()) {
                    inventory.setItem(slot, null);
                }
            }
        }

        if (this.isPlaceholder()) {
            fillPlaceholders(player, inventory, inventory.getSize());
        }

        this.buttons = currentButtons;
        this.lastButtons = new HashMap<>(currentButtons);
        this.lastTitle = newTitle;
    }

    public int size(Map<Integer, Button> buttons) {
        int highest = 0;

        for (int buttonValue : buttons.keySet()) {
            if (buttonValue > highest) {
                highest = buttonValue;
            }
        }

        return (int) (Math.ceil((highest + 1) / 9D) * 9D);
    }

    public int getSlot(int x, int y) {
        return ((9 * y) + x);
    }

    public int getSize() {
        return -1;
    }

    public abstract String getTitle(Player player);

    public abstract Map<Integer, Button> getButtons(Player player);

    public void onOpen(Player player) {
    }

    public void onClose(Player player) {
    }

    public void onClickEvent(InventoryClickEvent event) {

    }

    public boolean isAutoUpdate() {
        return this.autoUpdate;
    }

    public void setAutoUpdate(boolean autoUpdate) {
        this.autoUpdate = autoUpdate;
    }

    public boolean isUpdateAfterClick() {
        return this.updateAfterClick;
    }

    public void setUpdateAfterClick(boolean updateAfterClick) {
        this.updateAfterClick = updateAfterClick;
    }

    public boolean isClosedByMenu() {
        return this.closedByMenu;
    }

    public void setClosedByMenu(boolean closedByMenu) {
        this.closedByMenu = closedByMenu;
    }

    public boolean isPlaceholder() {
        return this.placeholder;
    }

    public void setPlaceholder(boolean placeholder) {
        this.placeholder = placeholder;
    }

    public Button getPlaceholderButton() {
        return this.placeholderButton;
    }

    public void setPlaceholderButton(Button placeholderButton) {
        this.placeholderButton = placeholderButton;
    }

    public Map<Integer, Button> getButtons() {
        return this.buttons;
    }

    public void setButtons(Map<Integer, Button> buttons) {
        this.buttons = buttons;
    }

    public String getLastTitle() {
        return lastTitle;
    }
}