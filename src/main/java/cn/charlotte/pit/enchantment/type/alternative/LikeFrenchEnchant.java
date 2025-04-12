package cn.charlotte.pit.enchantment.type.alternative;

import cn.charlotte.pit.enchantment.AbstractEnchantment;
import cn.charlotte.pit.enchantment.param.event.PlayerOnly;
import cn.charlotte.pit.enchantment.param.item.ArmorOnly;
import cn.charlotte.pit.enchantment.rarity.EnchantmentRarity;
import cn.charlotte.pit.parm.listener.IAttackEntity;
import cn.charlotte.pit.parm.listener.IPlayerShootEntity;
import cn.charlotte.pit.util.cooldown.Cooldown;
import cn.charlotte.pit.util.item.ItemUtil;
import com.google.common.util.concurrent.AtomicDouble;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Creator Starry_Killer
 * @Date 2024/7/8 12:33
 */
@ArmorOnly
public class LikeFrenchEnchant extends AbstractEnchantment implements IAttackEntity, IPlayerShootEntity {

    @Override
    public String getEnchantName() {
        return "像法国人一样做";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "like_french";
    }

    @Override
    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.RAGE;
    }

    @Override
    public Cooldown getCooldown() {
        return null;
    }

    @Override
    public String getUsefulnessLore(int enchantLevel) {
        return "&7对佩戴 &6国王的王冠 &7的玩家额外造成 &c" + (enchantLevel * 0.5) + "❤ &7的伤害";
    }

    @Override
    @PlayerOnly
    public void handleAttackEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        Player targetPlayer = (Player) target;
        if (targetPlayer.getInventory().getHelmet() != null && "kings_helmet".equals(ItemUtil.getInternalName(targetPlayer.getInventory().getHelmet()))) {
            boostDamage.getAndAdd(enchantLevel * 0.1);
        }
    }

    @Override
    @PlayerOnly
    public void handleShootEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        Player targetPlayer = (Player) target;
        if (targetPlayer.getInventory().getHelmet() != null && "kings_helmet".equals(ItemUtil.getInternalName(targetPlayer.getInventory().getHelmet()))) {
            boostDamage.getAndAdd(enchantLevel * 0.1);
        }
    }
}
