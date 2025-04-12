package cn.charlotte.pit.perk;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: Starry_Killer
 * @Created_In: 2024/4/13
 */
@Getter
@AllArgsConstructor
public enum ShopPerkType {

    UPGRADE("升级"),
    BUFF("天赋"),
    SHOP("商店物品"),
    STREAK("连杀");

    private final String displayName;
}
