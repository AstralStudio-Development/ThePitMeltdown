package cn.charlotte.pit.enchantment.type.addon;

import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.enchantment.AbstractEnchantment;
import cn.charlotte.pit.enchantment.param.item.ArmorOnly;
import cn.charlotte.pit.enchantment.rarity.EnchantmentRarity;
import cn.charlotte.pit.parm.listener.IAttackEntity;
import cn.charlotte.pit.util.cooldown.Cooldown;
import com.google.common.util.concurrent.AtomicDouble;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import real.nanoneko.register.IMagicLicense;

import java.util.concurrent.atomic.AtomicBoolean;

// 此附魔首次以ThePitAddon的形式出现, 后进行重写
// Original: Starry_Killer
// 二次修改: Irina/pi_ka
@ArmorOnly
public class CoinGloriousEnchant extends AbstractEnchantment implements IAttackEntity, IMagicLicense {

    public String getEnchantName() {
        return "金碧辉煌";
    }

    public int getMaxEnchantLevel() {
        return 3;
    }

    public String getNbtName() {
        return "coin_glorious";
    }

    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.RARE;
    }

    public Cooldown getCooldown() {
        return null;
    }

    public String getUsefulnessLore(int enchantLevel) {
        return "&7每拥有 &620000硬币 &7增加 &c1% &7的伤害 &8( 可叠加,最高 &e" + enchantLevel * 13 + "% &8)";
    }

    public void handleAttackEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(attacker.getUniqueId());
        long coins = (long) profile.getCoins();

        double damageIncrease = (double) coins / 20000;

        if (damageIncrease <= (double)enchantLevel * 13 && damageIncrease >= 1) {
            boostDamage.getAndAdd(0.01 * damageIncrease);
            attacker.sendMessage("§6§l金碧辉煌! §7增加 §c" + (int) damageIncrease + "% §7伤害");
        } else if (damageIncrease > (double)enchantLevel * 13) {
            boostDamage.getAndAdd((double) (enchantLevel * 13) / 100);
            attacker.sendMessage("§6§l金碧辉煌! §7增加 §c" + enchantLevel * 10 + "% §7伤害");
        }
    }
}
