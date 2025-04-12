package cn.charlotte.pit.enchantment.type.alternative;

import cn.charlotte.pit.enchantment.AbstractEnchantment;
import cn.charlotte.pit.enchantment.param.item.WeaponOnly;
import cn.charlotte.pit.enchantment.rarity.EnchantmentRarity;
import cn.charlotte.pit.parm.listener.IPlayerDamaged;
import cn.charlotte.pit.util.cooldown.Cooldown;
import com.google.common.util.concurrent.AtomicDouble;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author: Starry_Killer
 * @Created_In: 2024/8/18 16:45
 */
@WeaponOnly
public class GrassHopperEnchant extends AbstractEnchantment implements IPlayerDamaged {

    @Override
    public String getEnchantName() {
        return "蚱蜢";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "grass_hopper";
    }

    @Override
    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.NORMAL;
    }

    @Override
    public Cooldown getCooldown() {
        return null;
    }

    @Override
    public String getUsefulnessLore(int enchantLevel) {
		int target = 0;
		switch (enchantLevel) {
			case 1:
				target = 5;
				break;
			case 2:
				target = 9;
				break;
			case 3:
				target = 15;
				break;
		}
        return "&7站在草方块上造成的伤害 &c+" + target + "% &7.";
    }

    @Override
    public void handlePlayerDamaged(int enchantLevel, Player myself, Entity attacker, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        if (attacker instanceof Player) {
            org.bukkit.Location playerLocation = ((Player)attacker).getLocation();
			org.bukkit.block.Block block = playerLocation.getBlock();
			double target = 0;
			switch (enchantLevel) {
				case 1:
					target = 0.05;
					break;
				case 2:
					target = 0.09;
					break;
				case 3:
					target = 0.15;
					break;
			}
			if (block.getType() == Material.GRASS) {
				boostDamage.set(damage * target);
			}
        }
    }
}