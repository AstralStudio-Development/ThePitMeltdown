package cn.charlotte.pit.enchantment.type.addon;

import cn.charlotte.pit.ThePit;
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
import org.bukkit.scheduler.BukkitRunnable;
import real.nanoneko.register.IMagicLicense;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@WeaponOnly
public class SevereInjuryEnchant extends AbstractEnchantment implements IAttackEntity, IActionDisplayEnchant, IMagicLicense {
    private static final HashMap<UUID, Cooldown> cooldown = new HashMap<>();
    public static final HashMap<UUID, Boolean> severeInjuryIsActive = new HashMap<>();

    public String getEnchantName() {
        return "重伤";
    }

    public int getMaxEnchantLevel() {
        return 3;
    }

    public String getNbtName() {
        return "severeInjury";
    }

    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.RARE;
    }

    public Cooldown getCooldown() {
        return null;
    }

    private int getCooldownSeconds(int enchantLevel) {
        switch (enchantLevel) {
            case 2:
                return 13;
            case 3:
                return 10;
            default:
                return 15;
        }
    }

    public String getUsefulnessLore(int enchantLevel) {
        return "&7攻击玩家后, 将对玩家造成 &c重伤 &8(00:04) &7效果 &8(" + getCooldownSeconds(enchantLevel) + "s冷却 )"
                + "/s" + "&f- &c重伤&7: 使玩家每秒减少 &c1.0❤ &7并且被施加 &2缓慢 II &7效果";
    }

    public void handleAttackEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        Player targetPlayer = (Player) target;
        if (cooldown.getOrDefault(attacker.getUniqueId(), new Cooldown(0L)).hasExpired()) {
            cooldown.put(attacker.getUniqueId(), new Cooldown(getCooldownSeconds(enchantLevel), TimeUnit.SECONDS));
            targetPlayer.sendMessage(CC.translate("&c&l重伤! &7你在 &e4s &7内会持续 &c流血 &7并被施加 &2缓慢 II"));
            severeInjuryIsActive.put(targetPlayer.getUniqueId(), true);
            interdictionEffect(targetPlayer);
        }
    }

    public void interdictionEffect(Player targetPlayer) {
        if (severeInjuryIsActive.getOrDefault(targetPlayer.getUniqueId(), false)) {
            targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 120, 1, true, false));
            BukkitRunnable task = new BukkitRunnable() {
                public void run() {
                    if (severeInjuryIsActive.getOrDefault(targetPlayer.getUniqueId(), false)) {
                        targetPlayer.damage(2);
                        if (targetPlayer.isDead()) {
                            this.cancel();
                        }
                    }
                }
            };

            task.runTaskTimer(ThePit.getInstance(), 20L, 20L);

            BukkitRunnable taskCancel = new BukkitRunnable() {
                @Override
                public void run() {
                    task.cancel();
                    if (severeInjuryIsActive.getOrDefault(targetPlayer.getUniqueId(), false)) {
                        targetPlayer.sendMessage(CC.translate("&c流血停止了..."));
                        severeInjuryIsActive.put(targetPlayer.getUniqueId(), false);
                    }
                }
            };

            taskCancel.runTaskLater(ThePit.getInstance(), 20 * 4L);
        }
    }


    public String getText(int level, Player attacker) {
        return (cooldown.getOrDefault(attacker.getUniqueId(), new Cooldown(0L))).hasExpired() ? "&a&l✔" : "&c&l" + TimeUtil.millisToRoundedTime((cooldown.get(attacker.getUniqueId())).getRemaining()).replace(" ", "");
    }
}
