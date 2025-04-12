package cn.charlotte.pit.enchantment.type.addon;

// Author ShiroKyu_
// Date 2024/4/25

import cn.charlotte.pit.enchantment.AbstractEnchantment;
import cn.charlotte.pit.enchantment.IActionDisplayEnchant;
import cn.charlotte.pit.enchantment.param.item.WeaponOnly;
import cn.charlotte.pit.enchantment.rarity.EnchantmentRarity;
import cn.charlotte.pit.parm.listener.IAttackEntity;
import cn.charlotte.pit.util.chat.CC;
import cn.charlotte.pit.util.cooldown.Cooldown;
import cn.charlotte.pit.util.time.TimeUtil;
import com.google.common.util.concurrent.AtomicDouble;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import real.nanoneko.register.IMagicLicense;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@WeaponOnly
public class SilentEnchant extends AbstractEnchantment implements IAttackEntity , IActionDisplayEnchant, IMagicLicense {
    private static final HashMap<UUID, Cooldown> cooldown = new HashMap<>();
    public static final HashMap<UUID, Boolean> silentIsActive = new HashMap<>();

    public String getEnchantName() {
        return "沉默";
    }

    public int getMaxEnchantLevel() {
        return 3;
    }

    public String getNbtName() {
        return "silent";
    }

    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.RARE;
    }

    public Cooldown getCooldown() {
        return null;
    }

    private String getUseEffectSuffix(int enchantLevel) {
        switch (enchantLevel) {
            case 2:
                return "IV";
            case 3:
                return "V";
            default:
                return "III";
        }
    }

    private int getCooldownSeconds(int enchantLevel) {
        switch (enchantLevel) {
            case 2:
                return 18;
            case 3:
                return 16;
            default:
                return 20;
        }
    }

    public String getUsefulnessLore(int enchantLevel) {
        String EffectSuffix = getUseEffectSuffix(enchantLevel);
        int ACooldown = getCooldownSeconds(enchantLevel);
        return "&7攻击玩家时可对其施加 &2沉默 " + EffectSuffix +" &f(00:03) &7效果 &8( " + ACooldown + "s 冷却 )" +
                "/s" + "&f- &2沉默: &7对玩家造成 &c失明 &7与 &c虚弱 &7效果";
    }

    public void handleAttackEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        if (cooldown.getOrDefault(attacker.getUniqueId(), new Cooldown(0L)).hasExpired() && !silentIsActive.getOrDefault(attacker.getUniqueId(), false)) {
            silentIsActive.put(attacker.getUniqueId(), true);
        }

        if (cooldown.getOrDefault(attacker.getUniqueId(), new Cooldown(0L)).hasExpired() && silentIsActive.getOrDefault(attacker.getUniqueId(), false)) {
            cooldown.put(attacker.getUniqueId(), new Cooldown(getCooldownSeconds(enchantLevel), TimeUnit.SECONDS));
            Player targetPlayer = (Player) target;

            targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, (enchantLevel + 1), true, false));
            targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, (enchantLevel + 1), true, false));

            targetPlayer.sendMessage(CC.translate("&2沉默! &7你被施加了 &c失明 &7与 &c虚弱 &7效果"));
            silentIsActive.put(attacker.getUniqueId(), false);
        }
    }
    public String getText(int level, Player player) {
        return (cooldown.getOrDefault(player.getUniqueId(), new Cooldown(0L))).hasExpired() ? "&a&l✔" : "&c&l" + TimeUtil.millisToRoundedTime((cooldown.get(player.getUniqueId())).getRemaining()).replace(" ", "");
    }
}
