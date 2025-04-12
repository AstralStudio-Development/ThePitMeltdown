package cn.charlotte.pit.util.bossbar;

import cn.charlotte.pit.ThePit;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BossBar {
    private final ConcurrentHashMap<UUID, EntityWither> withers = new ConcurrentHashMap<>();
    private String title;

    public BossBar(String title) {
        this.title = title;
    }

    public void addPlayer(Player player) {
        if (player == null || !player.isOnline()) return;
        final UUID playerUUID = player.getUniqueId();

        EntityWither wither = new EntityWither(((CraftWorld) player.getWorld()).getHandle());
        Location location = getWitherLocation(player.getLocation());
        wither.setCustomName(title);
        wither.setInvisible(true);
        wither.setLocation(location.getX(), location.getY(), location.getZ(), 0.0F, 0.0F);

        withers.put(playerUUID, wither);

        final PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(wither);

        if (Bukkit.isPrimaryThread()) {
            sendPacketInternal(player, packet);
        } else {
            Bukkit.getScheduler().runTask(ThePit.getInstance(), () -> sendPacketInternal(player, packet));
        }
    }

    public void removePlayer(Player player) {
        if (player == null) return;
        final UUID playerUUID = player.getUniqueId();
        EntityWither wither = withers.remove(playerUUID);
        if (wither == null) return;

        final PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(wither.getId());

        if (Bukkit.isPrimaryThread()) {
            sendPacketInternal(player, packet);
        } else {
            Bukkit.getScheduler().runTask(ThePit.getInstance(), () -> {
                Player onlinePlayer = Bukkit.getPlayer(playerUUID);
                if (onlinePlayer != null && onlinePlayer.isOnline()) {
                    sendPacketInternal(onlinePlayer, packet);
                }
            });
        }
    }

    private void sendPacketInternal(Player player, Packet<?> packet) {
        if (player != null && player.isOnline() && packet != null) {
            try {
                ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
            } catch (Exception e) {
                ThePit.getInstance().getLogger().warning("[BossBar] Failed to send packet " + packet.getClass().getSimpleName() + " to " + player.getName() + ": " + e.getMessage());
            }
        }
    }

    public void setProgress(double progress) {
        final double finalProgress = Math.max(0.0, Math.min(1.0, progress));

        for (UUID uuid : withers.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            EntityWither wither = withers.get(uuid);
            if (player != null && player.isOnline() && wither != null) {
                wither.setHealth((float) (finalProgress * wither.getMaxHealth()));
                final PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(
                        wither.getId(), wither.getDataWatcher(), true);
                if (Bukkit.isPrimaryThread()) {
                    sendPacketInternal(player, packet);
                } else {
                    final UUID finalUUID = uuid;
                    Bukkit.getScheduler().runTask(ThePit.getInstance(), () -> {
                        Player onlinePlayer = Bukkit.getPlayer(finalUUID);
                        EntityWither currentWither = withers.get(finalUUID);
                        if (onlinePlayer != null && onlinePlayer.isOnline() && currentWither != null) {
                            sendPacketInternal(onlinePlayer, packet);
                        }
                    });
                }
            }
        }
    }

    public void setTitle(String title) {
        if (Objects.equals(this.title, title)) return;
        this.title = title;

        for (UUID uuid : withers.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            EntityWither wither = withers.get(uuid);
            if (player != null && player.isOnline() && wither != null) {
                wither.setCustomName(title);
                final PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(wither.getId(), wither.getDataWatcher(), true);
                if (Bukkit.isPrimaryThread()) {
                    sendPacketInternal(player, packet);
                } else {
                    final UUID finalUUID = uuid;
                    Bukkit.getScheduler().runTask(ThePit.getInstance(), () -> {
                        Player onlinePlayer = Bukkit.getPlayer(finalUUID);
                        EntityWither currentWither = withers.get(finalUUID);
                        if (onlinePlayer != null && onlinePlayer.isOnline() && currentWither != null) {
                            sendPacketInternal(onlinePlayer, packet);
                        }
                    });
                }
            }
        }
    }

    public void update() {
        for (UUID uuid : withers.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            EntityWither wither = withers.get(uuid);
            if (player != null && player.isOnline() && wither != null) {
                updateWitherLocationAndSendPacket(player, wither);
            }
        }
    }

    public void update(Player player) {
        if (player == null) return;
        EntityWither wither = this.withers.get(player.getUniqueId());
        if (wither != null) {
            updateWitherLocationAndSendPacket(player, wither);
        }
    }

    private void updateWitherLocationAndSendPacket(Player player, EntityWither wither) {
        Location location = getWitherLocation(player.getLocation());
        wither.setLocation(location.getX(), location.getY(), location.getZ(), 0.0F, 0.0F);
        PacketPlayOutEntityTeleport packet = new PacketPlayOutEntityTeleport(wither);
        sendPacketInternal(player, packet);
    }

    public Location getWitherLocation(Location location) {
        return location.clone().add(location.getDirection().multiply(60));
    }

    public String getTitle() {
        return this.title;
    }

    public ConcurrentHashMap<UUID, EntityWither> getWithers() {
        return this.withers;
    }
}
