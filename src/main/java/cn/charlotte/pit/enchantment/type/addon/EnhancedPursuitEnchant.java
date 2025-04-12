package cn.charlotte.pit.enchantment.type.addon;

import cn.charlotte.pit.enchantment.AbstractEnchantment;
import cn.charlotte.pit.enchantment.IActionDisplayEnchant;
import cn.charlotte.pit.enchantment.param.item.WeaponOnly;
import cn.charlotte.pit.enchantment.rarity.EnchantmentRarity;
import cn.charlotte.pit.parm.listener.IAttackEntity;
import cn.charlotte.pit.util.cooldown.Cooldown;
import com.google.common.util.concurrent.AtomicDouble;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import real.nanoneko.register.IMagicLicense;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@WeaponOnly
public class EnhancedPursuitEnchant extends AbstractEnchantment implements IAttackEntity, IActionDisplayEnchant, IMagicLicense {
    private static final HashMap<UUID, String> targetNames = new HashMap<>();

    public String getEnchantName() {
        return "强化追击";
    }

    public int getMaxEnchantLevel() {
        return 3;
    }

    public String getNbtName() {
        return "enhanced_pursuit";
    }

    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.RARE;
    }

    public Cooldown getCooldown() {
        return null;
    }

    public String getUsefulnessLore(int enchantLevel) {
        return "&7命中目标时, 若没有锁定目标, 则立刻锁定当前攻击目标 /s" +
                "&7若攻击已锁定目标, 则自身伤害提升 &c" + (int)(boostNumber(enchantLevel) * 100) + "% /s" +
                "&7若攻击其他未锁定目标, 则当前锁定目标将会更新";
    }

    private double boostNumber(int enchantLevel) {
        switch (enchantLevel) {
            case 2:
                return 0.35;
            case 3:
                return 0.5;
            default:
                return 0.2;
        }
    }

    public void handleAttackEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        Player targetPlayer = (Player) target;
        String currentTarget = targetNames.getOrDefault(attacker.getUniqueId(), null);
        if (currentTarget == null | !Objects.equals(currentTarget, targetPlayer.getName())) {
            targetNames.put(attacker.getUniqueId(), targetPlayer.getName());
        } else {
            boostDamage.getAndAdd(boostNumber(enchantLevel));
        }
    }

    @Override
    public String getText(int enchantLevel, Player player) {
        return targetNames.getOrDefault(player.getUniqueId(), null) == null ? "&e&lnull" : "&e&l" + targetNames.getOrDefault(player.getUniqueId(), null);
    }
}
