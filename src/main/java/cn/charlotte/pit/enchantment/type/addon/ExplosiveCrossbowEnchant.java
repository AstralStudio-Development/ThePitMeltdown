package cn.charlotte.pit.enchantment.type.addon;

import cn.charlotte.pit.enchantment.AbstractEnchantment;
import cn.charlotte.pit.enchantment.param.item.BowOnly;
import cn.charlotte.pit.enchantment.rarity.EnchantmentRarity;
import cn.charlotte.pit.parm.listener.IPlayerShootEntity;
import cn.charlotte.pit.util.cooldown.Cooldown;
import com.google.common.util.concurrent.AtomicDouble;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import real.nanoneko.register.IMagicLicense;

import java.util.concurrent.atomic.AtomicBoolean;

@BowOnly
public class ExplosiveCrossbowEnchant extends AbstractEnchantment implements IPlayerShootEntity, IMagicLicense {

    public String getEnchantName() {
        return "爆炸弩";
    }

    public int getMaxEnchantLevel() {
        return 3;
    }

    public String getNbtName() {
        return "explosive_crossbow";
    }

    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.RARE;
    }

    public Cooldown getCooldown() {
        return null;
    }

    public String getUsefulnessLore(int enchantLevel) {
        return "&7当弓箭命中目标时, 将以目标为中心的半径 &f2 &7格内的玩家 /s" +
                "&7造成 &c" + enchantLevel * 0.5 + "❤ &7的扩散伤害";
    }

    public void handleShootEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        Player targetPlayer = (Player) target;
        customExplosion(enchantLevel, targetPlayer.getLocation());
    }


    private void customExplosion(int enchantLevel, Location location) {
        for (Entity entity : location.getWorld().getNearbyEntities(location, 2.0, 2.0, 2.0)) {
            if (entity instanceof Player) {
                Player player = (Player) entity;
                player.damage(enchantLevel);
                player.getWorld().playSound(location, Sound.EXPLODE, 2F, 2F);
                player.getWorld().playEffect(location, Effect.EXPLOSION_LARGE, null);
                Vector currentVelocity = player.getVelocity();
                player.setVelocity(new Vector(currentVelocity.getX(), 0.5, currentVelocity.getZ()));
            }
        }
    }

}
