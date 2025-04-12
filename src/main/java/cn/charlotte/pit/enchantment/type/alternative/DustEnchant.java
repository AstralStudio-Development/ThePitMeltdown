package cn.charlotte.pit.enchantment.type.alternative;

import cn.charlotte.pit.enchantment.AbstractEnchantment;
import cn.charlotte.pit.enchantment.IActionDisplayEnchant;
import cn.charlotte.pit.enchantment.param.event.PlayerOnly;
import cn.charlotte.pit.enchantment.param.item.ArmorOnly;
import cn.charlotte.pit.enchantment.rarity.EnchantmentRarity;
import cn.charlotte.pit.parm.listener.IPlayerDamaged;
import cn.charlotte.pit.util.cooldown.Cooldown;
import com.google.common.util.concurrent.AtomicDouble;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author: Starry_Killer
 * @Created_In: 2024/8/18 16:45
 */
@ArmorOnly
public class DustEnchant extends AbstractEnchantment implements IPlayerDamaged, IActionDisplayEnchant {

    public static final HashMap<UUID, Cooldown> cooldown = new HashMap<>();

    @Override
    public String getEnchantName() {
        return  "尘埃";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "dust";
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
        int i = 10 + enchantLevel * 10;
        int time = 0;
        switch (enchantLevel) {
            case 1:
                time = 5;
                break;
            case 2:
                time = 4;
                break;
            case 3:
                time = 3;
                break;
        }
        return "&7装备时,在非冷却期间受到的&c伤害&7衰减&9 " + i + "% &7(" + time + "秒冷却)";
    }

    @Override
    @PlayerOnly
    public void handlePlayerDamaged(int enchantLevel, Player myself, Entity attacker, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        double i = (10 + enchantLevel * 10) * 0.1;
        int time = 0;
        switch (enchantLevel) {
            case 1:
                time = 5;
                break;
            case 2:
                time = 4;
                break;
            case 3:
                time = 3;
                break;
        }
        if (cooldown.getOrDefault(myself.getUniqueId(), new Cooldown(0)).hasExpired()) {
            boostDamage.set(boostDamage.get() - i);
            cooldown.put(myself.getUniqueId(), new Cooldown(time, TimeUnit.SECONDS));
        }
    }

    @Override
    public String getText(int level, Player player) {
        return this.getCooldownActionText(cooldown.getOrDefault(player.getUniqueId(), new Cooldown(0)));
    }
}
