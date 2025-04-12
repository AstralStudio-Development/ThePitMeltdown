package cn.charlotte.pit.enchantment.type.alternative;

import cn.charlotte.pit.enchantment.AbstractEnchantment;
import cn.charlotte.pit.enchantment.IActionDisplayEnchant;
import cn.charlotte.pit.enchantment.param.item.WeaponOnly;
import cn.charlotte.pit.enchantment.rarity.EnchantmentRarity;
import cn.charlotte.pit.parm.AutoRegister;
import cn.charlotte.pit.util.PlayerUtil;
import cn.charlotte.pit.util.cooldown.Cooldown;
import cn.charlotte.pit.util.item.ItemUtil;
import cn.charlotte.pit.util.thread.ThreadHelper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Starry_Killer
 * @Created_In: 2024/4/21
 */
@WeaponOnly
public class SpikeEnchant extends AbstractEnchantment implements Listener, IActionDisplayEnchant, ThreadHelper {

    public static final HashMap<UUID, Cooldown> cooldown = new HashMap<>();
    private static final SpikeEnchant spikeEnchant = new SpikeEnchant();

    @Override
    public String getEnchantName() {
        return "突刺";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "Spike";
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
        if (enchantLevel == 1) {
            return "&7右键发动技能,向准星方向突进一小段距离 (5秒冷却)";
        } else if (enchantLevel == 2) {
            return "&7右键发动技能,向准星方向突进一小段距离/s&7之后获得 &b速度 II &f(00:03)&7 (5秒冷却)";
        } else {
            return "&7右键发动技能,向准星方向突进一小段距离/s&7之后获得 &b速度 III &f(00:03) &7与 &c力量 I &f(00:03)&7 (5秒冷却)";
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // 检查是否是右键点击
        if (event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
            Player player = event.getPlayer();
            if (player.getInventory().getLeggings() != null && "mythic_sword".equals(ItemUtil.getInternalName(player.getInventory().getItemInHand())) && spikeEnchant.isItemHasEnchant(player.getInventory().getItemInHand())) {
                if (cooldown.getOrDefault(player.getUniqueId(), new Cooldown(0)).hasExpired()) {
                    cooldown.put(player.getUniqueId(), new Cooldown(5, TimeUnit.SECONDS));
                    int level = spikeEnchant.getItemEnchantLevel(player.getInventory().getItemInHand());
                    if (level == 1) {
                        player.setVelocity(event.getPlayer().getLocation().getDirection().multiply(2.0));
                    } else if (level == 2) {
                        player.setVelocity(event.getPlayer().getLocation().getDirection().multiply(2.0));
                        player.removePotionEffect(PotionEffectType.SPEED);
                        sync(() -> {
                            PlayerUtil.addPotionEffect(player, new PotionEffect(PotionEffectType.SPEED, 20 * 3, 1,true), true);
                        });
                    } else if (level == 3) {
                        player.setVelocity(event.getPlayer().getLocation().getDirection().multiply(2.0));
                        player.removePotionEffect(PotionEffectType.SPEED);
                        player.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
                        sync(() -> {
                            PlayerUtil.addPotionEffect(player, new PotionEffect(PotionEffectType.SPEED, 20 * 3, 2,true), true);
                            PlayerUtil.addPotionEffect(player, new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20 * 3, 0,true), true);
                        });
                    }
                }
            }
        }
    }

    @Override
    public String getText(int level, Player player) {
        return this.getCooldownActionText(cooldown.getOrDefault(player.getUniqueId(), new Cooldown(0)));
    }

}