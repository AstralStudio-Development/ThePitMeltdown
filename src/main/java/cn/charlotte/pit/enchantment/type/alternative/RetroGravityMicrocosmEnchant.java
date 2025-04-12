package cn.charlotte.pit.enchantment.type.alternative;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.enchantment.AbstractEnchantment;
import cn.charlotte.pit.enchantment.param.item.ArmorOnly;
import cn.charlotte.pit.enchantment.rarity.EnchantmentRarity;
import cn.charlotte.pit.parm.listener.IAttackEntity;
import cn.charlotte.pit.parm.listener.IPlayerDamaged;
import cn.charlotte.pit.parm.listener.IPlayerShootEntity;
import cn.charlotte.pit.util.cooldown.Cooldown;
import com.google.common.util.concurrent.AtomicDouble;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author: pi_ka
 */
@ArmorOnly
public class RetroGravityMicrocosmEnchant extends AbstractEnchantment implements IPlayerDamaged, IAttackEntity, IPlayerShootEntity {
    public static HashMap<Player, Integer> attackCount = new HashMap();
    public static HashMap<Player, Cooldown> attackCoolDown = new HashMap();
    public static HashMap<Player, Cooldown> boostCoolDown = new HashMap();

    public String getEnchantName() {
        return "微观反重力";
    }

    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "retro_gravity_microcosm";
    }

    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.RARE;
    }

    public Cooldown getCooldown() {
        return null;
    }

    public String getUsefulnessLore(int enchantLevel) {
        String pre = "&7若你在空中连续受到 &e3 &7次攻击(2秒内),则恢复 &c2❤ &7生命值";
        String lvl2 = "/s&7并且在接下来的30秒内,增加 &b10% &7的伤害(可叠加,最高2层)";
        String lvl3 = "/s&7并且在接下来的30秒内,增加 &b15% &7的伤害(可叠加,最高2层)";
        if (enchantLevel == 1) {
            return pre;
        }
        return pre + (enchantLevel == 2 ? lvl2 : lvl3);
    }

    public void handlePlayerDamaged(int enchantLevel, Player player, Entity attacker, double v, AtomicDouble atomicDouble, AtomicDouble atomicDouble1, AtomicBoolean atomicBoolean) {
        if (!attackCount.containsKey(player)) {
            attackCount.put(player, 0);
        }
        this.attackHeal(player);
        if (enchantLevel == 1) {
            return;
        }
        if (boostCoolDown.getOrDefault(player, new Cooldown(0L)).hasExpired()) {
            boostCoolDown.put(player, new Cooldown(30L, TimeUnit.SECONDS));
            if (!player.hasMetadata("boost") || ((MetadataValue)player.getMetadata("boost").get(0)).asInt() > 2) {
                player.setMetadata("boost", new FixedMetadataValue(ThePit.getInstance(), 0));
            }
            if (((MetadataValue)player.getMetadata("boost").get(0)).asInt() != 2) {
                player.setMetadata("boost", new FixedMetadataValue(ThePit.getInstance(), ((MetadataValue)player.getMetadata("boost").get(0)).asInt() + 1));
            }
        }
    }

    private void attackHeal(Player player) {
        Block block = player.getWorld().getBlockAt(player.getLocation().getBlockX(), player.getLocation().getBlockY() - 1, player.getLocation().getBlockZ());
        player.sendMessage(block.getType().name());
        if (!block.getType().equals(Material.AIR)) {
            return;
        }
        if (attackCount.get(player) != 2) {
            if (attackCoolDown.getOrDefault(player, new Cooldown(0L)).hasExpired()) {
                attackCount.put(player, 1);
                attackCoolDown.put(player, new Cooldown(2L, TimeUnit.SECONDS));
                return;
            }
            attackCount.put(player, attackCount.get(player) + 1);
            return;
        }
        attackCoolDown.put(player, new Cooldown(2L, TimeUnit.SECONDS));
        attackCount.put(player, 0);
        if (player.getHealth() + 4.0 < player.getMaxHealth()) {
            player.setHealth(player.getHealth() + 4.0);
            return;
        }
        player.setHealth(player.getMaxHealth());
    }

    public void handleAttackEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        if (attacker.hasMetadata("boost") && !boostCoolDown.getOrDefault(attacker, new Cooldown(0L)).hasExpired()) {
            int boost = ((MetadataValue)attacker.getMetadata("boost").get(0)).asInt();
            if (boost == 1) {
                if (enchantLevel == 2) {
                    boostDamage.set(boostDamage.get() + 0.1);
                } else if (enchantLevel == 3) {
                    boostDamage.set(boostDamage.get() + 0.15);
                }
            } else if (boost == 2) {
                if (enchantLevel == 2) {
                    boostDamage.set(boostDamage.get() + 0.2);
                } else if (enchantLevel == 3) {
                    boostDamage.set(boostDamage.get() + 0.3);
                }
            }
        }
    }

    public void handleShootEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        if (attacker.hasMetadata("boost")) {
            int boost = ((MetadataValue)attacker.getMetadata("boost").get(0)).asInt();
            if (boost == 1) {
                if (enchantLevel == 2) {
                    boostDamage.set(boostDamage.get() + 0.1);
                } else if (enchantLevel == 3) {
                    boostDamage.set(boostDamage.get() + 0.15);
                }
            } else if (boost == 2) {
                if (enchantLevel == 2) {
                    boostDamage.set(boostDamage.get() + 0.2);
                } else if (enchantLevel == 3) {
                    boostDamage.set(boostDamage.get() + 0.3);
                }
            }
        }
    }
}
