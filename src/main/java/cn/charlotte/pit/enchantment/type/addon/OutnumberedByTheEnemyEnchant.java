package cn.charlotte.pit.enchantment.type.addon;

import cn.charlotte.pit.enchantment.AbstractEnchantment;
import cn.charlotte.pit.enchantment.param.item.ArmorOnly;
import cn.charlotte.pit.enchantment.rarity.EnchantmentRarity;
import cn.charlotte.pit.parm.listener.IAttackEntity;
import cn.charlotte.pit.util.PlayerUtil;
import cn.charlotte.pit.util.cooldown.Cooldown;
import com.google.common.util.concurrent.AtomicDouble;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import real.nanoneko.register.IMagicLicense;

import java.util.concurrent.atomic.AtomicBoolean;

@ArmorOnly
public class OutnumberedByTheEnemyEnchant extends AbstractEnchantment implements IAttackEntity, IMagicLicense {

    public String getEnchantName() {
        return "敌众我寡";
    }

    public int getMaxEnchantLevel() {
        return 3;
    }

    public String getNbtName() {
        return "outnumbered_by_the_enemy";
    }

    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.RARE;
    }

    public Cooldown getCooldown() {
        return null;
    }

    public String getUsefulnessLore(int enchantLevel) {
        return "&7攻击时, 自身半径 &f" + (enchantLevel + 2) + " &7格内 /s" +
                "&7每有一名玩家, 自身伤害提升 &c1% &8( 上限&e" + enchantLevel * 12 + "%&7 )";
    }

    public void handleAttackEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        int nearbyPlayers = PlayerUtil.getNearbyPlayers(attacker.getLocation(), 2 + enchantLevel).size();
        if (!(nearbyPlayers >= enchantLevel * 12)) {
            boostDamage.getAndAdd(nearbyPlayers * 0.01);
        } else {
            boostDamage.getAndAdd(enchantLevel * 0.12);
        }
    }
}
