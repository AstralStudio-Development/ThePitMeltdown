package cn.charlotte.pit.menu.option.button;

import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.util.PlayerUtil;
import cn.charlotte.pit.util.item.ItemBuilder;
import cn.charlotte.pit.util.menu.Button;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: Misoryan
 * @Created_In: 2021/1/14 15:20
 */
public class EnderChestVisibilityButton extends Button {
    @Override
    public ItemStack getButtonItem(Player player) {
        List<String> lines = new ArrayList<>();
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        if (!profile.isSupporter() && !PlayerUtil.isStaff(player)) {
            profile.getPlayerOption().setEnderChestVisibility(true);
        }
        lines.add("&7允许普通玩家通过档案系统查看你的末影箱.");
        lines.add("&7你也可以使用 /view 查看其他玩家的档案.");
        lines.add(" ");
        lines.add("&7当前: " + (profile.getPlayerOption().isEnderChestVisibility() ? "&a允许" : "&c不允许"));
        lines.add((profile.isSupporter() || PlayerUtil.isStaff(player) ? "&e&e点击切换此选项!" : "&c需要&e天坑乱斗会员"));
        return new ItemBuilder(Material.ENDER_CHEST).name("&a隐私选项: 末影箱").lore(lines).build();
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType, int hotbarButton, ItemStack currentItem) {
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        if (!profile.isSupporter() && !PlayerUtil.isStaff(player)) {
            return;
        }
        profile.getPlayerOption().setEnderChestVisibility(!profile.getPlayerOption().isEnderChestVisibility());
    }

    @Override
    public boolean shouldUpdate(Player player, int slot, ClickType clickType) {
        return true;
    }
}
