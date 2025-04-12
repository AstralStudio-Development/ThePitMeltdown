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

@ArmorOnly
public class ProgressiveAdvancementEnchant extends AbstractEnchantment implements IAttackEntity, IMagicLicense {
    public ProgressiveAdvancementEnchant() {
    }

    public String getEnchantName() {
        return "层层递进";
    }

    public int getMaxEnchantLevel() {
        return 3;
    }

    public String getNbtName() {
        return "progressive_advancement";
    }

    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.RARE;
    }

    public Cooldown getCooldown() {
        return null;
    }

    public String getUsefulnessLore(int enchantLevel) {
        return "&7每拥有 &c50连杀 &7增加 &c1% &7的伤害 &8( 可叠加,最高 &e" + enchantLevel * 10+ "% &8)";
    }

    public void handleAttackEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(attacker.getUniqueId());
        double Streak = profile.getStreakKills();

        double damageIncrease = Streak / 50;

        if (damageIncrease <= (double)enchantLevel * 10 && damageIncrease >= 1) {
            boostDamage.getAndAdd(0.01 * damageIncrease);
            attacker.sendMessage("§c§l层层递进! §7增加 §c" + (int) damageIncrease + "% §7伤害");
        } else if (damageIncrease > (double)enchantLevel * 10) {
            boostDamage.getAndAdd((double) (enchantLevel * 10) / 100);
            attacker.sendMessage("§c§l层层递进! §7增加 §c" + enchantLevel * 5 + "% §7伤害");
        }
    }
}
