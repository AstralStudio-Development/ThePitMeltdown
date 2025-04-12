package cn.charlotte.pit.enchantment.type.addon;

import cn.charlotte.pit.enchantment.AbstractEnchantment;
import cn.charlotte.pit.enchantment.IActionDisplayEnchant;
import cn.charlotte.pit.enchantment.param.item.WeaponOnly;
import cn.charlotte.pit.enchantment.rarity.EnchantmentRarity;
import cn.charlotte.pit.parm.listener.IAttackEntity;
import cn.charlotte.pit.util.cooldown.Cooldown;
import cn.charlotte.pit.util.time.TimeUtil;
import com.google.common.util.concurrent.AtomicDouble;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import real.nanoneko.register.IMagicLicense;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@WeaponOnly
public class DivineBlazeResolutionEnchant extends AbstractEnchantment implements IAttackEntity, Listener, IActionDisplayEnchant, IMagicLicense {
    private static final HashMap<UUID, Cooldown> cooldown = new HashMap<>();

    public String getEnchantName() {
        return "炽焰神裁";
    }

    public int getMaxEnchantLevel() {
        return 3;
    }

    public String getNbtName() {
        return "divine_blaze_resolution";
    }

    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.RARE;
    }

    public Cooldown getCooldown() {
        return null;
    }

    public String getUsefulnessLore(int enchantLevel) {
        return "&7攻击目标时, 为目标施加 &6炽之罚&8(火焰附加) &7效果, 持续 &e4s /s" +
                "&7当攻击附着有 &6炽之罚 &7的目标时, &7则攻击额外造成 &f" + (enchantLevel * 0.5) + "❤ &7的&c必中&7伤害 /s" +
                "&7同时 &6炽之罚 &7效果消失, 进入冷却时间 &8( " + getCooldownSeconds(enchantLevel) + "s冷却 ) /s" +
                "&8( 如果附着效果期间未攻击而导致附着效果失效, 将不会进入冷却时间 )";
    }

    private int getCooldownSeconds(int enchantLevel) {
        switch (enchantLevel) {
            case 2:
                return 15;
            case 3:
                return 10;
            default:
                return 18;
        }
    }

    public void handleAttackEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        Player targetPlayer = (Player) target;
        if (cooldown.getOrDefault(attacker.getUniqueId(), new Cooldown(0L)).hasExpired()) {

            if (targetPlayer.getFireTicks() > 0) {
                cooldown.put(attacker.getUniqueId(), new Cooldown(getCooldownSeconds(enchantLevel), TimeUnit.SECONDS));
                if (targetPlayer.getHealth() > enchantLevel) {
                    targetPlayer.setHealth(targetPlayer.getHealth() - enchantLevel);
                } else {
                    targetPlayer.damage(targetPlayer.getMaxHealth() * 100);
                }
                targetPlayer.setFireTicks(0);
            } else {
                targetPlayer.setFireTicks(80);
            }

        }
    }

    @EventHandler
    public void onAttackTarget(EntityDamageByEntityEvent event) {
        Player targetPlayer = (Player) event.getEntity();
        if (targetPlayer.getFireTicks() > 0 && event.getCause() == DamageCause.FIRE || event.getCause() == DamageCause.FIRE_TICK) {
            event.setCancelled(true);
        }
    }

    public String getText(int level, Player attacker) {
        return (cooldown.getOrDefault(attacker.getUniqueId(), new Cooldown(0L))).hasExpired() ? "&a&l✔" : "&c&l" + TimeUtil.millisToRoundedTime((cooldown.get(attacker.getUniqueId())).getRemaining()).replace(" ", "");
    }
}

