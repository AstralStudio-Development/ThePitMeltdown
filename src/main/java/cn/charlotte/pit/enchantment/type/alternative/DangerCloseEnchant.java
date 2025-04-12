package cn.charlotte.pit.enchantment.type.alternative;

import cn.charlotte.pit.enchantment.AbstractEnchantment;
import cn.charlotte.pit.enchantment.IActionDisplayEnchant;
import cn.charlotte.pit.enchantment.param.item.ArmorOnly;
import cn.charlotte.pit.enchantment.rarity.EnchantmentRarity;
import cn.charlotte.pit.parm.listener.ITickTask;
import cn.charlotte.pit.util.PlayerUtil;
import cn.charlotte.pit.util.cooldown.Cooldown;
import cn.charlotte.pit.util.thread.ThreadHelper;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Starry_Killer
 * @Created_In: 2024/4/14
 */
@ArmorOnly
public class DangerCloseEnchant extends AbstractEnchantment implements IActionDisplayEnchant, ITickTask, ThreadHelper {

    public static final HashMap<UUID, Cooldown> cooldown = new HashMap<>();

    @Override
    public String getEnchantName() {
        return  "危险将至";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "danger_close";
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
        int time = 0;
        int left = 0;
        switch (enchantLevel) {
            case 1:
                time = 3;
                left = 3;
                break;
            case 2:
                time = 6;
                left = 4;
                break;
            case 3:
                time = 9;
                left = 4;
                break;
        }
        return "&7剩余 &c" + left + "❤ &7时获得 &b速度 III &f(00:0" + time + ") &7(10秒冷却)";
    }

    @Override
    public String getText(int level, Player player) {
        return this.getCooldownActionText(cooldown.getOrDefault(player.getUniqueId(), new Cooldown(0)));
    }

    @Override
    public void handle(int enchantLevel, Player player) {
        int time = 0;
        int left = 0;
        switch (enchantLevel) {
            case 1:
                left = 3;
                break;
            case 2:
                left = 4;
                break;
            case 3:
                left = 4;
                break;
        }
        if (cooldown.getOrDefault(player.getUniqueId(), new Cooldown(0)).hasExpired()) {
            if (player.getHealth() <= left * 2) {
                player.removePotionEffect(PotionEffectType.SPEED);
                sync(() -> {
                    PlayerUtil.addPotionEffect(player, new PotionEffect(PotionEffectType.SPEED, 20 * 3 * enchantLevel, 2,true), true);
                });
                cooldown.put(player.getUniqueId(), new Cooldown(10, TimeUnit.SECONDS));
            }
        }
    }

    @Override
    public int loopTick(int enchantLevel) {
        return 20;
    }
}
