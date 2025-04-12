package cn.charlotte.pit.enchantment.type.addon;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.enchantment.AbstractEnchantment;
import cn.charlotte.pit.enchantment.param.event.PlayerOnly;
import cn.charlotte.pit.enchantment.param.item.BowOnly;
import cn.charlotte.pit.enchantment.rarity.EnchantmentRarity;
import cn.charlotte.pit.parm.listener.IPlayerShootEntity;
import cn.charlotte.pit.util.PlayerUtil;
import cn.charlotte.pit.util.cooldown.Cooldown;
import com.google.common.util.concurrent.AtomicDouble;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;
import real.nanoneko.register.IMagicLicense;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@BowOnly
public class FeatherBladeEnchantEnchant extends AbstractEnchantment implements Listener, IPlayerShootEntity, IMagicLicense {
    private static final HashMap<UUID, Integer> Level = new HashMap<>();

    @Override
    public String getEnchantName() {
        return "羽刃";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "feather_blade";
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
        return "&7射出的箭矢命中目标时, 将为目标施加标记 &c翎羽 &f(00:0" + (enchantLevel + 3) + ") /s" +
                "&7若攻击持有 &c翎羽 &7标记的目标时, 将额外造成 &c" + ((enchantLevel * 10) + 10) + "% &7的伤害";
    }

    @PlayerOnly
    public void handleShootEntity(int enchantLevel, Player shooter, Entity target, double v, AtomicDouble atomicDouble, AtomicDouble atomicDouble1, AtomicBoolean atomicBoolean) {
        Player targetPlayer = (Player) target;

        if (targetPlayer.hasMetadata("featherBladeDamager")) targetPlayer.removeMetadata("featherBladeDamager", ThePit.getInstance());

        if (shooter.hasMetadata("featherBladeAttacker")) shooter.removeMetadata("featherBladeAttacker", ThePit.getInstance());

        if (Level.get(shooter.getUniqueId()) != null) Level.remove(shooter.getUniqueId());
        Level.put(shooter.getUniqueId(), enchantLevel);

        targetPlayer.setMetadata("featherBladeDamager", new FixedMetadataValue(ThePit.getInstance(), System.currentTimeMillis() + ((enchantLevel + 3) * 1000L)));
        shooter.setMetadata("featherBladeAttacker", new FixedMetadataValue(ThePit.getInstance(), System.currentTimeMillis() + ((enchantLevel + 3) * 1000L)));
    }

    @PlayerOnly
    @EventHandler(
            priority = EventPriority.LOW
    )
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player target = (Player)event.getEntity();
        if (!(event.getDamager() instanceof Arrow) || !(event.getDamager() instanceof Player)) return;
        Player attacker = (Player)(event.getDamager() instanceof Arrow ? ((Arrow) event.getDamager()).getShooter() : event.getDamager());

        if (PlayerUtil.isVenom(attacker) || PlayerUtil.isVenom(target)) return;

        if (target.hasMetadata("featherBladeDamager") && target.getMetadata("featherBladeDamager").get(0).asLong() > System.currentTimeMillis()) {
            if (attacker.hasMetadata("featherBladeAttacker") && attacker.getMetadata("featherBladeAttacker").get(0).asLong() > System.currentTimeMillis()) {
                double boostDamageCount = 0;
                if (Level.get(attacker.getUniqueId()) != null) boostDamageCount = (Level.get(attacker.getUniqueId()) * 0.1) + 0.1;
                event.setDamage(event.getDamage() * (1 + boostDamageCount));
            }
        }
    }
}
