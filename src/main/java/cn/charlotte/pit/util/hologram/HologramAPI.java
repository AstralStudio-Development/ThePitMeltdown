package cn.charlotte.pit.util.hologram;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.util.hologram.reflection.NMUClass;
import cn.charlotte.pit.util.hologram.reflection.Reflection;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.World;
import org.bukkit.Chunk;
import org.inventivetalent.reflection.minecraft.Minecraft;
import org.inventivetalent.reflection.util.AccessUtil;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;

@SuppressWarnings("unchecked")
public abstract class HologramAPI {

    // Keep track of holograms per world and chunk for faster lookup
    // World UUID -> Chunk Key (Long) -> Set<Hologram>
    private static final Map<UUID, Map<Long, Set<Hologram>>> hologramIndex = new ConcurrentHashMap<>();
    // Keep the original list for getHolograms() compatibility, but manage through index preferably
    protected static final List<Hologram> holograms = Collections.synchronizedList(new ArrayList<>()); // Use synchronized list for basic safety

    protected static boolean is1_8/*Or 1.9*/ = Minecraft.VERSION.newerThan(Minecraft.Version.v1_8_R1);
    protected static boolean packetsEnabled = false;
    protected static boolean useProtocolSupport = false;
    //Protocol Support
    static Class<?> ProtocolSupportAPI;
    static Class<?> ProtocolVersion;
    static Method ProtocolSupportAPI_getProtocolVersion;
    static Method ProtocolVersion_getId;

    // Helper to create a unique key for a chunk
    private static long getChunkKey(int chunkX, int chunkZ) {
        return (long)chunkX << 32 | chunkZ & 0xFFFFFFFFL;
    }
    private static long getChunkKey(Chunk chunk) {
        return getChunkKey(chunk.getX(), chunk.getZ());
    }
    private static long getChunkKey(Location location) {
        return getChunkKey(location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    // Add a hologram to the spatial index
    private static void addHologramToIndex(Hologram hologram) {
        if (hologram == null || hologram.getLocation() == null || hologram.getLocation().getWorld() == null) return;
        UUID worldId = hologram.getLocation().getWorld().getUID();
        long chunkKey = getChunkKey(hologram.getLocation());

        hologramIndex
            .computeIfAbsent(worldId, k -> new ConcurrentHashMap<>()) // Get or create world map
            .computeIfAbsent(chunkKey, k -> Collections.newSetFromMap(new ConcurrentHashMap<>())) // Get or create chunk set (thread-safe set)
            .add(hologram);
    }

    // Remove a hologram from the spatial index
    private static void removeHologramFromIndex(Hologram hologram) {
        if (hologram == null || hologram.getLocation() == null || hologram.getLocation().getWorld() == null) return;
        UUID worldId = hologram.getLocation().getWorld().getUID();
        long chunkKey = getChunkKey(hologram.getLocation());

        Map<Long, Set<Hologram>> worldMap = hologramIndex.get(worldId);
        if (worldMap != null) {
            Set<Hologram> chunkSet = worldMap.get(chunkKey);
            if (chunkSet != null) {
                chunkSet.remove(hologram);
                // Clean up empty sets/maps
                if (chunkSet.isEmpty()) {
                    worldMap.remove(chunkKey);
                    if (worldMap.isEmpty()) {
                        hologramIndex.remove(worldId);
                    }
                }
            }
        }
    }

    // Get holograms within a specific chunk
    public static Set<Hologram> getHologramsInChunk(World world, int chunkX, int chunkZ) {
        if (world == null) return Collections.emptySet();
        Map<Long, Set<Hologram>> worldMap = hologramIndex.get(world.getUID());
        if (worldMap == null) return Collections.emptySet();
        long chunkKey = getChunkKey(chunkX, chunkZ);
        return worldMap.getOrDefault(chunkKey, Collections.emptySet());
    }
     public static Set<Hologram> getHologramsInChunk(Chunk chunk) {
         if (chunk == null) return Collections.emptySet();
         return getHologramsInChunk(chunk.getWorld(), chunk.getX(), chunk.getZ());
     }

    /**
     * Creates a new {@link Hologram}
     *
     * @param loc  {@link Location} to spawn the hologram at
     * @param text Initial text content of the hologram
     * @return a new {@link Hologram}
     */
    public static Hologram createHologram(Location loc, String text) {
        // PitHook might be null if API is used standalone? Add check.
        Hologram hologram = ThePit.api != null ? ThePit.api.createHologram(loc, text) : null;
        // Assuming createHologram returns a valid hologram (e.g., DefaultHologram)
        if (hologram != null) {
            holograms.add(hologram); // Add to legacy list
            addHologramToIndex(hologram); // Add to new index
        } else {
             ThePit.getInstance().getLogger().warning("[HologramAPI] Failed to create hologram instance.");
        }
        return hologram;
    }

    /**
     * Removes a {@link Hologram}
     *
     * @param loc  {@link Location} of the hologram
     * @param text content of the hologram
     * @return <code>true</code> if a hologram was found and has been removed, <code>false</code> otherwise
     */
    public static boolean removeHologram(Location loc, String text) {
        // Iterate over legacy list to find match (less efficient)
        // TODO: Consider adding lookup by Location/Text to index if needed often
        Hologram toRemove = null;
        // Use iterator for safe removal from synchronized list
        synchronized (holograms) {
             Iterator<Hologram> iterator = holograms.iterator();
             while (iterator.hasNext()) {
                 Hologram h = iterator.next();
                 // Add null checks for safety
                 if (h.getLocation() != null && loc != null && h.getLocation().equals(loc) && Objects.equals(h.getText(), text)) {
                     toRemove = h;
                     iterator.remove(); // Remove using iterator
                     break;
                 }
             }
        }
        if (toRemove != null) {
            removeHologramFromIndex(toRemove); // Remove from index
            if (toRemove.isSpawned()) {
                toRemove.deSpawn(); // Despawn if needed
            }
            return true;
        }
        return false;
    }

    /**
     * Removes a {@link Hologram}
     *
     * @param hologram {@link Hologram} to remove
     * @return <code>true</code> if the hologram has been removed
     */
    public static boolean removeHologram(@Nonnull Hologram hologram) {
        boolean removed = false;
        // Use iterator for safe removal from synchronized list
        synchronized (holograms) {
            removed = holograms.remove(hologram);
        }
        if (removed) {
            removeHologramFromIndex(hologram); // Remove from index
            if (hologram.isSpawned()) {
                hologram.deSpawn();
            }
        }
        return removed;
    }

    /**
     * @return {@link Collection} of all registered {@link Hologram}s
     */
    public static Collection<Hologram> getHolograms() {
        // Return copy of the legacy list for compatibility
        synchronized (holograms) {
             return new ArrayList<>(holograms);
        }
    }

    protected static boolean spawn(@Nonnull final Hologram hologram, final Collection<? extends Player> receivers) throws Exception {
        checkReceiverWorld(hologram, receivers);
        if (!receivers.isEmpty()) {
            ((CraftHologram) hologram).sendSpawnPackets(receivers, true, true);
            ((CraftHologram) hologram).sendTeleportPackets(receivers, true, true);
            ((CraftHologram) hologram).sendNamePackets(receivers);
            ((CraftHologram) hologram).sendAttachPacket(receivers);
        }
        return true;
    }

    protected static boolean despawn(@Nonnull Hologram hologram, Collection<? extends Player> receivers) throws Exception {
        if (receivers.isEmpty()) {
            return false;
        }

        ((CraftHologram) hologram).sendDestroyPackets(receivers);
        return true;
    }

    protected static void sendPacket(Player p, Object packet) {
        if (p == null || packet == null) {
            throw new IllegalArgumentException("player and packet cannot be null");
        }
        try {
            Object handle = Reflection.getHandle(p);
            Object connection = Reflection.getFieldWithException(handle.getClass(), "playerConnection").get(handle);
            Reflection.getMethod(connection.getClass(), "sendPacket", Reflection.getNMSClass("Packet")).invoke(connection, packet);
        } catch (Exception e) {
        }
    }

    protected static void checkReceiverWorld(final Hologram hologram, final Collection<? extends Player> receivers) {
        if (hologram == null || hologram.getLocation() == null || hologram.getLocation().getWorld() == null || receivers == null) return;
        World hologramWorld = hologram.getLocation().getWorld();
        // Use iterator for safe removal while iterating
        receivers.removeIf(player -> player == null || !player.getWorld().equals(hologramWorld));
    }

    protected static int getVersion(Player p) {
        try {
            if (useProtocolSupport) {
                Object protocolVersion = ProtocolSupportAPI_getProtocolVersion.invoke(null, p);
                int id = (int) ProtocolVersion_getId.invoke(protocolVersion);
                return id;
            } else {
                final Object handle = Reflection.getHandle(p);
                Object connection = AccessUtil.setAccessible(handle.getClass().getDeclaredField("playerConnection")).get(handle);
                Object network = AccessUtil.setAccessible(connection.getClass().getDeclaredField("networkManager")).get(connection);
                String name = null;
                if (HologramAPI.is1_8) {
                    if (Reflection.getVersion().contains("1_8_R1")) {
                        name = "i";
                    }
                    if (Reflection.getVersion().contains("1_8_R2")) {
                        name = "k";
                    }
                } else {
                    name = "m";
                }
                if (name == null) {
                    //				System.err.println("Invalid server version! Unable to find proper channel-field in getVersion.");
                    return 99;
                }
                Object channel = AccessUtil.setAccessible(network.getClass().getDeclaredField(name)).get(network);

                Object version = 0;
                try {
                    version = AccessUtil.setAccessible(network.getClass().getDeclaredMethod("getVersion", NMUClass.io_netty_channel_Channel)).invoke(network, channel);
                } catch (Exception e) {
                    // e.printStackTrace();
                    return 182;
                }
                return (int) version;
            }
        } catch (Exception e) {
            //			e.printStackTrace();
        }
        return 0;
    }

    public static boolean is1_8() {
        return is1_8;
    }

    public static boolean packetsEnabled() {
        return packetsEnabled;
    }

    protected static void enableProtocolSupport() {
        useProtocolSupport = true;

        try {
            ProtocolSupportAPI = Class.forName("protocolsupport.api.ProtocolSupportAPI");
            ProtocolVersion = Class.forName("protocolsupport.api.ProtocolVersion");

            ProtocolSupportAPI_getProtocolVersion = Reflection.getMethod(ProtocolSupportAPI, "getProtocolVersion", Player.class);
            ProtocolVersion_getId = Reflection.getMethod(ProtocolVersion, "getId");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
