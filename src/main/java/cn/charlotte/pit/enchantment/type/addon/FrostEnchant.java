package cn.charlotte.pit.enchantment.type.addon;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.util.PlayerUtil;
import cn.charlotte.pit.enchantment.AbstractEnchantment;
import cn.charlotte.pit.enchantment.IActionDisplayEnchant;
import cn.charlotte.pit.enchantment.param.item.WeaponOnly;
import cn.charlotte.pit.enchantment.rarity.EnchantmentRarity;
import cn.charlotte.pit.parm.listener.IPlayerDamaged;
import cn.charlotte.pit.util.chat.CC;
import cn.charlotte.pit.util.cooldown.Cooldown;
import com.google.common.util.concurrent.AtomicDouble;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import real.nanoneko.register.IMagicLicense;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@WeaponOnly
public class FrostEnchant extends AbstractEnchantment implements IPlayerDamaged, IActionDisplayEnchant, IMagicLicense {
    private static final HashMap<UUID, Integer> hasBlocks = new HashMap<>();

    public String getEnchantName() {
        return "寒霜";
    }

    public int getMaxEnchantLevel() {
        return 3;
    }

    public String getNbtName() {
        return "frost";
    }

    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.RARE;
    }

    public Cooldown getCooldown() {
        return null;
    }

    public String getUsefulnessLore(int enchantLevel) {
        return "&7每当格挡 &e" + onBlocks(enchantLevel) + " &7次时 /s" +
                "&7将会为攻击者施加 &b寒霜侵袭 &f(00:04) &7效果 /s" +
                "&7&b寒霜侵袭&f: &7降低目标的 &b" + enchantLevel * 15 + "% &7移速 /s" +
                "&7并且每秒对目标造成 &c0.5❤ &7的&c必中&7伤害";
    }

    private int onBlocks(int enchantLevel) {
        return enchantLevel >= 3 ? 1 : 6 - enchantLevel;
    }

    @Override
    public void handlePlayerDamaged(int enchantLevel, Player player, Entity target, double damage, AtomicDouble boostDamage, AtomicDouble reduceDamage, AtomicBoolean cancel) {
        Player targetPlayer = (Player) target;
        if (player.isBlocking()) {
            if (hasBlocks.getOrDefault(player.getUniqueId(), 0).equals(onBlocks(enchantLevel))) {
                hasBlocks.replace(player.getUniqueId(), 0);

                targetPlayer.sendMessage(CC.translate("&b&l寒霜侵袭! &7你的移速下降了!"));
                player.sendMessage(CC.translate("&b&l寒霜侵袭! &7目标移速下降了!"));

                float speed = targetPlayer.getWalkSpeed();

                targetPlayer.setWalkSpeed(speed - (speed * (enchantLevel * 0.15F)));

                BukkitRunnable damageTask = new BukkitRunnable() {
                    public void run() {
                        PlayerUtil.damage(targetPlayer, PlayerUtil.DamageType.TRUE, 1.0, false);
                    }
                };

                damageTask.runTaskTimer(ThePit.getInstance(), 20L, 20L);

                new BukkitRunnable() {
                    public void run() {
                        targetPlayer.setWalkSpeed(speed);
                        damageTask.cancel();
                    }
                }.runTaskLater(ThePit.getInstance(), 4 * 20L);

            } else {
                hasBlocks.put(player.getUniqueId(), hasBlocks.getOrDefault(player.getUniqueId(), 0) + 1);
            }
        }
    }

    @Override
    public String getText(int enchantLevel, Player player) {
        return hasBlocks.getOrDefault(player.getUniqueId(), 0).equals(onBlocks(enchantLevel)) ? "&a&l✔" : "&e&l格挡次数: " + hasBlocks.getOrDefault(player.getUniqueId(), 0);
    }
}
