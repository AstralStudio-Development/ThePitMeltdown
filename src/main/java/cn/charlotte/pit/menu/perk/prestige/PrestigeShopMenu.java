package cn.charlotte.pit.menu.perk.prestige;

import cn.charlotte.pit.menu.perk.prestige.button.*;
import cn.charlotte.pit.util.menu.Button;
import cn.charlotte.pit.util.menu.Menu;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: Starry_Killer
 * @Created_In: 2024/4/13 11:31
 */
public class PrestigeShopMenu extends Menu {


    @Override
    public String getTitle(Player player) {
        return "声望商店";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> button = new HashMap<>();

        button.put(10, new UpgradePrestigePerkButton());
        button.put(12, new BuffPrestigePerkButton());
        button.put(14, new ShopPrestigePerkButton());
        button.put(16, new StreakPrestigePerkButton());

        button.put(22, new BackToPrestigeMainButton());
        return button;
    }

    @Override
    public int getSize() {
        return 3 * 9;
    }

    @Override
    public boolean isAutoUpdate() {
        return true;
    }
}
