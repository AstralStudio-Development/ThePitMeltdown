package cn.charlotte.pit.enchantment.type.addon;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.enchantment.AbstractEnchantment;
import cn.charlotte.pit.enchantment.param.item.BowOnly;
import cn.charlotte.pit.enchantment.rarity.EnchantmentRarity;
import cn.charlotte.pit.parm.listener.IPlayerShootEntity;
import cn.charlotte.pit.util.PlayerUtil;
import cn.charlotte.pit.util.cooldown.Cooldown;
import com.google.common.util.concurrent.AtomicDouble;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import real.nanoneko.register.IMagicLicense;

import java.util.concurrent.atomic.AtomicBoolean;

@BowOnly
public class LunarDeityEnchant extends AbstractEnchantment implements Listener, IPlayerShootEntity, IMagicLicense {
    @Override
    public String getEnchantName() {
        return "月神之矢";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "lunar_deity";
    }

    @Override
    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.RARE;
    }

    @Nullable
    @Override
    public Cooldown getCooldown() {
        return null;
    }

    @Override
    public String getUsefulnessLore(int enchantLevel) {
        return "&7射出箭矢时, 箭矢将锁定于自身中心的 &f" + checkRadius(enchantLevel) + " &7格内距离最近的目标 /s" +
                "&7射出的箭矢速度将会加快, 同时命中目标时将获得 &3抗性提升 I &f(00:04)";
    }

    private int checkRadius(int enchantLevel) {
        return (enchantLevel >= 3 ? enchantLevel * 8 : enchantLevel + 12);
    }

    @EventHandler
    public void onPlayerShootBow(EntityShootBowEvent e) {
        if (!(e.getProjectile() instanceof Arrow)) return;
        Arrow arrow = (Arrow) e.getProjectile();

        if (!(arrow.getShooter() instanceof Player)) return;
        Player shooter = (Player) arrow.getShooter();
        ItemStack handItem = shooter.getItemInHand();

        int enchantLevel = ThePit.getApi().getItemEnchantLevel(handItem, this.getNbtName());
        if (enchantLevel < 1 || PlayerUtil.shouldIgnoreEnchant(shooter)) return;

        Location arrowLoc = arrow.getLocation().clone();

        int radius = checkRadius(enchantLevel);
        double nearestDistanceSquared = Double.MAX_VALUE;
        Player nearestPlayer = null;
        double radiusSquared = radius * radius;

        for (Entity entity : arrow.getNearbyEntities(radius, radius, radius)) {
            if (!(entity instanceof Player)) continue;
            Player target = (Player) entity;
            if (target == shooter) continue;

            double dx = arrowLoc.getX() - target.getLocation().getX();
            double dy = arrowLoc.getY() - target.getLocation().getY();
            double dz = arrowLoc.getZ() - target.getLocation().getZ();
            double distanceSquared = dx*dx + dy*dy + dz*dz;

            if (distanceSquared < nearestDistanceSquared && distanceSquared <= radiusSquared) {
                nearestDistanceSquared = distanceSquared;
                nearestPlayer = target;
            }
        }

        if (nearestPlayer != null) {
            arrow.setVelocity(optimizedTrajectory(arrowLoc, nearestPlayer, arrow.getVelocity().length() + 0.2));
        }
    }

    private Vector optimizedTrajectory(Location start, Player target, double speed) {
        Location targetHead = target.getLocation().add(0, 1.55, 0);
        double tx = targetHead.getX() - start.getX();
        double ty = targetHead.getY() - start.getY();
        double tz = targetHead.getZ() - start.getZ();

        double hDistSq = tx*tx + tz*tz;
        double hDist = Math.sqrt(hDistSq);

        double gravityComp = 0.05;
        ty += gravityComp * hDist;

        double angle = Math.atan2(ty, hDist);
        double yVel = Math.sin(angle) * speed;
        double hVel = Math.cos(angle) * speed;

        if (hDist > 1e-5) {
            return new Vector(
                    (tx/hDist) * hVel,
                    yVel,
                    (tz/hDist) * hVel
            );
        }
        return new Vector(0, yVel, 0);
    }

    @Override
    public void handleShootEntity(int i, Player shooter, Entity target, double v, AtomicDouble atomicDouble, AtomicDouble atomicDouble1, AtomicBoolean atomicBoolean) {
        if (shooter.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) shooter.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
        shooter.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 4, 0, false ,true));
    }
}
