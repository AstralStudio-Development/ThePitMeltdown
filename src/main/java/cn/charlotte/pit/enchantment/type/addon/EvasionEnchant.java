package cn.charlotte.pit.enchantment.type.addon;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.enchantment.AbstractEnchantment;
import cn.charlotte.pit.enchantment.param.item.ArmorOnly;
import cn.charlotte.pit.enchantment.rarity.EnchantmentRarity;

import cn.charlotte.pit.util.PlayerUtil;
import cn.charlotte.pit.util.chat.CC;
import cn.charlotte.pit.util.cooldown.Cooldown;
import cn.charlotte.pit.util.item.ItemUtil;
import cn.charlotte.pit.util.random.RandomUtil;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import real.nanoneko.register.IMagicLicense;


@ArmorOnly
public class EvasionEnchant extends AbstractEnchantment implements Listener, IMagicLicense {
    @Override
    public String getEnchantName() {
        return "闪避";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "evasion";
    }

    @Override
    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.RAGE_RARE;
    }

    @Override
    public Cooldown getCooldown() {
        return null;
    }

    @Override
    public String getUsefulnessLore(int enchantLevel) {
        return "&7受击时, 有 &f" + enchantLevel * 6 + "% &7的几率无视此次攻击 /s" +
                "&7同时, 获得 &b速度 II &f(00:03) &7效果";
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (p.getInventory().getLeggings() != null && ItemUtil.getInternalName(p.getInventory().getLeggings()).equalsIgnoreCase("mythic_leggings") && ThePit.getApi().getItemEnchantLevel(p.getInventory().getLeggings(), "evasion") >= 0) {

                Player attacker = (Player)((e.getDamager() instanceof Arrow) ? ((Arrow) e.getDamager()).getShooter() : e.getDamager());
                if (PlayerUtil.isVenom(p) || PlayerUtil.isVenom(attacker)) return;

                int enchantLevel = ThePit.getApi().getItemEnchantLevel(p.getInventory().getLeggings(), "evasion");
                if (RandomUtil.hasSuccessfullyByChance(enchantLevel * 0.05)) {
                    attacker.sendMessage(CC.translate("&f&l闪避! &7对方无视了该次攻击!"));
                    p.sendMessage(CC.translate("&f&l闪避! &7你无视了该次攻击!"));

                    e.setCancelled(true);

                    if (p.hasPotionEffect(PotionEffectType.SPEED)) p.removePotionEffect(PotionEffectType.SPEED);

                    p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 80, 1, true, false));
                }
            }
        }
    }
}
