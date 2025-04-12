package cn.charlotte.pit.enchantment.type.addon;

import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.enchantment.AbstractEnchantment;
import cn.charlotte.pit.enchantment.IActionDisplayEnchant;
import cn.charlotte.pit.enchantment.param.item.ArmorOnly;
import cn.charlotte.pit.enchantment.rarity.EnchantmentRarity;
import cn.charlotte.pit.parm.listener.IAttackEntity;
import cn.charlotte.pit.util.chat.CC;
import cn.charlotte.pit.util.cooldown.Cooldown;
import com.google.common.util.concurrent.AtomicDouble;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import real.nanoneko.register.IMagicLicense;

import java.util.concurrent.atomic.AtomicBoolean;

@ArmorOnly
public class ComboTransferEnchant extends AbstractEnchantment implements IAttackEntity, IActionDisplayEnchant, IMagicLicense {

    public String getEnchantName() {
        return "强力击: 转移";
    }

    public int getMaxEnchantLevel() {
        return 3;
    }

    public String getNbtName() {
        return "combo_transfer";
    }

    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.RARE;
    }

    public Cooldown getCooldown() {
        return null;
    }

    public String getUsefulnessLore(int enchantLevel) {
        return "&7每击中目标 &e" + (Hits(enchantLevel) - 1) + " &7下, &7可将自身的负面效果转移到目标上";
    }

    private int Hits(int enchantLevel) {
        switch (enchantLevel) {
            case 2:
                return 3;
            case 3:
                return 2;
            default:
                return 5;
        }
    }

    @Override
    public void handleAttackEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        Player targetPlayer = (Player) target;
        int hit = 0;

        if (attacker.getItemInHand() != null) hit = PlayerProfile.getPlayerProfileByUuid(attacker.getUniqueId()).getMeleeHit();

        int activeHitValue = Hits(enchantLevel);

        if (hit % activeHitValue == 0) {
            attacker.sendMessage(CC.translate("&d&l转移! &7你已为目标转移了DEBUFF"));
            targetPlayer.sendMessage(CC.translate("&d&l转移! &7目标已为你转移了DEBUFF"));
            transferNegativeEffects(attacker, targetPlayer);
        }
    }

    private void transferNegativeEffects(Player fromPlayer, Player toPlayer) {
        for (PotionEffect effect : fromPlayer.getActivePotionEffects()) {
            if (isNegativeEffect(effect.getType())) {
                toPlayer.addPotionEffect(new PotionEffect(effect.getType(), effect.getDuration(), effect.getAmplifier()));
                fromPlayer.removePotionEffect(effect.getType());
            }
        }
    }

    private boolean isNegativeEffect(PotionEffectType type) {
        return isInPotionEffectType(type);
    }

    public static boolean isInPotionEffectType(PotionEffectType type) {
        return type.equals(PotionEffectType.BLINDNESS) ||
                type.equals(PotionEffectType.CONFUSION) ||
                type.equals(PotionEffectType.HUNGER) ||
                type.equals(PotionEffectType.POISON) ||
                type.equals(PotionEffectType.SLOW) ||
                type.equals(PotionEffectType.SLOW_DIGGING) ||
                type.equals(PotionEffectType.WEAKNESS) ||
                type.equals(PotionEffectType.WITHER) ||
                type.equals(PotionEffectType.HARM);
    }

    public String getText(int enchantLevel, Player player) {
        return getHitActionText(player, Hits(enchantLevel));
    }
}
