package cn.charlotte.pit.util.scoreboard;

import cn.charlotte.pit.ThePit;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class AssembleThread implements Runnable {

    private final Assemble assemble;
    protected int taskId;
    private final boolean runAsynchronously;

    /**
     * Assemble Thread.
     *
     * @param assemble instance.
     */
    AssembleThread(Assemble assemble) {
        this.assemble = assemble;
        Plugin protocolLib = Bukkit.getPluginManager().getPlugin("ProtocolLib");
        this.runAsynchronously = (protocolLib != null && protocolLib.getDescription().getVersion().startsWith("5"));

        if (runAsynchronously) {
            taskId = Bukkit.getScheduler().runTaskTimerAsynchronously(ThePit.getInstance(), this, assemble.getTicks(), assemble.getTicks()).getTaskId();
        } else {
            taskId = Bukkit.getScheduler().runTaskTimer(ThePit.getInstance(), this, assemble.getTicks(), assemble.getTicks()).getTaskId();
        }
    }

    @Override
    @SneakyThrows
    public void run() {
        if (runAsynchronously) {
            asyncTick();
        } else {
            tick();
        }
    }

    /**
     * Synchronous tick logic.
     */
    private void tick() {
        for (Player player : this.assemble.getPlugin().getServer().getOnlinePlayers()) {
            try {
                updateBoard(player);
            } catch (Exception e) {
                if (assemble.isDebugMode()) {
                    e.printStackTrace();
                }
                assemble.getPlugin().getLogger().log(Level.SEVERE, "[Assemble] Error updating scoreboard for " + player.getName(), e);
            }
        }
    }

    /**
     * Asynchronous tick logic: Gathers data async, schedules sync updates.
     */
    private void asyncTick() {
        for (Player player : this.assemble.getPlugin().getServer().getOnlinePlayers()) {
            AssembleBoard board = this.assemble.getBoards().get(player.getUniqueId());
            if (board == null) continue;

            try {
                AssembleAdapter adapter = this.assemble.getAdapter();
                String newTitle = adapter.getTitle(player);
                List<String> newLinesRaw = adapter.getLines(player);

                final String finalNewTitle = (newTitle == null) ? "" : ChatColor.translateAlternateColorCodes('&', newTitle);
                final List<String> finalNewLines = new ArrayList<>();
                if (newLinesRaw != null) {
                    for (String line : newLinesRaw) {
                        if (line != null) {
                            finalNewLines.add(ChatColor.translateAlternateColorCodes('&', line));
                        }
                    }
                }
                if (finalNewLines.size() > 15) {
                    finalNewLines.subList(15, finalNewLines.size()).clear();
                }

                if (!this.assemble.getAssembleStyle().isDescending()) {
                    Collections.reverse(finalNewLines);
                }

                boolean titleChanged = !Objects.equals(board.getLastTitle(), finalNewTitle);
                boolean linesChanged = !Objects.equals(board.getLastLines(), finalNewLines);

                if (!titleChanged && !linesChanged) {
                    continue;
                }

                board.setLastTitle(finalNewTitle);
                board.setLastLines(finalNewLines);

                Bukkit.getScheduler().runTask(this.assemble.getPlugin(), () -> {
                    Player onlinePlayer = Bukkit.getPlayer(board.getUuid());
                    if (onlinePlayer == null || !onlinePlayer.isOnline()) return;

                    AssembleBoard currentBoard = this.assemble.getBoards().get(board.getUuid());
                    if (currentBoard == null) return;

                    Scoreboard scoreboard = currentBoard.getScoreboard();
                    Objective objective = currentBoard.getObjective();

                    if (scoreboard == null || objective == null) {
                        return;
                    }

                    try {
                        if (titleChanged) {
                            objective.setDisplayName(finalNewTitle);
                        }

                        if (linesChanged) {
                            if (currentBoard.getEntries().size() > finalNewLines.size()) {
                                for (int i = finalNewLines.size(); i < currentBoard.getEntries().size(); i++) {
                                    AssembleBoardEntry entry = currentBoard.getEntryAtPosition(i);
                                    if (entry != null) {
                                        entry.remove();
                                    }
                                }
                                currentBoard.getEntries().subList(finalNewLines.size(), currentBoard.getEntries().size()).clear();
                            }

                            int cache = this.assemble.getAssembleStyle().getStartNumber();
                            for (int i = 0; i < finalNewLines.size(); i++) {
                                AssembleBoardEntry entry = currentBoard.getEntryAtPosition(i);
                                String line = finalNewLines.get(i);

                                if (entry == null) {
                                    entry = new AssembleBoardEntry(currentBoard, line, i);
                                } else {
                                    if (!Objects.equals(entry.getText(), line)) {
                                        entry.setText(line);
                                        entry.setup();
                                    }
                                }
                                entry.send(this.assemble.getAssembleStyle().isDescending() ? cache-- : cache++);
                            }
                        }

                        if (onlinePlayer.getScoreboard() != scoreboard && !assemble.isHook()) {
                            onlinePlayer.setScoreboard(scoreboard);
                        }
                    } catch (Exception e) {
                        if (assemble.isDebugMode()) {
                            e.printStackTrace();
                        }
                        assemble.getPlugin().getLogger().log(Level.SEVERE, "[Assemble] Error applying sync update for " + onlinePlayer.getName(), e);
                    }
                });

            } catch (Exception e) {
                if (assemble.isDebugMode()) {
                    e.printStackTrace();
                }
                assemble.getPlugin().getLogger().log(Level.SEVERE, "[Assemble] Error during async processing for " + player.getName(), e);
            }
        }
    }

    /**
     * Handles the update for a single player (Used by synchronous tick).
     * Contains the core update logic, ensuring it's called from the correct thread.
     */
    private void updateBoard(Player player) {
        AssembleBoard board = this.assemble.getBoards().get(player.getUniqueId());
        if (board == null) return;

        Scoreboard scoreboard = board.getScoreboard();
        Objective objective = board.getObjective();
        if (scoreboard == null || objective == null) return;

        AssembleAdapter adapter = this.assemble.getAdapter();
        String newTitle = adapter.getTitle(player);
        List<String> newLinesRaw = adapter.getLines(player);

        final String finalNewTitle = (newTitle == null) ? "" : ChatColor.translateAlternateColorCodes('&', newTitle);
        final List<String> finalNewLines = new ArrayList<>();
        if (newLinesRaw != null) {
            for (String line : newLinesRaw) {
                if (line != null) {
                    finalNewLines.add(ChatColor.translateAlternateColorCodes('&', line));
                }
            }
        }
        if (finalNewLines.size() > 15) {
            finalNewLines.subList(15, finalNewLines.size()).clear();
        }
        if (!this.assemble.getAssembleStyle().isDescending()) {
            Collections.reverse(finalNewLines);
        }

        boolean titleChanged = !Objects.equals(board.getLastTitle(), finalNewTitle);
        boolean linesChanged = !Objects.equals(board.getLastLines(), finalNewLines);

        if (!titleChanged && !linesChanged) {
            return;
        }

        board.setLastTitle(finalNewTitle);
        board.setLastLines(finalNewLines);

        if (titleChanged) {
            objective.setDisplayName(finalNewTitle);
        }

        if (linesChanged) {
            if (board.getEntries().size() > finalNewLines.size()) {
                for (int i = finalNewLines.size(); i < board.getEntries().size(); i++) {
                    AssembleBoardEntry entry = board.getEntryAtPosition(i);
                    if (entry != null) entry.remove();
                }
                board.getEntries().subList(finalNewLines.size(), board.getEntries().size()).clear();
            }

            int cache = this.assemble.getAssembleStyle().getStartNumber();
            for (int i = 0; i < finalNewLines.size(); i++) {
                AssembleBoardEntry entry = board.getEntryAtPosition(i);
                String line = finalNewLines.get(i);
                if (entry == null) {
                    entry = new AssembleBoardEntry(board, line, i);
                } else {
                    if (!Objects.equals(entry.getText(), line)) {
                        entry.setText(line);
                        entry.setup();
                    }
                }
                entry.send(this.assemble.getAssembleStyle().isDescending() ? cache-- : cache++);
            }
        }

        if (player.getScoreboard() != scoreboard && !assemble.isHook()) {
            player.setScoreboard(scoreboard);
        }
    }
}
