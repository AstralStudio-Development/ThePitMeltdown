package cn.charlotte.pit.enchantment.type.addon;

import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.enchantment.AbstractEnchantment;
import cn.charlotte.pit.enchantment.IActionDisplayEnchant;
import cn.charlotte.pit.enchantment.param.item.ArmorOnly;
import cn.charlotte.pit.enchantment.rarity.EnchantmentRarity;
import cn.charlotte.pit.parm.listener.IAttackEntity;
import cn.charlotte.pit.util.cooldown.Cooldown;
import com.google.common.util.concurrent.AtomicDouble;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import real.nanoneko.register.IMagicLicense;

import java.util.concurrent.atomic.AtomicBoolean;

@ArmorOnly
public class ComboChaosEnchant extends AbstractEnchantment implements IAttackEntity, IActionDisplayEnchant, IMagicLicense {

    public String getEnchantName() {
        return "强力击: 混沌";
    }

    public int getMaxEnchantLevel() {
        return 1;
    }

    public String getNbtName() {
        return "combo_chaos";
    }

    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.DARK_NORMAL;
    }

    public Cooldown getCooldown() {
        return null;
    }

    public String getUsefulnessLore(int enchantLevel) {
        return "&7每 &e4 &7次击中玩家时, 你的下一次攻击会附带 &c失明 I &8(00:03) &7和 &c虚弱 III &8(00:03)";
    }

    public void handleAttackEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        Player targetPlayer = (Player) target;

        int hit = 0;

        if (attacker.getItemInHand() != null) hit = PlayerProfile.getPlayerProfileByUuid(attacker.getUniqueId()).getMeleeHit();

        if (hit % 4 == 0) {
            targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 2, true, false));
            targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0, true, false));
        }
    }

    public String getText(int enchantLevel, Player player) {
        return getHitActionText(player, 4);
    }
}
