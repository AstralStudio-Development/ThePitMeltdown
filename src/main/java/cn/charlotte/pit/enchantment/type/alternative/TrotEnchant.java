package cn.charlotte.pit.enchantment.type.alternative;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.enchantment.AbstractEnchantment;
import cn.charlotte.pit.enchantment.param.item.ArmorOnly;
import cn.charlotte.pit.enchantment.rarity.EnchantmentRarity;
import cn.charlotte.pit.util.cooldown.Cooldown;
import cn.charlotte.pit.util.item.ItemUtil;
import lombok.SneakyThrows;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import spg.lgdev.handler.MovementHandler;
import spg.lgdev.iSpigot;

/**
 * @Author: Starry_Killer
 * @Created_In: 2023/11/22 18:15
 * @Mod_By: Irina
 */
@ArmorOnly
public class TrotEnchant extends AbstractEnchantment implements MovementHandler {
    private static final TrotEnchant trotEnchant = new TrotEnchant();

    @SneakyThrows
    public TrotEnchant() {
        try {
            iSpigot.INSTANCE.addMovementHandler(this);
        } catch (NoClassDefFoundError ignore) {
        }
    }
    @Override
    public String getEnchantName() {
        return "疾走";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "Trot";
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
        return "&7穿戴时行走速度提升 &b" + (enchantLevel == 3 ? "20%" : (enchantLevel == 2 ? "10%" : "5%"));
    }

    @Override
    public void handleUpdateLocation(Player player, Location location, Location location1, PacketPlayInFlying packetPlayInFlying) {
        Bukkit.getScheduler().runTaskAsynchronously(ThePit.getInstance(), () -> {
            int enchantLevel = ThePit.getApi().getItemEnchantLevel(player.getInventory().getLeggings(), this.getNbtName());

            float walkSpeed = enchantLevel >= 1 ? 0.2f * (1f + (enchantLevel * 0.05f)) : 0.2f;

            if (player.getWalkSpeed() == walkSpeed) return;

            Bukkit.getScheduler().runTask(ThePit.getInstance(), () -> player.setWalkSpeed(walkSpeed));
        });
    }

    @Override
    public void handleUpdateRotation(Player player, Location location, Location location1, PacketPlayInFlying packetPlayInFlying) {

    }
}
