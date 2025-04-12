package cn.charlotte.pit.enchantment.type.addon;

import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.enchantment.AbstractEnchantment;
import cn.charlotte.pit.enchantment.IActionDisplayEnchant;
import cn.charlotte.pit.enchantment.param.item.WeaponOnly;
import cn.charlotte.pit.enchantment.rarity.EnchantmentRarity;
import cn.charlotte.pit.parm.listener.IAttackEntity;
import cn.charlotte.pit.util.PlayerUtil;
import cn.charlotte.pit.util.cooldown.Cooldown;
import cn.charlotte.pit.util.random.RandomUtil;
import com.google.common.util.concurrent.AtomicDouble;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import real.nanoneko.register.IMagicLicense;

import java.util.concurrent.atomic.AtomicBoolean;

@WeaponOnly
public class PharmacistEnchant extends AbstractEnchantment implements IAttackEntity, IActionDisplayEnchant, IMagicLicense {

    public String getEnchantName() {
        return "药剂师";
    }

    public int getMaxEnchantLevel() {
        return 3;
    }

    public String getNbtName() {
        return "pharmacist";
    }

    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.RARE;
    }

    public Cooldown getCooldown() {
        return null;
    }

    public String getUsefulnessLore(int enchantLevel) {
        return "&7每击中目标 &e" + (Hits(enchantLevel) - 1) + " &7下, 随机获得效果: /s" +
                "&b速度 " + EffectLevel(enchantLevel) + " &f(00:03) /s" +
                "&c生命恢复 " + EffectLevel(enchantLevel) + " &f(00:03) /s" +
                "&3抗性提升 " + EffectLevel(enchantLevel) + " &f(00:03)";
    }

    private int Hits(int enchantLevel) {
        switch (enchantLevel) {
            case 1:
                return 9;
            case 2:
                return 8;
            case 3:
                return 7;
            default:
                return enchantLevel;
        }
    }

    private String EffectLevel(int enchantLevel) {
        switch (enchantLevel) {
            case 2:
                return "II";
            case 3:
                return "III";
            default:
                return "I";
        }
    }

    public void handleAttackEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        int hit = 0;

        if (attacker.getItemInHand() != null) hit = PlayerProfile.getPlayerProfileByUuid(attacker.getUniqueId()).getMeleeHit();

        int activeHitValue = Hits(enchantLevel);

        if (hit % activeHitValue == 0) {
            int randomCount = (int) RandomUtil.helpMeToChooseOne(1, 2, 3);
            switch (randomCount) {
                case 3:
                    attacker.removePotionEffect(PotionEffectType.REGENERATION);
                    attacker.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 3, enchantLevel - 1, true, false));
                    break;
                case 2:
                    attacker.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
                    attacker.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 3, enchantLevel - 1, true, false));
                    break;
                case 1:
                    attacker.removePotionEffect(PotionEffectType.SPEED);
                    attacker.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 3, enchantLevel - 1, true, false));
                    break;
            }
        }
    }

    public String getText(int enchantLevel, Player player) {
        if (!PlayerUtil.shouldIgnoreEnchant(player)) return getHitActionText(player, Hits(enchantLevel));

        return "&c&l✘";
    }
}
