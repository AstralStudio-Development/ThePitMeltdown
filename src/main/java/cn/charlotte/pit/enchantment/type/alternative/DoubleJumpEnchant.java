package cn.charlotte.pit.enchantment.type.alternative;

import cn.charlotte.pit.enchantment.AbstractEnchantment;
import cn.charlotte.pit.enchantment.IActionDisplayEnchant;
import cn.charlotte.pit.enchantment.param.item.ArmorOnly;
import cn.charlotte.pit.enchantment.rarity.EnchantmentRarity;
import cn.charlotte.pit.parm.AutoRegister;
import cn.charlotte.pit.util.cooldown.Cooldown;
import cn.charlotte.pit.util.item.ItemUtil;
import cn.charlotte.pit.util.time.TimeUtil;
import lombok.SneakyThrows;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import spg.lgdev.handler.MovementHandler;
import spg.lgdev.iSpigot;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Starry_Killer
 * @Created_In: 2024/1/30
 */
@ArmorOnly
public class DoubleJumpEnchant extends AbstractEnchantment implements Listener, IActionDisplayEnchant, MovementHandler {

    private static final DoubleJumpEnchant doubleJumpEnchant = new DoubleJumpEnchant();
    private static final HashMap<UUID, Cooldown> cooldown = new HashMap<>();

    @SneakyThrows
    public DoubleJumpEnchant() {
        try {
            iSpigot.INSTANCE.addMovementHandler(this);
        } catch (NoClassDefFoundError ignore) {
        }
    }
    @Override
    public String getEnchantName() {
        return "二段跳";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "double_jump_enchant";
    }

    @Override
    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.RARE;
    }

    @Override
    public Cooldown getCooldown() {
        return null;
    }

    private static int getCooldownInt(int enchantLevel) {
        switch (enchantLevel) {
            case 2:
                return 10;
            case 3:
                return 5;
            default:
                return 20;
        }
    }

    @Override
    public String getUsefulnessLore(int enchantLevel) {
        return "&7在半空中按下跳跃键,你将向上方冲刺一段距离 (" + getCooldownInt(enchantLevel) + "秒冷却)";
    }

    @EventHandler
    public void onToggle(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        int level = doubleJumpEnchant.getItemEnchantLevel(player.getInventory().getLeggings());
        if (player.getInventory().getLeggings() != null && "mythic_leggings".equals(ItemUtil.getInternalName(player.getInventory().getLeggings())) && doubleJumpEnchant.isItemHasEnchant(player.getInventory().getLeggings()) && player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
            if (cooldown.getOrDefault(player.getUniqueId(), new Cooldown(0)).hasExpired()) {
                cooldown.put(player.getUniqueId(), new Cooldown(getCooldownInt(level), TimeUnit.SECONDS));
                event.setCancelled(true);
                player.setVelocity(event.getPlayer().getLocation().getDirection().multiply(1.2));
                player.setAllowFlight(false);
            }
        }
    }

    @Override
    public String getText(int level, Player player) {
        return cooldown.getOrDefault(player.getUniqueId(), new Cooldown(0)).hasExpired() ? "&a&l✔" : "&c&l" + TimeUtil.millisToRoundedTime(cooldown.get(player.getUniqueId()).getRemaining()).replace(" ", "");
    }

    @Override
    public void handleUpdateLocation(Player player, Location location, Location location1, PacketPlayInFlying packetPlayInFlying) {
        if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
            if (cooldown.getOrDefault(player.getUniqueId(), new Cooldown(0)).hasExpired()) {
                player.setAllowFlight(player.getInventory().getLeggings() != null && "mythic_leggings".equals(ItemUtil.getInternalName(player.getInventory().getLeggings())) && doubleJumpEnchant.isItemHasEnchant(player.getInventory().getLeggings()));
            }
        } else {
            player.setAllowFlight(true);
        }
    }

    @Override
    public void handleUpdateRotation(Player player, Location location, Location location1, PacketPlayInFlying packetPlayInFlying) {
    }
}
