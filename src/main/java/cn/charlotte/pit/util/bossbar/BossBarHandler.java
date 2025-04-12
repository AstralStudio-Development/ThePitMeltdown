package cn.charlotte.pit.util.bossbar;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.parm.AutoRegister;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;
import java.util.logging.Level;

/**
 * @Author: EmptyIrony
 * @Date: 2021/1/11 23:50
 */

@AutoRegister
public class BossBarHandler implements Listener {
    @Getter
    private final BossBar bossBar;
    private BukkitTask updateTask;
    private boolean running = false;

    @Getter
    private static BossBarHandler instance;

    public BossBarHandler() {
        instance = this;
        this.bossBar = new BossBar("");

        this.startUpdateTask();
    }

    private void startUpdateTask() {
        if (running || updateTask != null) {
             ThePit.getInstance().getLogger().warning("[BossBarHandler] Update task already running or exists.");
             return;
        }
        running = true;
        this.updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                boolean shouldBeVisible = bossBar.getTitle() != null && !bossBar.getTitle().isEmpty();

                Bukkit.getScheduler().runTask(ThePit.getInstance(), () -> {
                    if (!running) return;

                    try {
                        if (shouldBeVisible) {
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                if (!bossBar.getWithers().containsKey(player.getUniqueId())) {
                                    bossBar.addPlayer(player);
                                }
                            }
                            bossBar.update();
                        } else {
                            Object[] playerUUIDs = bossBar.getWithers().keySet().toArray();
                            for (Object uuidObj : playerUUIDs) {
                                Player player = Bukkit.getPlayer((UUID)uuidObj);
                                if (player != null) {
                                     bossBar.removePlayer(player);
                                }
                             }
                        }
                    } catch (Exception e) {
                        ThePit.getInstance().getLogger().log(Level.SEVERE, "[BossBarHandler] BossBar 更新出错", e);
                    }
                });
            }
        }.runTaskTimerAsynchronously(ThePit.getInstance(), 20L, 10L);
        ThePit.getInstance().getLogger().info("[BossBarHandler] BossBar 更新任务开始执行");
    }

    public void stopUpdateTask() {
        if (!running) return;
        running = false;
         if (this.updateTask != null) {
             this.updateTask.cancel();
             this.updateTask = null;
             ThePit.getInstance().getLogger().info("[BossBarHandler] BossBar 更新任务停止");
         }
         try {
            for (UUID uuid : bossBar.getWithers().keySet()) {
                 Player player = Bukkit.getPlayer(uuid);
                 if (player != null) {
                     bossBar.removePlayer(player);
                 }
            }
             bossBar.getWithers().clear();
         } catch (Exception e) {
             ThePit.getInstance().getLogger().log(Level.SEVERE, "[BossBarHandler] 清理BossBar时出错", e);
         }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        if (bossBar.getTitle() != null && !bossBar.getTitle().isEmpty()) {
             this.bossBar.addPlayer(event.getPlayer());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.bossBar.removePlayer(event.getPlayer());
    }

}
