package cn.charlotte.pit.util.menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class MenuUpdateTask implements Runnable {

    @Override
    public void run() {
        Menu.currentlyOpenedMenus.forEach((uuid, menu) -> {
            final Player player = Bukkit.getPlayer(uuid);

            if (player != null && player.isOnline()) {
                if (menu.isAutoUpdate()) {
                    menu.openMenu(player);
                }
            }
        });
    }
}
