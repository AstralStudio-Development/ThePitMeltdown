package cn.charlotte.pit.enchantment.type.addon;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.enchantment.AbstractEnchantment;
import real.nanoneko.register.IMagicLicense;
import cn.charlotte.pit.enchantment.IActionDisplayEnchant;
import cn.charlotte.pit.enchantment.param.item.BowOnly;
import cn.charlotte.pit.enchantment.param.item.WeaponOnly;
import cn.charlotte.pit.enchantment.rarity.EnchantmentRarity;

import cn.charlotte.pit.parm.listener.IAttackEntity;
import cn.charlotte.pit.parm.listener.IPlayerShootEntity;
import cn.charlotte.pit.util.chat.CC;
import cn.charlotte.pit.util.cooldown.Cooldown;
import com.google.common.util.concurrent.AtomicDouble;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


@BowOnly
@WeaponOnly
public class ComboBrokenStringEnchant extends AbstractEnchantment implements IMagicLicense, IPlayerShootEntity, IAttackEntity, Listener, IActionDisplayEnchant {
    private static final String BROKEN_STRING = "BrokenString";
    private static final HashMap<UUID, Cooldown> cooldown = new HashMap<>();

    @Override
    public String getEnchantName() {
        return "强力击: 断弦";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "Combo_Broken_String";
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
        return "&7每命中目标 &e3 &7次, 将对目标施加 &8断弦 &7(00:0" + (enchantLevel >= 3 ? 4 : 2) + ") 效果 (20s冷却) /s" +
                "&7效果 &8断弦 &7: 无法射出箭矢";
    }

    @Override
    public void handleAttackEntity(int enchantLevel, Player attacker, Entity target, double v, AtomicDouble atomicDouble, AtomicDouble atomicDouble1, AtomicBoolean atomicBoolean) {
        if (!(target instanceof Player)) return;

        if (!cooldown.getOrDefault(attacker.getUniqueId(), new Cooldown(0L)).hasExpired()) return;

        int hit = 0;

        if (attacker.getItemInHand() != null) hit = PlayerProfile.getPlayerProfileByUuid(attacker.getUniqueId()).getMeleeHit();

        if (hit % 3 == 0) {
            cooldown.put(attacker.getUniqueId(), new Cooldown((enchantLevel >= 2 ? 16L : 20L), TimeUnit.SECONDS));
            onActive(attacker, ((Player) target).getPlayer(), enchantLevel);
        }
    }

    @Override
    public void handleShootEntity(int enchantLevel, Player shooter, Entity target, double v, AtomicDouble atomicDouble, AtomicDouble atomicDouble1, AtomicBoolean atomicBoolean) {
        if (!(target instanceof Player)) return;

        if (!cooldown.getOrDefault(shooter.getUniqueId(), new Cooldown(0L)).hasExpired()) return;

        int hit = 0;

        if (shooter.getItemInHand() != null) hit = PlayerProfile.getPlayerProfileByUuid(shooter.getUniqueId()).getBowHit();

        if (hit % 3 == 0) {
            cooldown.put(shooter.getUniqueId(), new Cooldown((enchantLevel >= 2 ? 16L : 20L), TimeUnit.SECONDS));
            onActive(shooter, ((Player) target).getPlayer(), enchantLevel);
        }
    }

    @EventHandler
    public void onShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player shooter = (Player) event.getEntity();

        if (!shooter.hasMetadata(BROKEN_STRING) || (shooter.getMetadata(BROKEN_STRING).get(0)).asLong() <= System.currentTimeMillis()) return;

        event.setCancelled(true);
        shooter.sendMessage(CC.translate("&c&l断弦! &7你现在无法发射箭矢!"));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (player.hasMetadata(BROKEN_STRING)) player.removeMetadata(BROKEN_STRING, ThePit.getInstance());
    }

    private void onActive(Player attacker, Player targetPlayer, int enchantLevel) {
        if (targetPlayer.hasMetadata(BROKEN_STRING)) targetPlayer.removeMetadata(BROKEN_STRING, ThePit.getInstance());

        targetPlayer.setMetadata(BROKEN_STRING, new FixedMetadataValue(ThePit.getInstance(), System.currentTimeMillis() + (enchantLevel >= 3 ? 4 : 2) * 1000L));

        attacker.sendMessage(CC.translate("&c&l断弦! &f" + targetPlayer.getDisplayName() + " &7将在接下来 &e" + (enchantLevel >= 3 ? 4 : 2) + "s &7内无法发射箭矢!"));
        targetPlayer.sendMessage(CC.translate("&c&l断弦! &7你将在接下来 &e" + enchantLevel * 2 + "s &7内无法发射箭矢!"));
    }


    @Override
    public String getText(int i, Player player) {
        return cooldown.getOrDefault(player.getUniqueId(), new Cooldown(0L)).hasExpired()
                ? getHitActionText(player, 3)
                : getCooldownActionText(cooldown.getOrDefault(player.getUniqueId(), new Cooldown(0L)));
    }
}
