package cn.charlotte.pit.events.impl;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.config.NewConfiguration;
import cn.charlotte.pit.config.PitConfig;
import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.events.IEvent;
import cn.charlotte.pit.events.INormalEvent;
import cn.charlotte.pit.medal.impl.challenge.CakeEventMedal;
import cn.charlotte.pit.util.LocationUtil;
import cn.charlotte.pit.util.aabb.AxisAlignedBB;
import cn.charlotte.pit.util.chat.CC;
import cn.charlotte.pit.util.chat.MessageType;
import cn.charlotte.pit.util.cooldown.Cooldown;
import cn.charlotte.pit.util.random.RandomUtil;
import com.boydti.fawe.FaweAPI;
import com.boydti.fawe.util.TaskManager;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * @Author: EmptyIrony
 * @Date: 2021/2/5 23:37
 */
public class CakeEvent implements IEvent, INormalEvent, Listener {
    private static CuboidRegion[] regions;
    private final DecimalFormat numFormatTwo = new DecimalFormat("0.00");
    private final DecimalFormat df = new DecimalFormat(",###,###,###,###");
    private BukkitRunnable endTask;
    private BukkitRunnable flushDataTask;
    private EditSession session;
    private AxisAlignedBB alignedBB;
    private Map<UUID, CakePlayerData> dataCache;

    public CakeEvent() {
        if (regions == null) {
            PitConfig config = ThePit.getInstance().getPitConfig();
            regions = new CuboidRegion[]{
                    new CuboidRegion(BukkitUtil.toVector(config.getCakeZoneAPosA()), BukkitUtil.toVector(config.getCakeZoneAPosB())),
                    new CuboidRegion(BukkitUtil.toVector(config.getCakeZoneBPosA()), BukkitUtil.toVector(config.getCakeZoneBPosB())),
                    new CuboidRegion(BukkitUtil.toVector(config.getCakeZoneCPosA()), BukkitUtil.toVector(config.getCakeZoneCPosB())),
                    new CuboidRegion(BukkitUtil.toVector(config.getCakeZoneDPosA()), BukkitUtil.toVector(config.getCakeZoneDPosB()))
            };
        }
    }

    @Override
    public String getEventInternalName() {
        return "cake";
    }

    @Override
    public String getEventName() {
        return "&d蛋糕争夺战";
    }

    @Override
    public int requireOnline() {
        return NewConfiguration.INSTANCE.getEventOnlineRequired().get(getEventInternalName());
    }

    @Override
    public void onActive() {
        CC.boardCast(MessageType.EVENT, "&d蛋糕! &7巨型蛋糕已生成在地图上，吃掉蛋糕获得大量金币和经验!");

        this.dataCache = new ConcurrentHashMap<>();

        final CuboidRegion region = regions[RandomUtil.random.nextInt(regions.length)];
        final Vector pos1 = region.getPos1();
        final Vector pos2 = region.getPos2();
        this.alignedBB = new AxisAlignedBB(pos1.getX(), pos1.getY(), pos1.getZ(), pos2.getX(), pos2.getY(), pos2.getZ());

        TaskManager.IMP.async(() -> {
            try {
                BukkitWorld world = new BukkitWorld(Bukkit.getWorlds().get(0));
                this.session = FaweAPI.getEditSessionBuilder(world).build();

                final RandomPattern pattern = new RandomPattern();
                pattern.add(new BaseBlock(BlockID.CAKE_BLOCK), 0.94);
                pattern.add(new BaseBlock(BlockID.STAINED_CLAY, 2), 0.03);
                pattern.add(new BaseBlock(BlockID.STAINED_CLAY, 15), 0.03);

                session.setBlocks(region, pattern);

                session.flushQueue();
            } catch (Throwable e) {
                ThePit.getInstance().getLogger().log(Level.SEVERE, "[CakeEvent] Error generating cake blocks with FAWE", e);
            }
        });

        Bukkit.getPluginManager()
                .registerEvents(this, ThePit.getInstance());

        this.endTask = new BukkitRunnable() {
            @Override
            public void run() {
                ThePit.getInstance().getEventFactory().inactiveEvent(CakeEvent.this);
            }
        };

        this.endTask.runTaskLater(ThePit.getInstance(), 20 * 60 * 5);

        this.flushDataTask = new BukkitRunnable() {
            @Override
            public void run() {
                flushCakeData(false);
            }
        };
        this.flushDataTask.runTaskTimerAsynchronously(ThePit.getInstance(), 20 * 30, 20 * 30);
    }

    @Override
    public void onInactive() {
        if (endTask != null) {
            endTask.cancel();
            endTask = null;
        }
        if (flushDataTask != null) {
            flushDataTask.cancel();
            flushDataTask = null;
        }

        if (this.session != null) {
            TaskManager.IMP.async(() -> {
                try {
                    this.session.undo(session);
                    this.session.flushQueue();
                } catch (Throwable e) {
                    ThePit.getInstance().getLogger().log(Level.SEVERE, "[CakeEvent] Error undoing FAWE session", e);
                }
            });
        }

        HandlerList.unregisterAll(this);

        flushCakeData(true);
    }

    @EventHandler
    public void onEatCake(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        final Block block = event.getClickedBlock();
        if (block == null) return;
        final Location location = block.getLocation();

        final AxisAlignedBB blockAABB = new AxisAlignedBB(location.getX(), location.getY(), location.getZ(), location.getX() + 1, location.getY() + 1, location.getZ() + 1);

        if (alignedBB == null || !alignedBB.intersectsWith(blockAABB)) {
            return;
        }

        final Player player = event.getPlayer();

        CakePlayerData cakePlayerData = dataCache.computeIfAbsent(player.getUniqueId(), uuid -> {
            CakePlayerData newData = new CakePlayerData();
            newData.uuid = uuid;
            newData.name = player.getDisplayName();
            newData.cooldown = new Cooldown(0);
            return newData;
        });

        if (!cakePlayerData.cooldown.hasExpired()) {
            return;
        }

        if (!canEat(location)) {
            CC.send(MessageType.EVENT, player, "&d&l蛋糕! &c你不能吃里面的蛋糕! 请先吃掉周围的蛋糕!");
            return;
        }

        cakePlayerData.cooldown = new Cooldown(100);

        Material blockType = block.getType();
        double gainedCoins = 0;
        double gainedXp = 0;
        boolean ateSomething = false;

        if (blockType == Material.CAKE_BLOCK) {
            int baseCoins = 1;
            byte data = block.getData();
            if (data == 6) {
                 baseCoins += 5;
                block.setType(Material.AIR);
            } else {
                block.setData((byte) (data + 1));
                 block.getState().update(true, false);
            }
            gainedCoins = baseCoins;
            ateSomething = true;

        } else if (blockType == Material.STAINED_CLAY) {
            byte data = block.getState().getData().getData();
            if (data == 2) {
                block.setType(Material.AIR);
                gainedCoins = 20;
                ateSomething = true;
                CC.send(MessageType.EVENT, player, "&d&l蛋糕! &7吃下了 &c樱桃 &6+20金币&7!");
            } else if (data == 15) {
                block.setType(Material.AIR);
                gainedXp = 20;
                ateSomething = true;
                CC.send(MessageType.EVENT, player, "&d&l蛋糕! &7吃下了 &4巧克力 &b+20经验&7!");
            }
        }

        if (ateSomething) {
            cakePlayerData.clicked++;
            cakePlayerData.coins += gainedCoins;
            cakePlayerData.xp += gainedXp;

            player.playSound(player.getLocation(), Sound.EAT, 1, 1);

            if (gainedCoins > 0 && gainedXp > 0) {
                 CC.send(MessageType.EVENT, player, "&d&l蛋糕! &6+" + gainedCoins + "金币 &b+" + gainedXp + "经验 &d(" + cakePlayerData.clicked + "次) &6总计: " + numFormatTwo.format(cakePlayerData.coins) + "金币, " + numFormatTwo.format(cakePlayerData.xp) + "经验");
            } else if (gainedCoins > 0) {
                 CC.send(MessageType.EVENT, player, "&d&l蛋糕! &6+" + gainedCoins + "金币 &d(" + cakePlayerData.clicked + "次) &6总计: " + numFormatTwo.format(cakePlayerData.coins) + "金币");
            } else if (gainedXp > 0) {
                 CC.send(MessageType.EVENT, player, "&d&l蛋糕! &b+" + gainedXp + "经验 &d(" + cakePlayerData.clicked + "次) &b总计: " + numFormatTwo.format(cakePlayerData.xp) + "经验");
            }
        }
    }

    private void flushCakeData(boolean isFinalFlush) {
        Map<UUID, CakePlayerData> dataToFlush = new HashMap<>(this.dataCache);

        if (isFinalFlush) {
             this.dataCache.clear();
        }

        if (dataToFlush.isEmpty()) {
            return;
        }

        ThePit plugin = ThePit.getInstance();

        for (Map.Entry<UUID, CakePlayerData> entry : dataToFlush.entrySet()) {
            UUID uuid = entry.getKey();
            CakePlayerData data = entry.getValue();

            double coinsToSave = data.coins;
            double xpToSave = data.xp;
            int clicks = data.clicked;

            if (coinsToSave <= 0 && xpToSave <= 0) {
                continue;
            }

            CompletableFuture.runAsync(() -> {
                PlayerProfile profile = PlayerProfile.getOrLoadPlayerProfileByUuid(uuid);
                if (profile != null && profile != PlayerProfile.NONE_PROFILE) {
                    try {
                        boolean profileChanged = false;
                        if (coinsToSave > 0) {
                            profile.setCoins(profile.getCoins() + coinsToSave);
                            profile.grindCoins(coinsToSave);
                            profileChanged = true;
                        }
                        if (xpToSave > 0) {
                            profile.setExperience(profile.getExperience() + xpToSave);
                            profileChanged = true;
                        }

                        if (isFinalFlush && data.coins >= 5000) {
                             new CakeEventMedal().addProgress(profile, 1);
                             profileChanged = true;
                        }

                        if (profileChanged) {
                            profile.save(null);
                        }

                        if (xpToSave > 0) {
                            Player player = Bukkit.getPlayer(uuid);
                            if (player != null && player.isOnline()) {
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        profile.applyExperienceToPlayer(player);
                                    }
                                }.runTask(plugin);
                            }
                        }

                         if (!isFinalFlush) {
                              CakePlayerData liveData = this.dataCache.get(uuid);
                              if (liveData != null) {
                                   liveData.coins -= coinsToSave;
                                   liveData.xp -= xpToSave;
                              }
                         }

                    } catch (Exception e) {
                        plugin.getLogger().log(Level.SEVERE, "[CakeEvent] Error processing data for " + uuid, e);
                    }
                } else {
                    plugin.getLogger().warning("[CakeEvent] Failed to load profile for " + uuid + " during data flush.");
                }
            }, plugin.getSavingThreadPool());
        }
    }

    private boolean canEat(Location location) {
        for (Location face : LocationUtil.getFaces(location)) {
            Material type = face.getBlock().getType();
            if (type != Material.CAKE_BLOCK && type != Material.STAINED_CLAY) {
                return true;
            }
        }
        return false;
    }

    public static class CakePlayerData {
        private UUID uuid;
        private String name;
        private double coins = 0;
        private double xp = 0;
        private int clicked = 0;
        private Cooldown cooldown;
    }
}
