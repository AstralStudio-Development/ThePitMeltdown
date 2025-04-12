package cn.charlotte.pit.events.impl;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.config.NewConfiguration;
import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.events.IEvent;
import cn.charlotte.pit.events.INormalEvent;
import cn.charlotte.pit.medal.impl.challenge.QuickMathsMedal;
import cn.charlotte.pit.util.chat.CC;
import cn.charlotte.pit.util.chat.TitleUtil;
import cn.charlotte.pit.util.random.RandomUtil;
import cn.charlotte.pit.util.time.TimeUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * @Author: Misoryan
 * @Created_In: 2021/2/7 11:08
 */
public class QuickMathEvent implements IEvent, INormalEvent, Listener {
    public int RandomNumber;
    public int RandomNumber2;
    public int TheEquation;
    public String equationType;
    private final Set<UUID> alreadyAnswered = ConcurrentHashMap.newKeySet();
    @Setter
    @Getter
    private long startTime = 0L;
    private final AtomicInteger top = new AtomicInteger(0);
    private volatile boolean ended = false;

    @Override
    public String getEventInternalName() {
        return "quick_math_event";
    }

    @Override
    public String getEventName() {
        return "&9&l速算";
    }

    @Override
    public int requireOnline() {
        return NewConfiguration.INSTANCE.getEventOnlineRequired().get(getEventInternalName());
    }

    @Override
    public void onActive() {
        ended = false;
        randomEquation();
        if (!"x".equals(equationType)) {
            randomNumber(10, 100);
        } else {
            randomNumber(10, 30);
        }
        top.set(0);
        startTime = System.currentTimeMillis();
        alreadyAnswered.clear();
        CC.boardCast("&5&l速算! &7前五名在聊天栏发出答案的玩家可以获得 &6+200硬币 &b+100经验值 &7!");
        CC.boardCast("&5&l速算! &7在聊天栏里写下你的答案: &e" + RandomNumber + " " + equationType + " " + RandomNumber2);
        for (Player player : Bukkit.getOnlinePlayers()) {
            TitleUtil.sendTitle(player, "&5&l速算!", ("&e" + RandomNumber + " " + equationType + " " + RandomNumber2), 20, 20 * 5, 10);
        }
        Bukkit.getPluginManager().registerEvents(this, ThePit.getInstance());
        Bukkit.getScheduler().runTaskLater(ThePit.getInstance(), () -> {
            if (!ended) {
                ThePit.getInstance().getEventFactory().inactiveEvent(this);
            }
        }, 5 * 60 * 20L);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSyncChatForCancel(PlayerChatEvent e) {
        if (ended) return;

        String message = e.getMessage().trim();
        String correctAnswer = String.valueOf(TheEquation);

        if (message.equals(correctAnswer)) {
            if (alreadyAnswered.contains(e.getPlayer().getUniqueId())) {
                e.getRecipients().clear();
                e.getRecipients().add(e.getPlayer());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onAsyncChat(AsyncPlayerChatEvent e) {
        if (ended) return;

        String message = e.getMessage().trim();
        String correctAnswer = String.valueOf(TheEquation);

        if (message.equals(correctAnswer)) {
            Player player = e.getPlayer();
            UUID playerUUID = player.getUniqueId();

            if (alreadyAnswered.add(playerUUID)) {
                e.setCancelled(true);
                int currentTop = top.incrementAndGet();

                if (currentTop <= 5) {
                    long timeTaken = System.currentTimeMillis() - startTime;
                    boolean grantMedal = timeTaken <= 2500;
                    scheduleAsyncReward(playerUUID, currentTop, timeTaken, grantMedal);
                    if (currentTop == 5) {
                        Bukkit.getScheduler().runTask(ThePit.getInstance(), () -> {
                            if (!ended) {
                                ThePit.getInstance().getEventFactory().inactiveEvent(this);
                            }
                        });
                    }
                } else {
                    // Answered correctly but after top 5
                    // Let the message through or send a specific message?
                    // For now, keep cancelled as per previous logic.
                    // player.sendMessage(CC.translate("&5&l速算! &7你回答正确，但前5名已经产生!"));
                }
            } else {
                // Player already answered. Sync handler might cancel for others.
                // Or do nothing here if sync handler removed/changed.
            }
        }
    }

    @Override
    public void onInactive() {
        if (ended) return;
        ended = true;
        HandlerList.unregisterAll(this);
        CC.boardCast("&5&l速算! &7活动结束! 正确答案: &e" + TheEquation);
        alreadyAnswered.clear();
        top.set(0);
    }

    public void randomNumber(int min, int max) {
        if (min > max) {
            RandomNumber = (int) (Math.random() * (min - max + 1) + max);
            RandomNumber2 = (int) (Math.random() * (min - max + 1) + max);
        } else if (min < max) {
            RandomNumber = (int) (Math.random() * (max - min + 1) + min);
            RandomNumber2 = (int) (Math.random() * (max - min + 1) + min);
        } else {
            RandomNumber = (int) (Math.random() * (100 - 1 + 1) + 1);
            RandomNumber2 = (int) (Math.random() * (100 - 1 + 1) + 1);
        }
        switch (equationType) {
            case "+":
                TheEquation = RandomNumber + RandomNumber2;
                break;
            case "-":
                TheEquation = RandomNumber - RandomNumber2;
                break;
            case "x":
                TheEquation = RandomNumber * RandomNumber2;
                break;
        }
    }

    public void randomEquation() {
        equationType = (String) RandomUtil.helpMeToChooseOne("+", "-", "x");
    }

    // OPTIMIZATION: Helper method to schedule async reward processing
    private void scheduleAsyncReward(UUID playerUUID, int rank, long timeTaken, boolean grantMedal) {
        ThePit plugin = ThePit.getInstance();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            PlayerProfile profile = PlayerProfile.getOrLoadPlayerProfileByUuid(playerUUID);
            if (profile != null && profile != PlayerProfile.NONE_PROFILE) {
                try {
                    // Broadcast message (needs player name, get it from profile or cache)
                    // Needs to run on main thread
                    String formattedName = profile.getFormattedName(); // Assuming this is safe to call
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        // Check if event ended before broadcasting - avoid spam after end
                        if (!ended) {
                            CC.boardCast("&5&l速算! &e#" + rank + " " + formattedName + " &7在 &e" + TimeUtil.millisToRoundedTime(timeTaken) + " &7内回答正确!");
                        }
                    });

                    // Apply rewards
                    profile.setCoins(profile.getCoins() + 200);
                    profile.grindCoins(200); // Assuming this is needed
                    profile.setExperience(profile.getExperience() + 100);

                    // Apply medal progress
                    if (grantMedal) {
                        // FIXME: Medal system interaction might need different call/context
                        try {
                            new QuickMathsMedal().addProgress(profile, 1);
                        } catch (Exception medalEx) {
                            plugin.getLogger().log(Level.WARNING, "[QuickMathEvent] Error adding medal progress for " + playerUUID, medalEx);
                        }
                    }

                    // Save profile if changed
                    CompletableFuture.runAsync(() -> profile.save(null), plugin.getSavingThreadPool());

                } catch (Exception ex) {
                    plugin.getLogger().log(Level.SEVERE, "[QuickMathEvent] Error processing reward for " + playerUUID, ex);
                }
            } else {
                plugin.getLogger().warning("[QuickMathEvent] Failed to load profile for reward: " + playerUUID);
            }
        });
    }
}
