package cn.charlotte.pit.enchantment.type.alternative;

import cn.charlotte.pit.enchantment.AbstractEnchantment;
import cn.charlotte.pit.enchantment.param.item.WeaponOnly;
import cn.charlotte.pit.enchantment.rarity.EnchantmentRarity;
import cn.charlotte.pit.parm.listener.IAttackEntity;
import cn.charlotte.pit.util.chat.CC;
import cn.charlotte.pit.util.cooldown.Cooldown;
import cn.charlotte.pit.util.random.RandomUtil;
import com.google.common.util.concurrent.AtomicDouble;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author: Starry_Killer
 * @Created_In: 2024/1/3 18:50
 */
@WeaponOnly
public class ComboBacktrackEnchant extends AbstractEnchantment implements IAttackEntity {

    @Override
    public String getEnchantName() {
        return "强力击: 回溯";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "combo_backtrack_enchant";
    }

    @Override
    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.RARE;
    }

    @Override
    public Cooldown getCooldown() {
        return null;
    }

    @Override
    public String getUsefulnessLore(int enchantLevel) {
        return "&7攻击玩家时若自身血量低于 &c4❤ &7则有 &9" + enchantLevel * 10 + "% &7的概率回溯至满血状态";
    }

    @Override
    public void handleAttackEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        double attackerHealth = attacker.getHealth();
        if (attackerHealth < 8.0 && RandomUtil.hasSuccessfullyByChance(enchantLevel * 0.1)) {
            attacker.setHealth(attacker.getMaxHealth());
            attacker.sendMessage(CC.translate("&c&l回溯! &7你已恢复至满血状态!"));
        }
    }

}
