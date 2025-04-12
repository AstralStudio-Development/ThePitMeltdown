package cn.charlotte.pit.util.nametag;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

@Getter
@Setter
public class NametagHandler {

    private JavaPlugin plugin;

    private NametagAdapter adapter;
    private BukkitTask updateTask;
    private NametagListeners listeners;

    private Map<UUID, NametagBoard> boards;
    public long ticks = 2;
    private boolean hook = false;
    private boolean running = false;

    /**
     * Nametag Handler.
     *
     * @param plugin  instance.
     * @param adapter to display nametags.
     */
    public NametagHandler(JavaPlugin plugin, NametagAdapter adapter) {
        if (plugin == null) {
            throw new RuntimeException("Nametag Handler can not be instantiated without a plugin instance!");
        }

        this.plugin = plugin;
        this.adapter = adapter;
        this.boards = new ConcurrentHashMap<>();

        this.setup();
    }

    /**
     * Setup Library.
     */
    public void setup() {
        if (running) {
            plugin.getLogger().warning("[NametagHandler] Setup called while already running. Ignoring.");
            return;
        }
        this.listeners = new NametagListeners(this);
        this.plugin.getServer().getPluginManager().registerEvents(this.listeners, this.plugin);

        this.boards.clear();

        for (Player player : Bukkit.getOnlinePlayers()) {
            getBoards().putIfAbsent(player.getUniqueId(), new NametagBoard(player, this));
        }

        this.updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                tickAsync();
            }
        }.runTaskTimerAsynchronously(this.plugin, 0L, this.ticks);

        running = true;
        plugin.getLogger().info("[NametagHandler] Setup complete. Update task scheduled.");
    }

    /**
     * Asynchronous Tick Logic.
     * Fetches data for each player and schedules a synchronous update if needed.
     */
    private void tickAsync() {
        if (this.adapter == null) return;

        for (Player player : this.plugin.getServer().getOnlinePlayers()) {
            NametagBoard board = this.boards.get(player.getUniqueId());
            if (board == null) continue;

            try {
                boolean newShowHealth = this.adapter.showHealthBelowName(player);
                List<BufferedNametag> newPlates = this.adapter.getPlate(player);

                Bukkit.getScheduler().runTask(this.plugin, () -> board.updateBoardIfNeeded(newShowHealth, newPlates));

            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "[NametagHandler] Error fetching nametag data for " + player.getName(), e);
            }
        }
    }

    /**
     * Cleanup Library.
     */
    public void cleanup() {
        if (!running) return;
        running = false;

        if (this.updateTask != null) {
            this.updateTask.cancel();
            this.updateTask = null;
        }

        if (this.listeners != null) {
            HandlerList.unregisterAll(this.listeners);
            this.listeners = null;
        }

        for (UUID uuid : getBoards().keySet()) {
            Player player = Bukkit.getPlayer(uuid);

            if (player == null || !player.isOnline()) {
                continue;
            }

            NametagBoard board = getBoards().remove(uuid);
            if (board != null) {
                board.cleanup();
            }

            if (!isHook() && player.isOnline()) {
                try {
                    player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
                } catch (IllegalStateException e) {
                }
            }
        }
        this.boards.clear();
        plugin.getLogger().info("[NametagHandler] Cleanup complete.");
    }

}
