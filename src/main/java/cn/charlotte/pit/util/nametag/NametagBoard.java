package cn.charlotte.pit.util.nametag;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Getter
public class NametagBoard {

    private final UUID uuid;
    private final NametagHandler handler;

    // Cache fields for on-demand update
    private boolean lastShowHealth = false; // Default? Or get initial state?
    private List<BufferedNametag> lastPlates = Collections.emptyList();
    // Use ConcurrentHashMap if accessed/modified by multiple threads (less likely now)
    private final Map<String, String> playerTeamMap = new ConcurrentHashMap<>(); // Cache player name -> team name

    // Buffered teams might still be useful for cleanup tracking
    private Set<String> currentTeams = new HashSet<>();

    /**
     * Nametag Board.
     *
     * @param player  that board belongs to.
     * @param handler instance.
     */
    public NametagBoard(Player player, NametagHandler handler) {
        this.uuid = player.getUniqueId();
        this.handler = handler;
        // Initial setup of scoreboard
        setup(player);
        // Perform an initial update to set the baseline
        updateBoardIfNeeded(handler.getAdapter().showHealthBelowName(player), handler.getAdapter().getPlate(player));
    }

    /**
     * Setup Nametag Board.
     *
     * @param player that board belongs to.
     */
    private void setup(Player player) {
        Scoreboard scoreboard = getScoreboard();
        // Avoid setting if already correct or hooking
        if (player.getScoreboard() != scoreboard && !handler.isHook()) {
             try {
                player.setScoreboard(scoreboard);
             } catch (IllegalStateException e) {
                  Bukkit.getLogger().warning("[Nametag] Failed to set initial scoreboard for " + player.getName());
             }
        }
    }

    /**
     * Get Scoreboard Object.
     *
     * @return existing scoreboard if in hook, or create new one.
     */
    public Scoreboard getScoreboard() {
        Player player = Bukkit.getPlayer(getUuid());
        if (getHandler().isHook() || player.getScoreboard() != Bukkit.getScoreboardManager().getMainScoreboard()) {
            return player.getScoreboard();
        } else {
            return Bukkit.getScoreboardManager().getNewScoreboard();
        }
    }

    /**
     * Checks if an update is needed and applies it if necessary.
     * This method MUST be called from the main server thread.
     *
     * @param newShowHealth The new value for showing health.
     * @param newPlates     The new list of nametag plates.
     */
    public void updateBoardIfNeeded(boolean newShowHealth, List<BufferedNametag> newPlates) {
        if (newPlates == null) newPlates = Collections.emptyList(); // Ensure list is not null

        // --- Comparison --- 
        boolean showHealthChanged = this.lastShowHealth != newShowHealth;
        boolean platesChanged = !arePlateListsEqual(this.lastPlates, newPlates);

        // If nothing changed, do nothing
        if (!showHealthChanged && !platesChanged) {
            return;
        }

        // --- Apply Updates (Main Thread) --- 
        Player player = Bukkit.getPlayer(getUuid());
        // Player might have logged off between async fetch and sync execution
        if (player == null || !player.isOnline()) {
            return;
        }

        Scoreboard scoreboard = getScoreboard(); // Get scoreboard in sync context
        if (scoreboard == null) return; // Should not happen if player online

        // Update cache *before* applying changes
        this.lastShowHealth = newShowHealth;
        this.lastPlates = copyPlates(newPlates); // Store a deep copy

        try {
            if (showHealthChanged) {
                updateHealthBelow(player, scoreboard, newShowHealth);
            }

            if (platesChanged) {
                updateNametags(player, scoreboard, newPlates);
            }
        } catch (Exception e) {
            handler.getPlugin().getLogger().log(Level.SEVERE, "[Nametag] Error applying update for " + player.getName(), e);
            // Consider resetting the board or notifying the player/admin
        }
    }

    /**
     * Deep copies a list of BufferedNametag objects.
     * Needed because BufferedNametag is mutable (via Lombok setters).
     */
    private List<BufferedNametag> copyPlates(List<BufferedNametag> original) {
        if (original == null) return Collections.emptyList();
        List<BufferedNametag> copy = new ArrayList<>(original.size());
        for (BufferedNametag tag : original) {
            if (tag != null) {
                // Create a new instance with the same data
                copy.add(new BufferedNametag(tag.getGroupName(), tag.getPrefix(), tag.getSuffix(), tag.isFriendlyInvis(), tag.getPlayer()));
            }
        }
        return copy;
    }

     /**
      * Compares two lists of BufferedNametag for equality.
      * Assumes order matters for simplicity, but could be adapted.
      * Checks key properties: groupName, prefix, suffix, player, friendlyInvis.
      */
     private boolean arePlateListsEqual(List<BufferedNametag> list1, List<BufferedNametag> list2) {
         if (list1 == list2) return true; // Same instance
         if (list1 == null || list2 == null) return false; // One is null
         if (list1.size() != list2.size()) return false; // Different sizes

         for (int i = 0; i < list1.size(); i++) {
             BufferedNametag tag1 = list1.get(i);
             BufferedNametag tag2 = list2.get(i);
             if (tag1 == null && tag2 == null) continue;
             if (tag1 == null || tag2 == null) return false; // One is null

             // Compare relevant fields
             if (tag1.isFriendlyInvis() != tag2.isFriendlyInvis() ||
                 !Objects.equals(tag1.getGroupName(), tag2.getGroupName()) ||
                 !Objects.equals(tag1.getPrefix(), tag2.getPrefix()) ||
                 !Objects.equals(tag1.getSuffix(), tag2.getSuffix()) ||
                 !Objects.equals(tag1.getPlayer(), tag2.getPlayer())) {
                 return false;
             }
         }
         return true;
     }

    /**
     * Update Health Slot.
     * Now takes showHealth parameter directly.
     */
    private void updateHealthBelow(Player player, Scoreboard scoreboard, boolean showHealth) {
        Objective objective = scoreboard.getObjective(DisplaySlot.BELOW_NAME);
        if (showHealth) {
            if (objective == null) {
                objective = scoreboard.registerNewObjective("showhealth", "health");
                objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
                // Use unicode heart ♥ (U+2665) or ❤ (U+2764)
                 objective.setDisplayName(ChatColor.RED + "❤");
            }
            // Update health for all online players? This seems inefficient.
            // Maybe only update for the target player?
            // For now, keeping original logic but consider optimizing.
            for (Player loopPlayer : Bukkit.getOnlinePlayers()) {
                 try {
                     // Ensure objective is not null inside loop
                     if (objective != null) {
                         // Use getScore for safety, setScore might fail if unregistered
                          scoreboard.getObjective(DisplaySlot.BELOW_NAME).getScore(loopPlayer.getName()).setScore((int) Math.floor(loopPlayer.getHealth()));
                     }
                 } catch (Exception e) {
                      handler.getPlugin().getLogger().warning("[Nametag] Failed to update health score for " + loopPlayer.getName() + " for viewer " + player.getName());
                 }
            }
        } else {
            if (objective != null) {
                objective.unregister();
            }
        }
    }

    /**
     * Get's or creates a team for the scoreboard.
     *
     * @param scoreboard of team.
     * @param name       of team.
     * @return new or existing team.
     */
    private Team getOrRegisterTeam(Scoreboard scoreboard, String name) {
        // Limit team name length
        if (name != null && name.length() > 16) {
            name = name.substring(0, 16);
        }
        Team team = scoreboard.getTeam(name);
        if (team == null) {
            team = scoreboard.registerNewTeam(name);
        }
        return team;
    }

    /**
     * Update Nametags slot.
     * Takes the new plates directly.
     * Optimized to reduce unnecessary updates.
     */
    private void updateNametags(Player player, Scoreboard scoreboard, List<BufferedNametag> newPlates) {
        Set<String> requiredTeams = new HashSet<>();
        Map<String, List<String>> teamPlayerMap = new HashMap<>(); // Track players per team for this update
        Map<String, BufferedNametag> playerCurrentTag = new HashMap<>(); // Track the desired tag for each player name

        // Process new plates
        for (BufferedNametag plate : newPlates) {
            if (plate == null || plate.getPlayer() == null) continue; // Skip invalid plates

            String teamName = plate.getGroupName();
            // Ensure team name is valid length
            if (teamName != null && teamName.length() > 16) {
                teamName = teamName.substring(0, 16);
            }
            if (teamName == null) continue; // Skip plates without a team name

            requiredTeams.add(teamName);
            teamPlayerMap.computeIfAbsent(teamName, k -> new ArrayList<>()).add(plate.getPlayer().getName());
            playerCurrentTag.put(plate.getPlayer().getName(), plate);

            Team team = getOrRegisterTeam(scoreboard, teamName);

            // Update Team properties only if changed (check requires storing last state or always setting)
            // For simplicity now, always set properties, but could be optimized further
            String prefix = plate.getPrefix() != null ? plate.getPrefix() : "";
            String suffix = plate.getSuffix() != null ? plate.getSuffix() : "";
             // Avoid setting identical properties?
             if (!team.getPrefix().equals(prefix)) team.setPrefix(prefix);
             if (!team.getSuffix().equals(suffix)) team.setSuffix(suffix);
             if (team.allowFriendlyFire() != !plate.isFriendlyInvis()) team.setAllowFriendlyFire(!plate.isFriendlyInvis()); // Assumption based on name
             if (team.canSeeFriendlyInvisibles() != plate.isFriendlyInvis()) team.setCanSeeFriendlyInvisibles(plate.isFriendlyInvis());

            // Add player to team if not already in the correct one
            String currentPlayerName = plate.getPlayer().getName();
            String previousTeamName = playerTeamMap.get(currentPlayerName);

            if (!teamName.equals(previousTeamName)) {
                 // Remove from old team if known
                 if (previousTeamName != null) {
                     Team oldTeam = scoreboard.getTeam(previousTeamName);
                     if (oldTeam != null && oldTeam.hasEntry(currentPlayerName)) {
                         oldTeam.removeEntry(currentPlayerName);
                     }
                 }
                 // Add to new team if not already present
                if (!team.hasEntry(currentPlayerName)) {
                     team.addEntry(currentPlayerName);
                }
                playerTeamMap.put(currentPlayerName, teamName); // Update cache
            }
        }

        // Clean up players removed from teams they were previously in
        Set<String> playersInUpdate = playerCurrentTag.keySet();
        playerTeamMap.forEach((playerName, teamName) -> {
            if (!playersInUpdate.contains(playerName)) {
                 Team team = scoreboard.getTeam(teamName);
                 if (team != null && team.hasEntry(playerName)) {
                     team.removeEntry(playerName);
                 }
            }
        });
        // Update the main cache after cleanup
        playerTeamMap.clear();
        playerTeamMap.putAll(playerCurrentTag.entrySet().stream()
                .filter(entry -> entry.getValue() != null && entry.getValue().getGroupName() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getGroupName())));

        // Clean up unused teams
        Set<String> teamsToRemove = new HashSet<>(currentTeams); // Teams from last update
        teamsToRemove.removeAll(requiredTeams); // Keep only those not needed now

        for (String teamNameToRemove : teamsToRemove) {
            Team team = scoreboard.getTeam(teamNameToRemove);
            if (team != null) {
                team.unregister();
            }
        }

        // Update the set of current teams for the next cleanup
        currentTeams = requiredTeams;
    }

    /**
     * Update method is deprecated, use updateBoardIfNeeded triggered by Handler.
     */
    @Deprecated
    public void update() {
        // This logic is now inside updateBoardIfNeeded and triggered by the handler.
        // Player player = Bukkit.getPlayer(getUuid());
        // if (player == null) return;
        // Scoreboard scoreboard = this.getScoreboard();
        // boolean showHealth = handler.getAdapter().showHealthBelowName(player);
        // List<BufferedNametag> plates = handler.getAdapter().getPlate(player);
        // updateHealthBelow(player, scoreboard, showHealth);
        // updateNametags(player, scoreboard, plates);
    }

    /**
     * Cleanup Board.
     */
    public void cleanup() {
        // Clear caches
        playerTeamMap.clear();
        currentTeams.clear();
        lastPlates = Collections.emptyList();
        // Scoreboard itself is reset by Handler/Listener
    }
}

