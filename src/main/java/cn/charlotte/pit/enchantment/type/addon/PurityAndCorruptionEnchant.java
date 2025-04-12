package cn.charlotte.pit.enchantment.type.addon;

import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.enchantment.AbstractEnchantment;
import cn.charlotte.pit.enchantment.param.item.ArmorOnly;
import cn.charlotte.pit.enchantment.rarity.EnchantmentRarity;
import cn.charlotte.pit.events.genesis.team.GenesisTeam;
import cn.charlotte.pit.parm.listener.IAttackEntity;
import cn.charlotte.pit.parm.listener.ITickTask;
import cn.charlotte.pit.util.chat.CC;
import cn.charlotte.pit.util.cooldown.Cooldown;
import com.google.common.util.concurrent.AtomicDouble;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import real.nanoneko.register.IMagicLicense;

import java.util.concurrent.atomic.AtomicBoolean;

@ArmorOnly
public class PurityAndCorruptionEnchant extends AbstractEnchantment implements ITickTask, IAttackEntity, IMagicLicense {

    public String getEnchantName() {
        return "圣洁与堕落";
    }

    public int getMaxEnchantLevel() {
        return 3;
    }

    public String getNbtName() {
        return "purity_and_corruption";
    }

    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.RARE;
    }

    public Cooldown getCooldown() {
        return null;
    }

    private String getUseEffectSuffix(int enchantLevel) {
        switch (enchantLevel) {
            case 2:
                return "II";
            case 3:
                return "III";
            default:
                return "I";
        }
    }
    public String getUsefulnessLore(int enchantLevel) {
        return "&7穿戴时, 身处不同阵营将获得不同效果: " + "/s" +
                "&b天使&f: &7获得永久 &b抗性提升 I &7, 攻击时恢复自身血量 &c" + enchantLevel * 0.5 + "❤&7" + "/s" +
                "&c恶魔&f: &7每秒受到 &f" + enchantLevel * 0.5 + "❤ &7的伤害, 获得永久 &b速度 " + getUseEffectSuffix(enchantLevel) + "&7, 伤害提升 &c+" + enchantLevel * 25 + "%";
    }

    public void handleAttackEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(attacker.getUniqueId());
        if (profile.getGenesisData().getTeam() != GenesisTeam.NONE) {

            if (profile.getGenesisData().getTeam() == GenesisTeam.ANGEL) {
                if (attacker.getHealth() <= attacker.getMaxHealth() - enchantLevel) {
                    attacker.setHealth(attacker.getHealth() + enchantLevel);
                } else {
                    attacker.setHealth(attacker.getMaxHealth());
                }
            } else if (profile.getGenesisData().getTeam() == GenesisTeam.DEMON) {
                boostDamage.getAndAdd(enchantLevel * 0.25);
            }

        }
    }

    public void handle(int enchantLevel, Player player) {
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        if (profile.getGenesisData().getTeam() != GenesisTeam.NONE) {
                if (profile.getGenesisData().getTeam() == GenesisTeam.ANGEL) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 80, 0, true, false));
                } else if (profile.getGenesisData().getTeam() == GenesisTeam.DEMON) {
                    if (player.getHealth() > enchantLevel) {
                        player.setHealth(player.getHealth() - (enchantLevel));
                    } else {
                        player.damage(player.getMaxHealth() * 100);
                    }
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 80, enchantLevel - 1, true, false));
                }
        } else {
            player.sendMessage(CC.translate("&c请选择你所依靠的阵容以激活附魔特性!"));
        }
    }

    public int loopTick(int enchantLevel) {
        return 20;
    }
}
