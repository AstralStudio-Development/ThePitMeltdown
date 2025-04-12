package cn.charlotte.pit.util.hologram;

import cn.charlotte.pit.ThePit;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

/**
 * @Author: EmptyIrony
 * @Date: 2021/3/13 19:35
 */
//@AutoRegister
public class HologramListeners implements Listener {

    private void spawnHologramsForPlayer(Player player, Iterable<Hologram> hologramsToCheck) {
        if (player == null || !player.isOnline() || hologramsToCheck == null) return;
        ArrayList<Player> receiverList = new ArrayList<>(Collections.singletonList(player));

        for (Hologram h : hologramsToCheck) {
            if (h != null && h.isSpawned() && h.getLocation() != null && h.getLocation().getWorld().equals(player.getWorld())) {
                try {
                    HologramAPI.spawn(h, receiverList);
                } catch (Exception e1) {
                    ThePit.getInstance().getLogger().warning("[HologramListeners] Error spawning hologram " + h.hashCode() + " for player " + player.getName() + " on event: " + e1.getMessage());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {
        Player p = e.getPlayer();
        Location to = e.getTo();
        Location from = e.getFrom();

        if (to == null || to.getWorld() == null) return;
        if (from != null && from.getWorld() != null && from.getWorld().equals(to.getWorld()) &&
            (from.getBlockX() >> 4) == (to.getBlockX() >> 4) &&
            (from.getBlockZ() >> 4) == (to.getBlockZ() >> 4)) {
            return;
        }

        Set<Hologram> hologramsInChunk = HologramAPI.getHologramsInChunk(to.getWorld(), to.getBlockX() >> 4, to.getBlockZ() >> 4);
        spawnHologramsForPlayer(p, hologramsInChunk);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent e) {
        Player p = e.getPlayer();
        Location loc = p.getLocation();
        Set<Hologram> hologramsInChunk = HologramAPI.getHologramsInChunk(loc.getWorld(), loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
        spawnHologramsForPlayer(p, hologramsInChunk);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent e) {
        Chunk chunk = e.getChunk();
        if (chunk == null) return;
        Set<Hologram> hologramsInChunk = HologramAPI.getHologramsInChunk(chunk);
        if (hologramsInChunk.isEmpty()) return;

        ArrayList<Player> playersInWorld = new ArrayList<>(chunk.getWorld().getPlayers());
        if (playersInWorld.isEmpty()) return;

        for (Hologram h : hologramsInChunk) {
            if (h != null && h.isSpawned()) {
                try {
                    HologramAPI.spawn(h, playersInWorld);
                } catch (Exception e1) {
                    ThePit.getInstance().getLogger().warning("[HologramListeners] Error spawning hologram " + h.hashCode() + " on chunk load: " + e1.getMessage());
                }
            }
        }
    }

}
