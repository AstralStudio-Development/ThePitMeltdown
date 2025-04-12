package cn.charlotte.pit.enchantment.type.alternative;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.enchantment.AbstractEnchantment;
import cn.charlotte.pit.enchantment.param.item.ArmorOnly;
import cn.charlotte.pit.enchantment.rarity.EnchantmentRarity;
import cn.charlotte.pit.parm.AutoRegister;
import cn.charlotte.pit.parm.listener.IPlayerBeKilledByEntity;
import cn.charlotte.pit.util.cooldown.Cooldown;
import com.google.common.util.concurrent.AtomicDouble;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * @Author: Starry_Killer
 * @Created_In: 2024/7/7 23:17
 */
@ArmorOnly
public class MartyrdomEnchant extends AbstractEnchantment implements IPlayerBeKilledByEntity, Listener {

    @Override
    public String getEnchantName() {
        return "殉道者";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "martyrdom_enchant";
    }

    @Override
    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.RARE;
    }

    @Override
    public Cooldown getCooldown() {
        return null;
    }

    @Override
    public String getUsefulnessLore(int enchantLevel) {
        return "&7死亡时生成 &d" + getValue(enchantLevel) + " &7只爬行者";
    }

    @Override
    public void handlePlayerBeKilledByEntity(int enchantLevel, Player myself, Entity target, AtomicDouble coins, AtomicDouble experience) {
        Location targetLocation = myself.getLocation();
        for (int i = 0; i < getValue(enchantLevel); ++i) {
            double angle = (double)i * 1.5707963267948966;
            double offsetX = Math.sin(angle) * 1.5;
            double offsetZ = Math.cos(angle) * 1.5;
            Location creeperLocation = targetLocation.clone().add(offsetX, 2.0, offsetZ);
            final Creeper creeper = (Creeper) target.getWorld().spawnEntity(creeperLocation, EntityType.CREEPER);
            creeper.setCustomNameVisible(false);
            new BukkitRunnable(){

                public void run() {
                    creeper.remove();
                    Location explosionLocation = creeper.getLocation();
                    TNTPrimed tnt = (TNTPrimed)explosionLocation.getWorld().spawnEntity(explosionLocation, EntityType.PRIMED_TNT);
                    tnt.setFuseTicks(0);
                    tnt.setIsIncendiary(false);
                    for (Entity entity : explosionLocation.getWorld().getNearbyEntities(explosionLocation, 5.0, 5.0, 5.0)) {
                        Player affectedPlayer;
                        if (!(entity instanceof Player) || (affectedPlayer = (Player) entity).getUniqueId().equals(target.getUniqueId()))
                            continue;
                        affectedPlayer.damage(12.0);
                        Vector velocity = affectedPlayer.getLocation().toVector().subtract(explosionLocation.toVector()).normalize().multiply(3);
                        affectedPlayer.setVelocity(velocity);
                    }
                }
            }.runTaskLater(ThePit.getInstance(), 20L);
        }
    }

    public int getValue(int enchantLevel) {
        switch (enchantLevel) {
            case 2:
                return 20;
            case 3:
                return 40;
            default:
                return 10;
        }
    }

    @EventHandler
    public void onTNTExplode(BlockExplodeEvent e) {
        e.blockList().clear();
        e.setCancelled(true);
    }

    @EventHandler
    public void onTNTEntityExplode(EntityExplodeEvent e) {
        e.blockList().clear();
    }
}
