package cn.charlotte.pit.events.impl;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.config.NewConfiguration;
import cn.charlotte.pit.event.PitKillEvent;
import cn.charlotte.pit.events.IEvent;
import cn.charlotte.pit.events.INormalEvent;
import cn.charlotte.pit.parm.AutoRegister;
import cn.charlotte.pit.util.chat.CC;
import cn.charlotte.pit.util.hologram.Hologram;
import cn.charlotte.pit.util.hologram.HologramAPI;
import lombok.Data;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@AutoRegister
public class KingOfTheHillEvent implements IEvent, INormalEvent, Listener {

    private final HashMap<Block, Material> blocks = new HashMap<>();
    private final HashMap<Block, Byte> blockData = new HashMap<>();
    @Getter
    private static HillData hillData;
    private boolean isActive = false;

    @Override
    public String getEventInternalName() {
        return "koth";
    }

    @Override
    public String getEventName() {
        return "&b&l占山为王";
    }

    @Override
    public int requireOnline() {
        return NewConfiguration.INSTANCE.getEventOnlineRequired().get(getEventInternalName());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if ("koth".equals(ThePit.getInstance().getEventFactory().getActiveNormalEventName())) {
            hillData.getFirstHologram().spawn(Collections.singletonList(event.getPlayer()));
            hillData.getSecondHologram().spawn(Collections.singletonList(event.getPlayer()));
            hillData.getThirdHologram().spawn(Collections.singletonList(event.getPlayer()));
        }
    }

    @Override
    public void onActive() {
        isActive = true;
        hillData = new HillData();
        Location location = ThePit.getInstance().getPitConfig().getKothLoc();
        if (location == null) {
            Bukkit.broadcastMessage(CC.translate("&c&l占山为王！ &7活动区域未设置，请联系管理员设置！"));
            ThePit.getInstance().getEventFactory().inactiveEvent(this);
            return;
        }
        Bukkit.getPluginManager().registerEvents(this, ThePit.getInstance());
        Bukkit.broadcastMessage(CC.translate("&b&l占山为王！ &7现在你可以在指定区域内获得四倍的&b经验&7与&6硬币&7加成！"));

        spawnKothCircle(location, 7, Material.GOLD_BLOCK);

        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                ThePit.getInstance().getEventFactory().inactiveEvent(KingOfTheHillEvent.this);
            }
        };
        runnable.runTaskLater(ThePit.getInstance(), 20 * 60 * 4);
    }

    @Override
    public void onInactive() {
        if (!isActive) return;
        isActive = false;
        despawnKothCircle();
        HandlerList.unregisterAll(this);
        hillData = null;
    }

    @EventHandler
    public void onKill(PitKillEvent event) {
        Player player = event.getKiller();
        if (isActive) {
            if (isRegion(player)) {
                event.setCoins(event.getCoins() * 4);
                event.setExp(event.getExp() * 4);
            }
        }
    }

    public void spawnKothCircle(Location var1, int var2, Material var3) {
        int var4 = var2 + 1;
        int var5 = var1.getBlockX();
        int var6 = var1.getBlockY();
        int var7 = var1.getBlockZ();
        World var8 = var1.getWorld();
        int var9 = var4 * var4;

        this.blocks.clear();
        this.blockData.clear();

        for (int var10 = var5 - var4; var10 <= var5 + var4; ++var10) {
            for (int var11 = var7 - var4; var11 <= var7 + var4; ++var11) {
                if ((var5 - var10) * (var5 - var10) + (var7 - var11) * (var7 - var11) <= var9) {
                    Location var12 = new Location(var8, var10, var6, var11);
                    Block currentBlock = var8.getBlockAt(var10, var6, var11);
                    this.blocks.put(currentBlock, currentBlock.getType());
                    this.blockData.put(currentBlock, currentBlock.getData());
                    currentBlock.setType(var3);
                }
            }
        }

        Block centerGlass = var8.getBlockAt(var1);
        this.blocks.put(centerGlass, centerGlass.getType());
        this.blockData.put(centerGlass, centerGlass.getData());
        centerGlass.setType(Material.STAINED_GLASS);
        centerGlass.setData((byte) 3);

        Block centerBeacon = var1.clone().add(0.0, -1.0, 0.0).getBlock();
        this.blocks.put(centerBeacon, centerBeacon.getType());
        this.blockData.put(centerBeacon, centerBeacon.getData());
        centerBeacon.setType(Material.BEACON);

        Location holoBaseLoc = centerBeacon.getLocation().clone().add(0.5, 0.0, 0.5);
        hillData.setFirstHologram(HologramAPI.createHologram(holoBaseLoc.clone().add(0, 5.0, 0), CC.translate("&b&l占山为王")));
        hillData.setSecondHologram(HologramAPI.createHologram(holoBaseLoc.clone().add(0, 4.5, 0), CC.translate("&e&l4x &b经验值&e/&6硬币")));
        hillData.getFirstHologram().spawn();
        hillData.getSecondHologram().spawn();

        for (int var18 = -1; var18 <= 1; ++var18) {
            for (int var19 = -1; var19 <= 1; ++var19) {
                Block platformBlock = var1.clone().add(var18, -2.0, var19).getBlock();
                this.blocks.put(platformBlock, platformBlock.getType());
                this.blockData.put(platformBlock, platformBlock.getData());
                platformBlock.setType(var3);
            }
        }
    }

    public void despawnKothCircle() {
        if (hillData != null) {
            if (hillData.getFirstHologram() != null) hillData.getFirstHologram().deSpawn();
            if (hillData.getSecondHologram() != null) hillData.getSecondHologram().deSpawn();
            if (hillData.getThirdHologram() != null) hillData.getThirdHologram().deSpawn();
        }
        for (Map.Entry<Block, Material> entry : this.blocks.entrySet()) {
            Block block = entry.getKey();
            Material originalType = entry.getValue();
            Byte originalData = this.blockData.get(block);
            if (originalData != null) {
                block.setType(originalType);
                block.setData(originalData);
            } else {
                block.setType(originalType);
            }
        }
        this.blocks.clear();
        this.blockData.clear();
    }

    public boolean isRegion(Player player) {
        Location location = player.getLocation();
        Material type1 = location.clone().add(0, -1, 0).getBlock().getType();
        if (type1 == Material.GOLD_BLOCK) return true;
        Material type2 = location.clone().add(0, -2, 0).getBlock().getType();
        return type2 == Material.GOLD_BLOCK;
    }

    @Data
    public static class HillData {
        private Hologram firstHologram;
        private Hologram secondHologram;
        private Hologram thirdHologram;
    }

}
