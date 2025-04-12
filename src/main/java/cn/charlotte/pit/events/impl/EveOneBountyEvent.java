package cn.charlotte.pit.events.impl;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.config.NewConfiguration;
import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.events.IEvent;
import cn.charlotte.pit.events.INormalEvent;
import cn.charlotte.pit.util.chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * @Author: Misoryan
 * @Created_In: 2021/2/8 21:25
 */
public class EveOneBountyEvent implements IEvent, INormalEvent {
    @Override
    public String getEventInternalName() {
        return "everyone_bounty_event";
    }

    @Override
    public String getEventName() {
        return "全员通缉";
    }

    @Override
    public int requireOnline() {
        return NewConfiguration.INSTANCE.getEventOnlineRequired().get(getEventInternalName());
    }

    @Override
    public void onActive() {
        Bukkit.broadcastMessage(CC.translate("&6&l全员通缉! &7所有人现在都被以 &6100g &7的赏金悬赏了!"));

        ThePit plugin = ThePit.getInstance();

        CompletableFuture.runAsync(() -> {
            for (UUID uuid : Bukkit.getOnlinePlayers().stream().map(Entity::getUniqueId).collect(Collectors.toList())) {
                try {
                    PlayerProfile profile = PlayerProfile.getOrLoadPlayerProfileByUuid(uuid);
                    if (profile != null && profile != PlayerProfile.NONE_PROFILE) {
                        if (profile.isInArena()) {
                            profile.setBounty(profile.getBounty() + 100);
                            profile.save(null);
                        }
                    } else {
                        plugin.getLogger().warning("[EveOneBountyEvent] Failed to load profile for UUID: " + uuid);
                    }
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "[EveOneBountyEvent] Error processing bounty for UUID: " + uuid, e);
                }
            }
            plugin.getLogger().info("[EveOneBountyEvent] Finished applying bounties asynchronously.");

        }, plugin.getSavingThreadPool());

        plugin.getEventFactory().inactiveEvent(this);
    }

    @Override
    public void onInactive() {

    }
}
