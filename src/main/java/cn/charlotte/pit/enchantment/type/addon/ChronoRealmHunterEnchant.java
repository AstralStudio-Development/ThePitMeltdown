package cn.charlotte.pit.enchantment.type.addon;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.util.PlayerUtil;
import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.enchantment.AbstractEnchantment;
import cn.charlotte.pit.enchantment.param.item.ArmorOnly;
import cn.charlotte.pit.enchantment.rarity.EnchantmentRarity;
import cn.charlotte.pit.parm.listener.IAttackEntity;
import cn.charlotte.pit.parm.listener.IPlayerKilledEntity;
import cn.charlotte.pit.parm.listener.ITickTask;
import cn.charlotte.pit.util.cooldown.Cooldown;
import com.google.common.util.concurrent.AtomicDouble;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import real.nanoneko.register.IMagicLicense;

import java.util.concurrent.atomic.AtomicBoolean;

@ArmorOnly
public class ChronoRealmHunterEnchant extends AbstractEnchantment implements ITickTask, IAttackEntity, IPlayerKilledEntity, IMagicLicense {

    public String getEnchantName() {
        return "时域猎者";
    }

    public int getMaxEnchantLevel() {
        return 3;
    }

    public String getNbtName() {
        return "chrono_realm_hunter";
    }

    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.RARE;
    }

    public Cooldown getCooldown() {
        return null;
    }

    public String getUsefulnessLore(int enchantLevel) {
        return "&7穿戴时, 若你当前连杀大于 &c300&7, 时钟开始摆动, 自身将陷入 &b时间的洪流 &7中/s" +
                "&7每秒将受到 &f" + enchantLevel * 0.5 + "❤ &7的伤害/s" +
                "&7同时, 伤害 &c+" + enchantLevel * 20 + "% &7/s" +
                "&7击杀获得的 &6金币 &7和 &b经验 &7提升 &e+" + enchantLevel * 30 + "%";
    }

    public void handleAttackEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(attacker.getUniqueId());
        double Streak = profile.getStreakKills();
        if (Streak >= 300) {
            boostDamage.getAndAdd(enchantLevel * 0.2);
        }
    }

    public void handlePlayerKilled(int enchantLevel, Player killer, Entity target, AtomicDouble coins, AtomicDouble experience) {
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(killer.getUniqueId());
        double Streak = profile.getStreakKills();
        if (Streak >= 300) {
            coins.addAndGet(coins.get() * (0.3 * enchantLevel));
            experience.addAndGet(experience.get() * (0.3 * enchantLevel));
        }
    }

    public void handle(int enchantLevel, Player player) {
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        double Streak = profile.getStreakKills();
        Bukkit.getScheduler().runTaskAsynchronously(ThePit.getInstance(), () -> {
            if (Streak >= 300) {
                new BukkitRunnable() {
                    public void run() {
                        PlayerUtil.damage(player, PlayerUtil.DamageType.TRUE, (double) enchantLevel, false);
                    }
                }.runTaskLater(ThePit.getInstance(), 2L);
                player.playSound(player.getLocation(), "note.hat", 3F, 0.5F);
            }
        });
    }

    public int loopTick(int enchantLevel) {
        return 20;
    }
}
