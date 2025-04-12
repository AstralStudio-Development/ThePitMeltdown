package cn.charlotte.pit.enchantment.type.addon;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.enchantment.AbstractEnchantment;
import cn.charlotte.pit.enchantment.IActionDisplayEnchant;
import cn.charlotte.pit.enchantment.param.item.WeaponOnly;
import cn.charlotte.pit.enchantment.rarity.EnchantmentRarity;
import cn.charlotte.pit.parm.listener.IAttackEntity;
import cn.charlotte.pit.util.PlayerUtil;
import cn.charlotte.pit.util.cooldown.Cooldown;
import com.google.common.util.concurrent.AtomicDouble;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import real.nanoneko.register.IMagicLicense;

import java.util.concurrent.atomic.AtomicBoolean;

@WeaponOnly
public class ComboParasiteEnchant extends AbstractEnchantment implements IAttackEntity, IActionDisplayEnchant, IMagicLicense {

    public String getEnchantName() {
        return "强力击: 寄生";
    }

    public int getMaxEnchantLevel() {
        return 3;
    }

    public String getNbtName() {
        return "combo_parasite";
    }

    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.RARE;
    }

    public Cooldown getCooldown() {
        return null;
    }

    public String getUsefulnessLore(int enchantLevel) {
        return "&7每击中目标 &e" + Hits(enchantLevel) + " &7下, &7将给予对方 &2寄生 &8(00:02) &7效果" +
                "/s" + "&f- &2寄生&f: &7给予 &c缓慢 &7效果, 并每秒扣除 &c" + enchantLevel * 5 + "%❤" +
                "/s" + "&7并将扣除血量的 &b50% &7转化给自身所有";
    }

    private int Hits(int enchantLevel) {
        switch (enchantLevel) {
            case 2:
                return 5;
            case 3:
                return 4;
            default:
                return 6;
        }
    }

    @WeaponOnly
    public void handleAttackEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        Player targetPlayer = (Player) target;
        int hit = 0;

        if (attacker.getItemInHand() != null) hit = PlayerProfile.getPlayerProfileByUuid(attacker.getUniqueId()).getMeleeHit();

        int activeHitValue = Hits(enchantLevel);

        if (hit % activeHitValue == 0) {
            targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 0, true, false));
            BukkitRunnable task = new BukkitRunnable() {
                @Override
                public void run() {
                    double Health = targetPlayer.getHealth() * (enchantLevel * 0.05);
                    PlayerUtil.damage(attacker, targetPlayer, PlayerUtil.DamageType.NORMAL, Health, true);
                    if (attacker.getHealth() + Health < attacker.getMaxHealth()) attacker.setHealth(attacker.getHealth() + Health);
                }
            };

            task.runTaskTimer(ThePit.getInstance(), 20L, 20L);

            new BukkitRunnable() {
                @Override
                public void run() {
                    task.cancel();
                }
            }.runTaskLater(ThePit.getInstance(), 40L);
        }
    }

    public String getText(int enchantLevel, Player player) {
        return getHitActionText(player, Hits(enchantLevel));
    }
}
