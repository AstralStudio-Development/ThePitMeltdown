package cn.charlotte.pit.enchantment.type.addon;

import cn.charlotte.pit.enchantment.AbstractEnchantment;
import cn.charlotte.pit.enchantment.param.item.WeaponOnly;
import cn.charlotte.pit.enchantment.rarity.EnchantmentRarity;
import cn.charlotte.pit.parm.listener.IAttackEntity;
import cn.charlotte.pit.util.PlayerUtil;
import cn.charlotte.pit.util.cooldown.Cooldown;
import com.google.common.util.concurrent.AtomicDouble;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import real.nanoneko.register.IMagicLicense;

import java.util.concurrent.atomic.AtomicBoolean;

@WeaponOnly
public class CriticalEnchant extends AbstractEnchantment implements IAttackEntity, IMagicLicense {

    public String getEnchantName() {
        return "暴伤";
    }

    public int getMaxEnchantLevel() {
        return 3;
    }

    public String getNbtName() {
        return "critical";
    }

    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.NORMAL;
    }

    public Cooldown getCooldown() {
        return null;
    }

    public String getUsefulnessLore(int enchantLevel) {
        return "&7攻击目标造成暴击时, 将会额外增加 &c" + enchantLevel * 15 + "% &7的伤害";
    }

    public void handleAttackEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        if (PlayerUtil.isCritical(attacker)) {
            boostDamage.getAndAdd(enchantLevel * 0.15);
        }
    }
}
