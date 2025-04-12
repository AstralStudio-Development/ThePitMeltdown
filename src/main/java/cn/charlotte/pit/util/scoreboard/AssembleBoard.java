package cn.charlotte.pit.util.scoreboard;

import cn.charlotte.pit.util.scoreboard.events.AssembleBoardCreatedEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.Objects;

public class AssembleBoard {

    private final Assemble assemble;

    private final List<AssembleBoardEntry> entries = new ArrayList<>();
    private final List<String> identifiers = new ArrayList<>();

    private final UUID uuid;

    // Cache fields for on-demand update
    private String lastTitle = null;
    private List<String> lastLines = Collections.emptyList(); // Initialize with empty list

    /**
     * Assemble Board.
     *
     * @param player   that the board belongs to.
     * @param assemble instance.
     */
    public AssembleBoard(Player player, Assemble assemble) {
        this.uuid = player.getUniqueId();
        this.assemble = assemble;
        this.setup(player);
    }

    /**
     * Gets a ChatColor based off the position in the collection.
     *
     * @param position of entry.
     * @return ChatColor adjacent to position.
     */
    private static String getRandomChatColor(int position) {
        return ChatColor.values()[position].toString();
    }

    /**
     * Get's a player's bukkit scoreboard.
     *
     * @return either existing scoreboard or new scoreboard.
     */
    public Scoreboard getScoreboard() {
        Player player = Bukkit.getPlayer(getUuid());

        if (assemble.isHook() || player.getScoreboard() != Bukkit.getScoreboardManager().getMainScoreboard()) {
            return player.getScoreboard();
        } else {
            return Bukkit.getScoreboardManager().getNewScoreboard();
        }
    }

    /**
     * Get's the player's scoreboard objective.
     *
     * @return either existing objecting or new objective.
     */
    public Objective getObjective() {
        Scoreboard scoreboard = getScoreboard();
        if (scoreboard.getObjective("Assemble") == null) {
            Objective objective = scoreboard.registerNewObjective("Assemble", "dummy");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
            objective.setDisplayName(getAssemble().getAdapter().getTitle(Bukkit.getPlayer(getUuid())));
            return objective;
        } else {
            return scoreboard.getObjective("Assemble");
        }
    }

    /**
     * Setup the board.
     *
     * @param player who's board to setup.
     */
    private void setup(Player player) {
        Scoreboard scoreboard = getScoreboard();
        // Avoid setting scoreboard if it's already the one we manage or if hooking
        if (player.getScoreboard() != scoreboard) {
             try {
                 player.setScoreboard(scoreboard);
             } catch (IllegalStateException e) {
                 // Handle potential errors if player is offline during setup (less likely but possible)
                 Bukkit.getLogger().warning("[Assemble] Failed to set scoreboard for offline player? " + player.getName());
                 return; // Stop setup if scoreboard can't be set
             }
        }
        getObjective(); // Ensure objective exists

        // Send Update.
        AssembleBoardCreatedEvent createdEvent = new AssembleBoardCreatedEvent(this);
        Bukkit.getPluginManager().callEvent(createdEvent);
    }

    /**
     * Get the board entry at a specific position.
     *
     * @param pos to find entry.
     * @return entry if it isn't out of range.
     */
    public AssembleBoardEntry getEntryAtPosition(int pos) {
        return pos >= this.entries.size() ? null : this.entries.get(pos);
    }

    /**
     * Get the unique identifier for position in scoreboard.
     * Uses a simpler prefix+number approach instead of color codes.
     *
     * @param position for identifier.
     * @return unique identifier (e.g., "_sb0", "_sb1").
     */
    public String getUniqueIdentifier(int position) {
        // Note: This assumes max lines <= 15, fitting within 16 chars easily.
        // If more lines were possible, this might need adjustment.
        return "§" + position; // Use section sign + number for uniqueness up to 15
        // Alternative: return "_sb" + position;
    }

    // --- Getters and Setters for Cache --- 
    public String getLastTitle() {
        return lastTitle;
    }

    public void setLastTitle(String lastTitle) {
        this.lastTitle = lastTitle;
    }

    public List<String> getLastLines() {
        return lastLines;
    }

    // Store a copy of the list to prevent external modification issues
    public void setLastLines(List<String> newLines) {
        this.lastLines = (newLines == null) ? Collections.emptyList() : new ArrayList<>(newLines);
    }
    // --- End Getters and Setters --- 

    public Assemble getAssemble() {
        return this.assemble;
    }

    public List<AssembleBoardEntry> getEntries() {
        return this.entries;
    }

    public List<String> getIdentifiers() {
        // Note: getUniqueIdentifier no longer uses this list for collision checking
        // This list might be removable if AssembleBoardEntry doesn't need it
        return this.identifiers;
    }

    public UUID getUuid() {
        return this.uuid;
    }
}
