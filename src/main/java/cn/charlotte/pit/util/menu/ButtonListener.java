package cn.charlotte.pit.util.menu;

import cn.charlotte.pit.parm.AutoRegister;
import cn.charlotte.pit.util.thread.ThreadHelper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.PlayerInventory;

@AutoRegister
public class ButtonListener implements Listener {

    @EventHandler(priority=EventPriority.HIGH)
    public void onButtonPress(InventoryClickEvent event) {
        Player player = (Player)event.getWhoClicked();
        Menu openMenu = Menu.currentlyOpenedMenus.get(player.getUniqueId());
        if (openMenu != null) {
            if (event.getClickedInventory() instanceof PlayerInventory) {
                event.setCancelled(true);
            }
            openMenu.onClickEvent(event);
            if (event.getSlot() != event.getRawSlot()) {
                if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) {
                    event.setCancelled(true);
                }
                return;
            }
            if (openMenu.getButtons().containsKey(event.getSlot())) {
                Button button = openMenu.getButtons().get(event.getSlot());
                boolean cancel = button.shouldCancel(player, event.getSlot(), event.getClick());
                if (cancel){
                    event.setCancelled(true);
                }
                button.clicked(player, event.getSlot(), event.getClick(), event.getHotbarButton(), event.getCurrentItem());
                if (Menu.currentlyOpenedMenus.containsKey(player.getUniqueId())) {
                    Menu newMenu = Menu.currentlyOpenedMenus.get(player.getUniqueId());
                    if (newMenu != openMenu && button.shouldUpdate(player, event.getSlot(), event.getClick())) {
                        openMenu.setClosedByMenu(true);
                        newMenu.openMenu(player);
                    }
                } else if (button.shouldUpdate(player, event.getSlot(), event.getClick())) {
                    openMenu.setClosedByMenu(true);
                    openMenu.openMenu(player);
                }
                if (event.isCancelled()) {
                    ThreadHelper.Sync(player::updateInventory);
                }
            } else {
                if (event.getCurrentItem() != null) {
                    event.setCancelled(true);
                }
                if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player)event.getPlayer();
        Menu openMenu = Menu.currentlyOpenedMenus.get(player.getUniqueId());
        if (openMenu != null) {
            openMenu.onClose(player);
            Menu.currentlyOpenedMenus.remove(player.getUniqueId());
        }
    }
}