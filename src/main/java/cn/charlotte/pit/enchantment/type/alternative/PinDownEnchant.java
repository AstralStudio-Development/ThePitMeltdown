package cn.charlotte.pit.enchantment.type.alternative;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.buff.impl.PinDownDeBuff;
import cn.charlotte.pit.enchantment.AbstractEnchantment;
import cn.charlotte.pit.enchantment.param.item.BowOnly;
import cn.charlotte.pit.enchantment.rarity.EnchantmentRarity;
import cn.charlotte.pit.parm.AutoRegister;
import cn.charlotte.pit.parm.listener.IPlayerShootEntity;
import cn.charlotte.pit.util.PlayerUtil;
import cn.charlotte.pit.util.chat.CC;
import cn.charlotte.pit.util.cooldown.Cooldown;
import cn.charlotte.pit.util.time.TimeUtil;
import com.google.common.util.concurrent.AtomicDouble;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author: Starry_Killer
 * @Created_In: 2024/3/26 17:33
 * @Mod_By: Irina
 */
@BowOnly
public class PinDownEnchant extends AbstractEnchantment implements Listener, IPlayerShootEntity {

    private static final HashMap<UUID, Cooldown> cooldown = new HashMap<>();

    public String getEnchantName() {
        return "阻滞战术";
    }

    public int getMaxEnchantLevel() {
        return 3;
    }

    public String getNbtName() {
        return "interdiction";
    }

    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.NORMAL;
    }

    public Cooldown getCooldown() {
        return null;
    }

    public String getUsefulnessLore(int enchantLevel) {
        return "&7射出的箭矢命中玩家时将对玩家造成 &2阻滞 &8(00:0" + (2 + (enchantLevel * 2)) + ") &7效果 (6s冷却) /s" +
                "&7效果 &2阻滞&7: 无法被施加 &b速度 &7和 &a跳跃提升 &7效果";
    }

    public void handleShootEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        if (cooldown.getOrDefault(attacker.getUniqueId(), new Cooldown(0L)).hasExpired()) {
            Player targetPlayer = (Player) target;
            cooldown.put(attacker.getUniqueId(), new Cooldown(6L, TimeUnit.SECONDS));
            targetPlayer.sendMessage(CC.translate("&2&l阻滞! &7你在 &e" + (2 + (enchantLevel * 2)) + "s &7内无法被施加 &b速度 &7和 &a跳跃 &7效果!"));
            interdictionEffect(targetPlayer, enchantLevel);
        }
    }

    private void interdictionEffect(Player targetPlayer, int enchantLevel) {
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                targetPlayer.removePotionEffect(PotionEffectType.SPEED);
                targetPlayer.removePotionEffect(PotionEffectType.JUMP);
            }
        };

        task.runTaskTimer(ThePit.getInstance(), 5L, 5L);

        new BukkitRunnable() {
            @Override
            public void run() {
                task.cancel();
            }
        }.runTaskLater(ThePit.getInstance(), (2 + (enchantLevel * 2L)) * 20L);
    }

    public String getText(int level, Player attacker) {
        return (cooldown.getOrDefault(attacker.getUniqueId(), new Cooldown(0L))).hasExpired() ? "&a&l✔" : "&c&l" + TimeUtil.millisToRoundedTime((cooldown.get(attacker.getUniqueId())).getRemaining()).replace(" ", "");
    }
}
