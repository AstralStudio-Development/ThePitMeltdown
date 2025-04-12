package cn.charlotte.pit.enchantment.type.normal;

import cn.charlotte.pit.enchantment.AbstractEnchantment;
import cn.charlotte.pit.enchantment.IActionDisplayEnchant;
import cn.charlotte.pit.enchantment.param.item.ArmorOnly;
import cn.charlotte.pit.enchantment.rarity.EnchantmentRarity;
import cn.charlotte.pit.parm.listener.IPlayerDamaged;
import cn.charlotte.pit.util.PlayerUtil;
import cn.charlotte.pit.util.cooldown.Cooldown;
import com.google.common.util.concurrent.AtomicDouble;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author: Misoryan
 * @Remake: Starry_Killer
 * @Created_In: 2021/1/25 22:19
 * @Remaked_In: 2024/3/26 21:32
 */
@ArmorOnly
public class PeroxideEnchant extends AbstractEnchantment implements IPlayerDamaged, IActionDisplayEnchant {

    public static final HashMap<UUID, Cooldown> cooldown = new HashMap<>();

    @Override
    public String getEnchantName() {
        return "过氧化物";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "Peroxide";
    }

    @Override
    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.NORMAL;
    }

    @Override
    public Cooldown getCooldown() {
        return null;
    }

    @Override
    public String getUsefulnessLore(int enchantLevel) {
        double time = 0;
        switch (enchantLevel) {
            case 1:
                time = 0.5;
                break;
            case 2:
                time = 0.7;
                break;
            case 3:
                time = 1.0;
                break;
        }
        return "&7受到攻击时恢复自身 &c" + time + "❤ &7生命值 &7(1.5秒冷却)";
    }

    @Override
    public void handlePlayerDamaged(int enchantLevel, Player myself, Entity attacker, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        double time = 0;
        switch (enchantLevel) {
            case 1:
                time = 0.5;
                break;
            case 2:
                time = 0.7;
                break;
            case 3:
                time = 1.0;
                break;
        }
        if (cooldown.getOrDefault(myself.getUniqueId(), new Cooldown(0)).hasExpired()) {
            PlayerUtil.heal(myself, time * 2);
            cooldown.put(myself.getUniqueId(), new Cooldown((long) 1.5, TimeUnit.SECONDS));
        }
    }

    @Override
    public String getText(int level, Player player) {
        return this.getCooldownActionText(cooldown.getOrDefault(player.getUniqueId(), new Cooldown(0)));
    }
}
