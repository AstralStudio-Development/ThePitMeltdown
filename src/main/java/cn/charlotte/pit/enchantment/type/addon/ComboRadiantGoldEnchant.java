package cn.charlotte.pit.enchantment.type.addon;

import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.enchantment.AbstractEnchantment;
import cn.charlotte.pit.enchantment.IActionDisplayEnchant;
import cn.charlotte.pit.enchantment.param.item.ArmorOnly;
import cn.charlotte.pit.enchantment.rarity.EnchantmentRarity;
import cn.charlotte.pit.parm.listener.IAttackEntity;
import cn.charlotte.pit.util.PlayerUtil;
import cn.charlotte.pit.util.cooldown.Cooldown;
import com.google.common.util.concurrent.AtomicDouble;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import real.nanoneko.register.IMagicLicense;

import java.util.concurrent.atomic.AtomicBoolean;

@ArmorOnly
public class ComboRadiantGoldEnchant extends AbstractEnchantment implements IAttackEntity, IActionDisplayEnchant, IMagicLicense {
    private final float LimitAbsorptionHearts = 30;

    public String getEnchantName() {
        return "强力击: 耀金";
    }

    public int getMaxEnchantLevel() {
        return 1;
    }

    public String getNbtName() {
        return "combo_radiant_gold";
    }

    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.DARK_RARE;
    }

    public Cooldown getCooldown() {
        return null;
    }

    public String getUsefulnessLore(int enchantLevel) {
        return "&7每 &e4 &7次击中目标时, 将立刻恢复 &c1.5❤ &7生命值, 并获得 &66 生命吸收(❤) &7和 &b速度 II &f(00:03)";
    }

    public void handleAttackEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        int hit = 0;

        if (attacker.getItemInHand() != null) hit = PlayerProfile.getPlayerProfileByUuid(attacker.getUniqueId()).getMeleeHit();

        if (hit % 4 == 0) {
            if (attacker.hasPotionEffect(PotionEffectType.SPEED)) attacker.removePotionEffect(PotionEffectType.SPEED);
            attacker.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 1, true, false));

            CraftPlayer nmsPlayer = (CraftPlayer) attacker;
            if (nmsPlayer.getHandle().getAbsorptionHearts() + 12 < LimitAbsorptionHearts) {
                nmsPlayer.getHandle().setAbsorptionHearts(nmsPlayer.getHandle().getAbsorptionHearts() + (float) 12);
            } else {
                nmsPlayer.getHandle().setAbsorptionHearts(LimitAbsorptionHearts);
            }

            PlayerUtil.heal(attacker, 3.0);
        }
    }

    public String getText(int enchantLevel, Player player) {
        return getHitActionText(player, 4);
    }
}
