package cn.charlotte.pit.enchantment.type.addon;

import cn.charlotte.pit.enchantment.AbstractEnchantment;
import cn.charlotte.pit.enchantment.param.item.ArmorOnly;
import cn.charlotte.pit.enchantment.rarity.EnchantmentRarity;
import cn.charlotte.pit.parm.listener.IPlayerDamaged;
import cn.charlotte.pit.util.cooldown.Cooldown;
import com.google.common.util.concurrent.AtomicDouble;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import real.nanoneko.register.IMagicLicense;

import java.util.concurrent.atomic.AtomicBoolean;

@ArmorOnly
public class PrickEnchant extends AbstractEnchantment implements IPlayerDamaged, IMagicLicense {
    public String getEnchantName() {
        return "荆棘";
    }

    public int getMaxEnchantLevel() {
        return 3;
    }

    public String getNbtName() {
        return "prick";
    }

    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.NORMAL;
    }

    public Cooldown getCooldown() {
        return null;
    }

    public String getUsefulnessLore(int enchantLevel) {
        return "&7受击时将对攻击者造成 &c" + enchantLevel * 0.25 + "❤ &7的&c必中&7伤害";
    }

    @Override
    public void handlePlayerDamaged(int enchantLevel, Player player, Entity attacker, double damage, AtomicDouble boostDamage, AtomicDouble reduceDamage, AtomicBoolean cancel) {
        if (attacker instanceof Player && ((Player) attacker).getHealth() >= enchantLevel * 0.5) {
            ((Player) attacker).setHealth(((Player) attacker).getHealth() - (enchantLevel * 0.5));
        } else if (attacker instanceof Player) {
            ((Player) attacker).damage(((Player) attacker).getHealth() * 100);
        }
    }
}
