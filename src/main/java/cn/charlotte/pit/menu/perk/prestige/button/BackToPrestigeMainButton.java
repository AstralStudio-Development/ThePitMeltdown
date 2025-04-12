package cn.charlotte.pit.menu.perk.prestige.button;

import cn.charlotte.pit.menu.prestige.PrestigeMainMenu;
import cn.charlotte.pit.util.item.ItemBuilder;
import cn.charlotte.pit.util.menu.Button;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

/**
 * @Author: Starry_Killer
 * @Created_In: 2024/4/13 11:34
 */
public class BackToPrestigeMainButton extends Button {
    @Override
    public ItemStack getButtonItem(Player player) {
        return new ItemBuilder(Material.ARROW)
                .name("&a返回")
                .lore("&7点击回到精通主界面.")
                .build();
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType, int hotbarButton, ItemStack currentItem) {
        new PrestigeMainMenu().openMenu(player);
    }


}
