package cn.charlotte.pit.enchantment.type.addon;

import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.enchantment.AbstractEnchantment;
import cn.charlotte.pit.enchantment.IActionDisplayEnchant;
import cn.charlotte.pit.enchantment.param.item.WeaponOnly;
import cn.charlotte.pit.enchantment.rarity.EnchantmentRarity;
import cn.charlotte.pit.parm.listener.IAttackEntity;
import cn.charlotte.pit.util.cooldown.Cooldown;
import com.google.common.util.concurrent.AtomicDouble;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import real.nanoneko.register.IMagicLicense;

import java.util.concurrent.atomic.AtomicBoolean;

@WeaponOnly
public class VitalEchoEnchant extends AbstractEnchantment implements IAttackEntity, IActionDisplayEnchant, IMagicLicense {

    public String getEnchantName() {
        return "生命回响";
    }

    public int getMaxEnchantLevel() {
        return 3;
    }

    public String getNbtName() {
        return "vital_echo";
    }

    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.RARE;
    }

    public Cooldown getCooldown() {
        return null;
    }

    public String getUsefulnessLore(int enchantLevel) {
        return "&7每次命中目标时将回复 &c0.5❤ &7生命值 /s" +
                "&7每命中目标 &e" + Hits(enchantLevel) + " &7次时 ,下次攻击将对目标额外造成自身生命值的 &c" + enchantLevel * 5 + "% &7的伤害/s" +
                "&7同时, 将额外回复 &c1.0❤ &7生命值";
    }

    private int Hits(int enchantLevel) {
        switch (enchantLevel) {
            case 2:
                return 5;
            case 3:
                return 4;
            default:
                return 6;
        }
    }

    @WeaponOnly
    public void handleAttackEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        Player targetPlayer = (Player) target;
        int hit = attacker.getItemInHand() != null && attacker.getItemInHand().getType() == Material.BOW
                ? PlayerProfile.getPlayerProfileByUuid(attacker.getUniqueId()).getBowHit()
                : PlayerProfile.getPlayerProfileByUuid(attacker.getUniqueId()).getMeleeHit();

        int activeHitValue = Hits(enchantLevel);

        if (attacker.getHealth() <= attacker.getMaxHealth() - 1) {
            attacker.setHealth(attacker.getHealth() + 1);
        }

        if (hit % activeHitValue == 0) {
            double healthDamage = attacker.getHealth() * (enchantLevel * 0.05);
            targetPlayer.damage(healthDamage);
            if (attacker.getHealth() <= attacker.getMaxHealth() - 2) {
                attacker.setHealth(attacker.getHealth() + 2);
            }
        }
    }

    public String getText(int enchantLevel, Player player) {
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        int hit = (player.getItemInHand() != null && player.getItemInHand().getType() == Material.BOW) ?
                profile.getBowHit() : profile.getMeleeHit();
        return hit % Hits(enchantLevel) == 0 ? "&a&l✔" : "&e&l" + (Hits(enchantLevel) - hit % Hits(enchantLevel));
    }
}
