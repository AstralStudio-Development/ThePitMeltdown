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

        Bukkit.getScheduler().runTaskLater(ThePit.getInstance(), () -> 
                ThePit.getInstance().getEventFactory().inactiveEvent(KingOfTheHillEvent.this), 20 * 60 * 4);
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

    public void spawnKothCircle(Location location, int range, Material type) {
        int newRange = range + 1;
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        World world = location.getWorld();
        int rangeSquare = newRange * newRange;

        this.blocks.clear();
        this.blockData.clear();

        for (int newX = x - newRange; newX <= x + newRange; ++newX) {
            for (int var11 = z - newRange; var11 <= z + newRange; ++var11) {
                if ((x - newX) * (x - newX) + (z - var11) * (z - var11) <= rangeSquare) {
                    Location var12 = new Location(world, newX, y, var11);
                    Block currentBlock = world.getBlockAt(newX, y, var11);
                    this.blocks.put(currentBlock, currentBlock.getType());
                    this.blockData.put(currentBlock, currentBlock.getData());
                    currentBlock.setType(type);
                }
            }
        }

        Block centerGlass = world.getBlockAt(location);
        this.blocks.put(centerGlass, centerGlass.getType());
        this.blockData.put(centerGlass, centerGlass.getData());
        centerGlass.setType(Material.STAINED_GLASS);
        centerGlass.setData((byte) 3);

        Block centerBeacon = location.clone().add(0.0, -1.0, 0.0).getBlock();
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
                Block platformBlock = location.clone().add(var18, -2.0, var19).getBlock();
                this.blocks.put(platformBlock, platformBlock.getType());
                this.blockData.put(platformBlock, platformBlock.getData());
                platformBlock.setType(type);
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
            block.setType(originalType);
            if (originalData != null) {
                block.setData(originalData);
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
