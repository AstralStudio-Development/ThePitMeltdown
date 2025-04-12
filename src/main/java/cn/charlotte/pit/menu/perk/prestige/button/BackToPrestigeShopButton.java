package cn.charlotte.pit.menu.perk.prestige.button;

import cn.charlotte.pit.menu.perk.prestige.PrestigeShopMenu;
import cn.charlotte.pit.util.item.ItemBuilder;
import cn.charlotte.pit.util.menu.Button;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

/**
 * @Author: Starry_Killer
 * @Created_In: 2024/4/13
 */
public class BackToPrestigeShopButton extends Button {
    @Override
    public ItemStack getButtonItem(Player player) {
        return new ItemBuilder(Material.ARROW)
                .name("&a返回")
                .lore("&7点击回到声望商店.")
                .build();
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType, int hotbarButton, ItemStack currentItem) {
        new PrestigeShopMenu().openMenu(player);
    }


}
