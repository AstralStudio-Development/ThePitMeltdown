package cn.charlotte.pit.enchantment.type.addon;

import cn.charlotte.pit.enchantment.AbstractEnchantment;
import cn.charlotte.pit.enchantment.param.item.WeaponOnly;
import cn.charlotte.pit.enchantment.rarity.EnchantmentRarity;
import cn.charlotte.pit.parm.listener.IAttackEntity;
import cn.charlotte.pit.util.cooldown.Cooldown;
import com.google.common.util.concurrent.AtomicDouble;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import real.nanoneko.register.IMagicLicense;

import java.util.concurrent.atomic.AtomicBoolean;

@WeaponOnly
public class DesperationEnchant extends AbstractEnchantment implements IAttackEntity, IMagicLicense {
    public String getEnchantName() {
        return "破败";
    }

    public int getMaxEnchantLevel() {
        return 3;
    }

    public String getNbtName() {
        return "desperation";
    }

    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.NORMAL;
    }

    public Cooldown getCooldown() {
        return null;
    }

    public String getUsefulnessLore(int enchantLevel) {
        return "&7攻击目标时将额外造成目标当前生命值的 &c" + (enchantLevel >= 3 ? (enchantLevel * 3) + 1 : enchantLevel * 3) + "% &7的伤害";
    }

    public void handleAttackEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        Player targetPlayer = (Player) target;
        targetPlayer.setHealth(targetPlayer.getHealth() - (targetPlayer.getHealth() * ((enchantLevel >= 3 ? (enchantLevel * 3) + 1 : enchantLevel * 3) * 0.01)));
    }
}
