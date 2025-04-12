package cn.charlotte.pit.listener;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.config.NewConfiguration;
import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.data.sub.QuestData;
import cn.charlotte.pit.enchantment.AbstractEnchantment;
import cn.charlotte.pit.enchantment.EnchantmentFactor;
import cn.charlotte.pit.enchantment.param.event.NotPlayerOnly;
import cn.charlotte.pit.enchantment.param.event.PlayerOnly;
import cn.charlotte.pit.enchantment.rarity.EnchantmentRarity;
import cn.charlotte.pit.event.OriginalTimeChangeEvent;
import cn.charlotte.pit.event.PitDamageEvent;
import cn.charlotte.pit.events.EventFactory;
import cn.charlotte.pit.game.Game;
import cn.charlotte.pit.parm.AutoRegister;
import cn.charlotte.pit.parm.listener.*;
import cn.charlotte.pit.perk.AbstractPerk;
import cn.charlotte.pit.perk.PerkFactory;
import cn.charlotte.pit.quest.AbstractQuest;
import cn.charlotte.pit.quest.QuestFactory;
import cn.charlotte.pit.util.PlayerUtil;
import cn.charlotte.pit.util.Utils;
import cn.charlotte.pit.util.chat.CC;
import cn.charlotte.pit.util.item.ItemUtil;
import com.google.common.util.concurrent.AtomicDouble;
import lombok.SneakyThrows;
import net.minecraft.server.v1_8_R3.EnchantmentManager;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.GenericAttributes;
import net.minecraft.server.v1_8_R3.MobEffectList;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

/**
 * @Author: EmptyIrony
 * @Date: 2021/1/2 12:40
 */
@AutoRegister
public class GameEffectListener implements Listener {
    private final DecimalFormat numFormatTwo = new DecimalFormat("0.00");

    @SneakyThrows
    public static void processKilled(IPlayerKilledEntity ins, int level, Player killer, Entity target, AtomicDouble coin, AtomicDouble exp) {
        if (level != -1) {
            final Method method = ins.getClass().getMethod("handlePlayerKilled", int.class, Player.class, Entity.class, AtomicDouble.class, AtomicDouble.class);
            if (method.isAnnotationPresent(PlayerOnly.class)) {
                if (target instanceof Player) {
                    Player player = (Player) target;
                    ins.handlePlayerKilled(level, killer, player, coin, exp);
                }
            } else if (method.isAnnotationPresent(NotPlayerOnly.class)) {
                if (!(target instanceof Player)) {
                    ins.handlePlayerKilled(level, killer, target, coin, exp);
                }
            } else {
                ins.handlePlayerKilled(level, killer, target, coin, exp);
            }
        }
    }

    @SneakyThrows
    public static void processBeKilledByEntity(IPlayerBeKilledByEntity ins, int level, Player myself, Entity target, AtomicDouble coin, AtomicDouble exp) {
        if (level != -1) {
            final Method method = ins.getClass().getMethod("handlePlayerBeKilledByEntity", int.class, Player.class, Entity.class, AtomicDouble.class, AtomicDouble.class);
            if (method.isAnnotationPresent(PlayerOnly.class)) {
                if (target instanceof Player) {
                    Player player = (Player) target;
                    ins.handlePlayerBeKilledByEntity(level, myself, player, coin, exp);
                }
            } else if (method.isAnnotationPresent(NotPlayerOnly.class)) {
                if (!(target instanceof Player)) {
                    ins.handlePlayerBeKilledByEntity(level, myself, target, coin, exp);
                }
            } else {
                ins.handlePlayerBeKilledByEntity(level, myself, target, coin, exp);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerFired(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && event.getCause().equals(EntityDamageEvent.DamageCause.FIRE_TICK)) {
            event.setCancelled(true);
            Player player = (Player) event.getEntity();
            if (player.getHealth() <= event.getFinalDamage()) {
                player.damage(player.getMaxHealth() * 100);
            } else {
                player.setHealth(player.getHealth() - event.getFinalDamage());
            }

            PlayerProfile.getPlayerProfileByUuid(player.getUniqueId())
                    .setNoDamageAnimations(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void modifyLeatherArmor(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            if ("kings_helmet".equals(ItemUtil.getInternalName(((Player) event.getEntity()).getInventory().getHelmet()))) {
                //0.12是每点伤害 铁裤和皮革裤子的减免差距，所以减去0.12*伤害值，以增加皮革裤子为铁裤防御
                event.setDamage(EntityDamageEvent.DamageModifier.ARMOR, event.getDamage(EntityDamageEvent.DamageModifier.ARMOR) - 0.06 * event.getDamage());
            }

            if ("mythic_leggings".equals(ItemUtil.getInternalName(((Player) event.getEntity()).getInventory().getLeggings()))) {
                if (ItemUtil.getItemStringData(((Player) event.getEntity()).getInventory().getLeggings(), "mythic_color").equals("dark")) {
                    return;
                }
                //0.12是每点伤害 铁裤和皮革裤子的减免差距，所以减去0.12*伤害值，以增加皮革裤子为铁裤防御
                event.setDamage(EntityDamageEvent.DamageModifier.ARMOR, event.getDamage(EntityDamageEvent.DamageModifier.ARMOR) - 0.12 * event.getDamage());
            }
        }
    }

    @EventHandler
    public void addEnchantToArrow(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player) {
            StringBuilder sb = new StringBuilder();
            for (IPlayerShootEntity instance : ThePit.getInstance()
                    .getEnchantmentFactor()
                    .getPlayerShootEntities()) {
                final AbstractEnchantment enchant = (AbstractEnchantment) instance;
                final int level = enchant.getItemEnchantLevel(event.getBow());
                if (level > 0) {
                    sb.append(enchant.getNbtName())
                            .append(":")
                            .append(level)
                            .append(";");
                }
            }

            if (sb.length() == 0) {
                return;
            }
            event.getProjectile().setMetadata("enchant", new FixedMetadataValue(ThePit.getInstance(), sb.substring(0, sb.length() - 1)));
        }
    }

    @EventHandler
    public void onTimeChange(OriginalTimeChangeEvent event) {
        CC.boardCast("Time change to: " + event.getTime());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerDamagePlayer(EntityDamageByEntityEvent event) {
        Entity victimEntity = event.getEntity();
        Entity damagerEntity = event.getDamager();
        Player attacker;
        Player victim;
        Arrow arrow = null;
        boolean melee = false;
        boolean bow = false;

        if (!(victimEntity instanceof Player)) return;
        victim = (Player) victimEntity;

        if (damagerEntity instanceof Player) {
            attacker = (Player) damagerEntity;
            melee = true;
        } else if (damagerEntity instanceof Arrow) {
            arrow = (Arrow) damagerEntity;
            if (arrow.getShooter() instanceof Player) {
                attacker = (Player) arrow.getShooter();
                bow = true;
            } else {
                return;
            }
        } else {
            return;
        }

        PerkFactory perkFactory = ThePit.getInstance().getPerkFactory();
        EventFactory eventFactory = ThePit.getInstance().getEventFactory();
        EnchantmentFactor enchantmentFactor = ThePit.getInstance().getEnchantmentFactor();
        QuestFactory questFactory = ThePit.getInstance().getQuestFactory();
        Game game = ThePit.getInstance().getGame();

        PlayerProfile attackerProfile = PlayerProfile.getPlayerProfileByUuid(attacker.getUniqueId());
        PlayerProfile victimProfile = PlayerProfile.getPlayerProfileByUuid(victim.getUniqueId());
        if (attackerProfile == null || victimProfile == null) {
            ThePit.getInstance().getLogger().warning("[GameEffectListener] Attacker or Victim profile is null! Att: " + attacker.getName() + " Vic: " + victim.getName());
            return;
        }

        boolean attackerIsSomber = PlayerUtil.isEquippingSomber(attacker);
        boolean victimIsSomber = PlayerUtil.isEquippingSomber(victim);
        boolean ignoreAttackerStandardEnchants = PlayerUtil.shouldIgnoreEnchant(attacker) || PlayerUtil.shouldIgnoreEnchant(attacker, victim);
        boolean ignoreVictimStandardEnchants = PlayerUtil.shouldIgnoreEnchant(victim) || PlayerUtil.shouldIgnoreEnchant(victim, attacker);
        boolean attackerHasComboVenom = false;
        if (attacker.hasMetadata("combo_venom")) {
            List<MetadataValue> meta = attacker.getMetadata("combo_venom");
            if (!meta.isEmpty()) {
                attackerHasComboVenom = meta.get(0).asLong() > System.currentTimeMillis();
            }
        }

        AtomicDouble finalDamageAdditive = new AtomicDouble(0.0);
        AtomicDouble boostDamageMultiplier = new AtomicDouble(1.0);
        AtomicBoolean cancel = new AtomicBoolean(false);
        double currentDamage = event.getDamage();

        if (melee) {
            if (attacker.getItemInHand() == null || attacker.getItemInHand().getType() == Material.AIR || attacker.getItemInHand().getType() == Material.FISHING_ROD) {
                currentDamage = 1.0;
            } else {
                try {
                    final EntityPlayer entityPlayer = ((CraftPlayer) attacker).getHandle();
                    float baseAttack = (float) entityPlayer.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getValue();
                    if (baseAttack > 9) {
                        currentDamage = 0.0;
                    }
                } catch (Exception e) {
                    ThePit.getInstance().getLogger().log(Level.WARNING, "[GameEffectListener] Error checking NMS attack damage for " + attacker.getName(), e);
                }
            }
        }
        if (NewConfiguration.INSTANCE.getNoobProtect() && attackerProfile.getPrestige() <= 0 && attackerProfile.getLevel() < NewConfiguration.INSTANCE.getNoobProtectLevel()) {
            boostDamageMultiplier.getAndAdd(NewConfiguration.INSTANCE.getNoobDamageBoost() - 1.0);
        }
        if (NewConfiguration.INSTANCE.getNoobProtect() && victimProfile.getPrestige() <= 0 && victimProfile.getLevel() < NewConfiguration.INSTANCE.getNoobProtectLevel()) {
            boostDamageMultiplier.getAndAdd(NewConfiguration.INSTANCE.getNoobDamageReduce() - 1.0);
        }

        if (PlayerUtil.isEquippingAngelChestplate(victim)) {
            boostDamageMultiplier.getAndAdd(-0.1);
        }

        for (IAttackEntity ins : perkFactory.getAttackEntities()) {
            AbstractPerk perk = (AbstractPerk) ins;
            if (game.getDisabledPerks().stream().anyMatch(p -> p.getInternalPerkName().equals(perk.getInternalPerkName())))
                continue;
            int level = perk.getPlayerLevel(attacker);
            processAttackEntity(ins, level, attacker, victim, currentDamage, finalDamageAdditive, boostDamageMultiplier, cancel);
            if (cancel.get()) {
                event.setCancelled(true);
                return;
            }
        }
        for (IAttackEntity ins : questFactory.getAttackEntities()) {
            AbstractQuest quest = (AbstractQuest) ins;
            QuestData currentQuest = attackerProfile.getCurrentQuest();
            if (currentQuest != null && currentQuest.getInternalName().equals(quest.getQuestInternalName())) {
                processAttackEntity(ins, currentQuest.getLevel(), attacker, victim, currentDamage, finalDamageAdditive, boostDamageMultiplier, cancel);
                if (cancel.get()) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
        if (!attackerIsSomber && !victimIsSomber && !attackerHasComboVenom && !ignoreAttackerStandardEnchants) {
            ItemStack mainHand = attacker.getItemInHand();
            ItemStack leggings = attacker.getInventory().getLeggings();
            for (IAttackEntity ins : enchantmentFactor.getAttackEntities()) {
                AbstractEnchantment enchant = (AbstractEnchantment) ins;
                if (enchant.getRarity() == EnchantmentRarity.DARK_NORMAL || enchant.getRarity() == EnchantmentRarity.DARK_RARE)
                    continue;
                int level;
                if (melee) {
                    if (mainHand != null && mainHand.getType() != Material.AIR && mainHand.getType() != Material.LEATHER_LEGGINGS) {
                        level = enchant.getItemEnchantLevel(mainHand);
                        processAttackEntity(ins, level, attacker, victim, currentDamage, finalDamageAdditive, boostDamageMultiplier, cancel);
                        if (cancel.get()) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                    if (leggings != null && leggings.getType() != Material.AIR) {
                        level = enchant.getItemEnchantLevel(leggings);
                        processAttackEntity(ins, level, attacker, victim, currentDamage, finalDamageAdditive, boostDamageMultiplier, cancel);
                        if (cancel.get()) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }

        for (IPlayerDamaged ins : perkFactory.getPlayerDamageds()) {
            AbstractPerk perk = (AbstractPerk) ins;
            if (game.getDisabledPerks().stream().anyMatch(p -> p.getInternalPerkName().equals(perk.getInternalPerkName())))
                continue;
            int level = perk.getPlayerLevel(victim);
            processPlayerDamaged(ins, level, victim, attacker, currentDamage, finalDamageAdditive, boostDamageMultiplier, cancel);
            if (cancel.get()) {
                event.setCancelled(true);
                return;
            }
        }
        for (IPlayerDamaged ins : questFactory.getPlayerDamageds()) {
            AbstractQuest quest = (AbstractQuest) ins;
            QuestData currentQuest = victimProfile.getCurrentQuest();
            if (currentQuest != null && currentQuest.getInternalName().equals(quest.getQuestInternalName())) {
                processPlayerDamaged(ins, currentQuest.getLevel(), victim, attacker, currentDamage, finalDamageAdditive, boostDamageMultiplier, cancel);
                if (cancel.get()) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
        if (!attackerIsSomber && !victimIsSomber && !attackerHasComboVenom && !ignoreVictimStandardEnchants) {
            ItemStack victimMainHand = victim.getItemInHand();
            ItemStack victimLeggings = victim.getInventory().getLeggings();
            for (IPlayerDamaged ins : enchantmentFactor.getPlayerDamageds()) {
                AbstractEnchantment enchant = (AbstractEnchantment) ins;
                if (enchant.getRarity() == EnchantmentRarity.DARK_NORMAL || enchant.getRarity() == EnchantmentRarity.DARK_RARE)
                    continue;
                int level;
                if (victimMainHand != null && victimMainHand.getType() != Material.AIR && victimMainHand.getType() != Material.LEATHER_LEGGINGS) {
                    level = enchant.getItemEnchantLevel(victimMainHand);
                    processPlayerDamaged(ins, level, victim, attacker, currentDamage, finalDamageAdditive, boostDamageMultiplier, cancel);
                    if (cancel.get()) {
                        event.setCancelled(true);
                        return;
                    }
                }
                if (victimLeggings != null && victimLeggings.getType() != Material.AIR) {
                    level = enchant.getItemEnchantLevel(victimLeggings);
                    processPlayerDamaged(ins, level, victim, attacker, currentDamage, finalDamageAdditive, boostDamageMultiplier, cancel);
                    if (cancel.get()) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }

        if (bow && arrow != null) {
            List<MetadataValue> data = arrow.getMetadata("enchant");
            String enchantData = (data != null && !data.isEmpty()) ? data.get(0).asString() : null;

            for (IPlayerShootEntity ins : perkFactory.getPlayerShootEntities()) {
                AbstractPerk perk = (AbstractPerk) ins;
                if (game.getDisabledPerks().stream().anyMatch(p -> p.getInternalPerkName().equals(perk.getInternalPerkName())))
                    continue;
                int level = perk.getPlayerLevel(attacker);
                processShootEntity(ins, level, attacker, victim, arrow, currentDamage, finalDamageAdditive, boostDamageMultiplier, cancel);
                if (cancel.get()) {
                    event.setCancelled(true);
                    return;
                }
            }
            for (IPlayerShootEntity ins : questFactory.getPlayerShootEntities()) {
                AbstractQuest quest = (AbstractQuest) ins;
                QuestData currentQuest = attackerProfile.getCurrentQuest();
                if (currentQuest != null && currentQuest.getInternalName().equals(quest.getQuestInternalName())) {
                    processShootEntity(ins, currentQuest.getLevel(), attacker, victim, arrow, currentDamage, finalDamageAdditive, boostDamageMultiplier, cancel);
                    if (cancel.get()) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
            if (enchantData != null && !attackerIsSomber && !victimIsSomber && !attackerHasComboVenom && !ignoreAttackerStandardEnchants) {
                final String[] split = enchantData.split(";");
                for (String enchantStr : split) {
                    final String[] enchStr = enchantStr.split(":");
                    if (enchStr.length == 2) {
                        final String enchantName = enchStr[0];
                        final int level = Integer.parseInt(enchStr[1]);
                        final AbstractEnchantment enchantment = enchantmentFactor.getEnchantmentMap().get(enchantName);
                        if (enchantment instanceof IPlayerShootEntity) {
                            if (enchantment.getRarity() == EnchantmentRarity.DARK_NORMAL || enchantment.getRarity() == EnchantmentRarity.DARK_RARE)
                                continue;
                            processShootEntity((IPlayerShootEntity) enchantment, level, attacker, victim, arrow, currentDamage, finalDamageAdditive, boostDamageMultiplier, cancel);
                            if (cancel.get()) {
                                event.setCancelled(true);
                                return;
                            }
                        }
                    }
                }
            }
        }

        if (cancel.get()) {
            event.setCancelled(true);
            return;
        }

        double calculatedDamage = (currentDamage + finalDamageAdditive.get()) * boostDamageMultiplier.get();
        calculatedDamage = Math.max(0.0, calculatedDamage);

        event.setDamage(calculatedDamage);
        final double finalAppliedDamage = event.getDamage();

        new PitDamageEvent(attacker, finalAppliedDamage, currentDamage).callEvent();

        if (victim.getInventory().getLeggings() != null) {
            int enchantLevel = Utils.getEnchantLevel(victim.getInventory().getLeggings(), "Mirror");
            double damageForMirror = finalDamageAdditive.get();
            if (enchantLevel > 1 && damageForMirror > 0 && damageForMirror < 1000) {
                if (melee && (!victim.hasMetadata("mirror_latest_active") || (!victim.getMetadata("mirror_latest_active").isEmpty() && System.currentTimeMillis() - victim.getMetadata("mirror_latest_active").get(0).asLong() > 500L))) {
                    victim.setMetadata("mirror_latest_active", new FixedMetadataValue(ThePit.getInstance(), System.currentTimeMillis()));
                    if (!victim.getUniqueId().equals(attacker.getUniqueId())) {
                        attacker.damage(0.01, victim);
                    }
                    float mirrorDamage = (float) (((enchantLevel * 25 - 25) * 0.01) * damageForMirror);
                    attacker.setHealth(Math.max(0.1, attacker.getHealth() - mirrorDamage));
                }
            }
            if (enchantLevel > 0 && damageForMirror > 0 && damageForMirror < 1000) {
                finalDamageAdditive.set(0.0);
                calculatedDamage = (currentDamage + finalDamageAdditive.get()) * boostDamageMultiplier.get();
                calculatedDamage = Math.max(0.0, calculatedDamage);
                event.setDamage(calculatedDamage);
            }
        }
        victim.setLastDamageCause(event);
        try {
            ((CraftPlayer) victim).getHandle().killer = ((CraftPlayer) attacker).getHandle();
        } catch (Exception e) { /* Ignore potential cast errors */ }

        if (!event.isCancelled() && victim.getHealth() - event.getDamage() <= 0) {
            AtomicDouble killCoin = new AtomicDouble(0);
            AtomicDouble killExp = new AtomicDouble(0);
            for (IPlayerKilledEntity ins : perkFactory.getPlayerKilledEntities()) {
                if (ins instanceof AbstractPerk) {
                    AbstractPerk perk = (AbstractPerk) ins;
                    int level = perk.getPlayerLevel(attacker);
                    processKilled(ins, level, attacker, victim, killCoin, killExp);
                } else {
                    processKilled(ins, -1, attacker, victim, killCoin, killExp);
                }
            }
            for (IPlayerKilledEntity ins : enchantmentFactor.getPlayerKilledEntities()) {
                int level = -1;
                processKilled(ins, level, attacker, victim, killCoin, killExp);
            }
            AtomicDouble deathCoin = new AtomicDouble(0);
            AtomicDouble deathExp = new AtomicDouble(0);
            for (IPlayerBeKilledByEntity ins : perkFactory.getPlayerBeKilledByEntities()) {
                if (ins instanceof AbstractPerk) {
                    AbstractPerk perk = (AbstractPerk) ins;
                    int level = perk.getPlayerLevel(victim);
                    processBeKilledByEntity(ins, level, victim, attacker, deathCoin, deathExp);
                } else {
                    processBeKilledByEntity(ins, -1, victim, attacker, deathCoin, deathExp);
                }
            }
            for (IPlayerBeKilledByEntity ins : enchantmentFactor.getPlayerBeKilledByEntities()) {
                int level = -1;
                processBeKilledByEntity(ins, level, victim, attacker, deathCoin, deathExp);
            }
        }

        if (attackerProfile.getPlayerOption().isDebugDamageMessage()) {
            attacker.sendMessage(CC.translate("&7造成伤害(Damage/Final Damage): &c" + numFormatTwo.format(currentDamage) + "&7/&c" + numFormatTwo.format(event.getDamage())));
            try {
                final EntityPlayer entityPlayer = ((CraftPlayer) attacker).getHandle();
                final double value = entityPlayer.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getValue();
                final float enchantDamage = EnchantmentManager.a(entityPlayer.bA(), ((CraftLivingEntity) victim).getHandle().getMonsterType());
                final boolean critical = entityPlayer.fallDistance > 0.0F && !entityPlayer.onGround && !entityPlayer.k_() && !entityPlayer.V() && !entityPlayer.hasEffect(MobEffectList.BLINDNESS) && entityPlayer.vehicle == null;
                attacker.sendMessage(CC.translate("&7基础: &c" + value + "&7,附魔伤害: &c" + enchantDamage + "&7,暴击: &c" + critical));
            } catch (Exception e) {/* ignore */}
        }
        if (victimProfile.getPlayerOption().isDebugDamageMessage()) {
            victim.sendMessage(CC.translate("&7受到伤害(Damage/Final Damage): &c" + numFormatTwo.format(currentDamage) + "&7/&c" + numFormatTwo.format(event.getDamage())));
        }
    }

    @EventHandler
    public void onItemDamaged(PlayerItemDamageEvent event) {
        ItemStack item = event.getItem();
        if (item == null || item.getType() == Material.AIR) {
            return;
        }
        AtomicBoolean atomicBoolean = new AtomicBoolean();
        for (IItemDamage itemDamage : ThePit.getInstance()
                .getEnchantmentFactor()
                .getiItemDamages()) {
            int level = ((AbstractEnchantment) itemDamage).getItemEnchantLevel(event.getPlayer().getItemInHand());

            itemDamage.handleItemDamaged(level, event.getItem(), event.getPlayer(), atomicBoolean);
        }
        if (atomicBoolean.get()) {
            event.setCancelled(true);
        }
    }

    @SneakyThrows
    private void processShootEntity(IPlayerShootEntity ins, int level, Player damager, Entity target, Entity damageSource, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        if (level != -1) {
            Method handleMethod = ins.getClass().getMethod("handleShootEntity", int.class, Player.class, Entity.class, double.class, AtomicDouble.class, AtomicDouble.class, AtomicBoolean.class);
            if (handleMethod.isAnnotationPresent(PlayerOnly.class) || handleMethod.isAnnotationPresent(NotPlayerOnly.class)) {
                if (target instanceof Player) {
                    Player player = (Player) target;
                    ins.handleShootEntity(level, damager, player, damage, finalDamage, boostDamage, cancel);
                }
            } else {
                ins.handleShootEntity(level, damager, target, damage, finalDamage, boostDamage, cancel);
            }
        }
    }

    @SneakyThrows
    private void processPlayerDamaged(IPlayerDamaged ins, int level, Player player, Entity damager, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        if (level != -1) {
            final Method method = ins.getClass().getMethod("handlePlayerDamaged", int.class, Player.class, Entity.class, double.class, AtomicDouble.class, AtomicDouble.class, AtomicBoolean.class);
            if (method.isAnnotationPresent(PlayerOnly.class) || method.isAnnotationPresent(NotPlayerOnly.class)) {
                if (damager instanceof Player) {
                    Player damagerPlayer = (Player) damager;
                    ins.handlePlayerDamaged(level, player, damagerPlayer, damage, finalDamage, boostDamage, cancel);
                }
            } else {
                ins.handlePlayerDamaged(level, player, damager, damage, finalDamage, boostDamage, cancel);
            }
        }
    }

    @SneakyThrows
    private void processAttackEntity(IAttackEntity ins, int level, Player damager, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        if (level != -1) {
            final Method method = ins.getClass().getMethod("handleAttackEntity", int.class, Player.class, Entity.class, double.class, AtomicDouble.class, AtomicDouble.class, AtomicBoolean.class);
            if (method.isAnnotationPresent(PlayerOnly.class) || method.isAnnotationPresent(NotPlayerOnly.class)) {
                if (target instanceof Player) {
                    Player player = (Player) target;
                    ins.handleAttackEntity(level, damager, player, damage, finalDamage, boostDamage, cancel);
                }
            } else {
                ins.handleAttackEntity(level, damager, target, damage, finalDamage, boostDamage, cancel);
            }
        }
    }
}