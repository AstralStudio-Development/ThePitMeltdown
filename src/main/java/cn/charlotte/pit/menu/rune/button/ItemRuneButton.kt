package cn.charlotte.pit.menu.rune.button

import cn.charlotte.pit.menu.rune.SecondChoseRuneMenu
import cn.charlotte.pit.util.menu.Button
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

/**
 * @author iForRiny
 * @since 2025/4/16
 */
class ItemRuneButton(private val item: ItemStack, private val index: Int) : Button() {

    override fun getButtonItem(player: Player): ItemStack {
        return item
    }

    override fun clicked(
        player: Player,
        slot: Int,
        clickType: ClickType,
        hotbarButton: Int,
        currentItem: ItemStack
    ) {
        SecondChoseRuneMenu(item, index).openMenu(player)
    }
}
