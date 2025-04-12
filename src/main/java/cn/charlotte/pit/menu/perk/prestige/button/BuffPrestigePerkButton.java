package cn.charlotte.pit.menu.perk.prestige.button;

import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.menu.perk.prestige.PrestigeBuffPerkBuyMenu;
import cn.charlotte.pit.util.item.ItemBuilder;
import cn.charlotte.pit.util.menu.Button;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: Starry_Killer
 * @Created_In: 2024/4/13
 */
public class BuffPrestigePerkButton extends Button {
    @Override
    public ItemStack getButtonItem(Player player) {
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        List<String> lore = new ArrayList<>();
        lore.add("&7增加可在天赋NPC处购买的新天赋。");
        lore.add(" ");
        lore.add("&7你的声望: &e" + profile.getRenown() + " 声望");
        lore.add(" ");
        lore.add("&e点击浏览！");
        return new ItemBuilder(Material.DIAMOND)
                .name("&b天赋")
                .lore(lore)
                .build();
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType, int hotbarButton, ItemStack currentItem) {
        new PrestigeBuffPerkBuyMenu().openMenu(player);
    }


}
