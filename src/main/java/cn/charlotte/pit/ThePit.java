package cn.charlotte.pit;

import cn.charlotte.pit.actionbar.IActionBarManager;
import cn.charlotte.pit.api.PitInternalHook;
import cn.charlotte.pit.api.PointsAPI;
import cn.charlotte.pit.buff.BuffFactory;
import cn.charlotte.pit.config.NewConfiguration;
import cn.charlotte.pit.config.PitConfig;
import cn.charlotte.pit.data.FixedRewardData;
import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.database.MongoDB;
import cn.charlotte.pit.enchantment.EnchantmentFactor;
import cn.charlotte.pit.event.OriginalTimeChangeEvent;
import cn.charlotte.pit.events.EventFactory;
import cn.charlotte.pit.events.EventsHandler;
import cn.charlotte.pit.game.Game;
import cn.charlotte.pit.hologram.HologramFactory;
import cn.charlotte.pit.impl.PitInternalImpl;
import cn.charlotte.pit.item.ItemFactor;
import cn.charlotte.pit.medal.MedalFactory;
import cn.charlotte.pit.minigame.MiniGameController;
import cn.charlotte.pit.mode.Mode;
import cn.charlotte.pit.movement.PlayerMoveHandler;
import cn.charlotte.pit.npc.NpcFactory;
import cn.charlotte.pit.parm.AutoRegister;
import cn.charlotte.pit.perk.PerkFactory;
import cn.charlotte.pit.pet.PetFactory;
import cn.charlotte.pit.quest.QuestFactory;
import cn.charlotte.pit.runnable.*;
import cn.charlotte.pit.util.bossbar.BossBarHandler;
import cn.charlotte.pit.util.chat.CC;
import cn.charlotte.pit.util.command.util.ClassUtil;
import cn.charlotte.pit.util.dependencies.Dependency;
import cn.charlotte.pit.util.dependencies.DependencyManager;
import cn.charlotte.pit.util.dependencies.loaders.LoaderType;
import cn.charlotte.pit.util.dependencies.loaders.ReflectionClassLoader;
import cn.charlotte.pit.util.inventory.InventoryUtil;
import cn.charlotte.pit.util.menu.Menu;
import cn.charlotte.pit.util.menu.MenuUpdateTask;
import cn.charlotte.pit.util.nametag.NametagHandler;
import cn.charlotte.pit.util.random.RandomStrUtil;
import cn.charlotte.pit.util.rank.RankUtil;
import cn.charlotte.pit.util.sign.SignGui;
import cn.charlotte.pit.util.sound.SoundFactory;
import cn.charlotte.pit.util.thread.ThreadHelper;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import dev.meltdown.pit.Meltdown;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.luckperms.api.LuckPerms;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;
import spg.lgdev.iSpigot;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.logging.Level;


/**
 * @author EmptyIrony, Misoryan, AstralStudio
 */

@Getter
@Setter
public class ThePit extends JavaPlugin implements PluginMessageListener {

    public static Mode mode = Mode.Myhic;

    @Setter
    @Getter
    public static PitInternalHook api;

    private static final Logger log = LoggerFactory.getLogger(ThePit.class);
    private static boolean DEBUG_SERVER = false;
    private static String bungeeServerName;
    private static ThePit instance;
    public IActionBarManager actionBarManager;

    private static String random;

    public MongoDB mongoDB;
    public JedisPool jedis;
    public PitConfig pitConfig;
    public EnchantmentFactor enchantmentFactor;
    public NpcFactory npcFactory;
    public NametagHandler nametagHandler;
    public Game game;
    public MedalFactory medalFactory;
    public PerkFactory perkFactory;
    public BuffFactory buffFactory;
    public HologramFactory hologramFactory;
    public EventFactory eventFactory;
    public PlayerMoveHandler movementHandler;
    public QuestFactory questFactory;
    public SignGui signGui;
    public BossBarHandler bossBar;
    public ItemFactor itemFactor = new ItemFactor();
    public RebootRunnable rebootRunnable;
    public MiniGameController miniGameController;
    public SoundFactory soundFactory;
    public PetFactory petFactory;

    public PlayerPointsAPI playerPoints;
    public LuckPerms luckPerms;

    public PointsAPI pointsAPI;

    public ExecutorService savingThreadPool;
    public ScheduledThreadPoolExecutor autoSaveExecutor;

    public static boolean isDEBUG_SERVER() {
        return ThePit.DEBUG_SERVER;
    }

    public static ThePit getInstance() {
        return ThePit.instance;
    }

    public static String getBungeeServerName() {
        return bungeeServerName == null ? random : bungeeServerName.toUpperCase();
    }

    private static void setBungeeServerName(String name) {
        bungeeServerName = name;
    }

    @Override
    public void onLoad() {
        this.loadDepend();
    }

    @Override
    public void onEnable() {
        instance = this;
        random = RandomStrUtil.generateRandomString(3);
        savingThreadPool = Executors.newFixedThreadPool(Math.max(1, Runtime.getRuntime().availableProcessors() / 2),
                new ThreadFactoryBuilder().setNameFormat("ThePit-Saver-%d").setDaemon(true).build());

        saveDefaultConfig();

        iSpigot spigot = new iSpigot();
        Bukkit.getServer().getPluginManager().registerEvents(spigot, this);

        this.loadConfig();
        this.loadDatabase();
        if (getPitConfig().isBetaVersion()) {
            getLogger().info("正在加载 Beta 版本...");
        }

        this.loadListener();
        setApi(PitInternalImpl.INSTANCE);
        try {
            NewConfiguration.INSTANCE.loadFile();
            NewConfiguration.INSTANCE.load();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        PitMain.start();
        Meltdown.init(ThePit.getInstance());
        PitInternalImpl.INSTANCE.setLoaded(true);

        this.loadMenu();
        this.loadNpc();
        this.loadGame();
        this.loadMedals();
        this.loadBuffs();
        this.loadHologram();
        this.loadSound();
        this.loadPerks();
        this.loadEnchantment();
        this.loadEvents();

        try {
            this.loadMoveHandler();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "加载 PlayerMoveHandler 失败", e);
        }

        this.loadQuest();
        this.initBossBar();

        this.initPet();

        this.signGui = new SignGui(this);

        this.rebootRunnable = new RebootRunnable();
        this.rebootRunnable.runTaskTimerAsynchronously(this, 20, 20);

        this.miniGameController = new MiniGameController();
        this.miniGameController.runTaskTimerAsynchronously(this, 1, 1);

        new AutoSaveRunnable().runTaskTimerAsynchronously(this, 20 * 60, 20 * 60);

        new DayNightCycleRunnable().runTaskTimerAsynchronously(this,20,20);

        ThreadHelper.Async(new LeaderBoardRunnable(this));

        try {
            EventsHandler.INSTANCE.loadFromDatabase();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "从数据库加载 EventsHandler 数据失败", e);
        }
        for (World world : Bukkit.getWorlds()) {
            world.setGameRuleValue("keepInventory", "true");
            world.setGameRuleValue("mobGriefing", "false");
            world.setGameRuleValue("doDaylightCycle", "false");
        }
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);
        FixedRewardData.Companion.refreshAll();
        new ProfileLoadRunnable(this);
    }

    @Override
    public void onDisable() {
        log.info("正在禁用 " + getDescription().getName() + " 版本 " + getDescription().getVersion() + "...");
        CC.boardCast("&6&l公告! &7正在关闭服务器并保存数据...");

        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        int playerCount = onlinePlayers.size();
        final CountDownLatch saveLatch = new CountDownLatch(playerCount);
        log.info("开始为 {} 名玩家异步保存任务...", playerCount);

        for (Player player : onlinePlayers) {
            savingThreadPool.submit(() -> {
                PlayerProfile profile;
                try {
                    profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
                    if (profile != null && profile != PlayerProfile.NONE_PROFILE && profile.isLoaded()) {
                        if (profile.isTempInvUsing()) {
                            log.info("正在保存玩家 {} 的档案 (使用临时物品栏)...", player.getName());
                        } else {
                            profile.setInventory(InventoryUtil.playerInventoryFromPlayer(player));
                            log.info("正在保存玩家 {} 的档案 (从玩家更新物品栏)...", player.getName());
                        }
                        profile.save(player);
                    } else if (profile == null || profile == PlayerProfile.NONE_PROFILE) {
                        log.warn("关闭期间未在缓存中找到玩家 {} 的档案。如果之前未持久化，数据可能不会保存。", player.getName());
                    } else {
                        log.warn("玩家 {} 的档案存在，但在关闭期间未标记为已加载。", player.getName());
                    }
                } catch (Exception e) {
                    String playerName = player != null ? player.getName() : "Unknown";
                    UUID playerUUID = player != null ? player.getUniqueId() : null;
                    log.error("保存玩家 {} ({}) 的档案失败", playerName, playerUUID, e);
                } finally {
                    saveLatch.countDown();
                }
            });
        }

        log.info("所有玩家保存任务已调度。正在等待完成...");
        try {
            if (!saveLatch.await(30, TimeUnit.SECONDS)) {
                log.warn("等待玩家数据保存任务完成超时。");
            }
        } catch (InterruptedException e) {
            log.warn("等待玩家数据保存任务时被中断。");
            Thread.currentThread().interrupt();
        }
        log.info("玩家数据保存流程结束。");

        log.info("正在清理打开的菜单...");
        Set<UUID> openMenuUUIDs = new HashSet<>(Menu.currentlyOpenedMenus.keySet());
        for (UUID uuid : openMenuUUIDs) {
            Player player = Bukkit.getPlayer(uuid);
            Menu menu = Menu.currentlyOpenedMenus.remove(uuid);
            if (menu != null && player != null && player.isOnline()) {
                 try {
                     menu.onClose(player);
                 } catch (Exception e) {
                      getLogger().log(Level.WARNING, "为玩家 " + player.getName() + " 执行 menu.onClose 时出错", e);
                 }
            }
        }
        Menu.currentlyOpenedMenus.clear();
        log.info("菜单清理完成。");

        BossBarHandler handlerInstance = BossBarHandler.getInstance();
        if (handlerInstance != null) {
            log.info("正在停止 BossBar 处理器...");
            try {
                handlerInstance.stopUpdateTask();
                log.info("BossBar 处理器已停止。");
            } catch (Exception e) {
                 getLogger().log(Level.SEVERE, "停止 BossBar 处理器时出错", e);
            }
        } else {
             log.warn("[ThePit] 禁用期间未找到 BossBarHandler 实例。");
        }

        log.info("正在关闭线程池...");
        if (savingThreadPool != null && !savingThreadPool.isShutdown()) {
            savingThreadPool.shutdown();
            try {
                if (!savingThreadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                    savingThreadPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                savingThreadPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        if (autoSaveExecutor != null) {
             log.info("正在关闭自动保存执行器...");
             autoSaveExecutor.shutdown();
             try {
                 if (!autoSaveExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                     autoSaveExecutor.shutdownNow();
                 }
             } catch (InterruptedException e) {
                 autoSaveExecutor.shutdownNow();
                 Thread.currentThread().interrupt();
             }
        }

        log.info("正在关闭数据库连接...");
        if (jedis != null) {
            try {
                jedis.close();
                log.info("JedisPool 已关闭。");
            } catch (Exception e) {
                 getLogger().log(Level.SEVERE, "关闭 JedisPool 时出错", e);
            }
        }

        if (mongoDB != null) {
             try {
                 mongoDB.getMongoClient().close();
                log.info("MongoDB 连接已关闭。");
             } catch (Exception e) {
                 getLogger().log(Level.SEVERE, "关闭 MongoDB 连接时出错", e);
             }
        }

        Bukkit.getScheduler().cancelTasks(this);

        log.info(getDescription().getName() + " 版本 " + getDescription().getVersion() + " 已禁用。");
        CC.boardCast("&6&l公告! &7服务器关闭完成。");
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!"BungeeCord".equals(channel)) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();
        if ("GetServer".equals(subchannel)) {
            setBungeeServerName(in.readUTF());
        }
    }

    public static boolean callTimeChange(long time) {
        final OriginalTimeChangeEvent event = new OriginalTimeChangeEvent(time);
        event.callEvent();
        return event.isCancelled();
    }


    private void initPet() {
        this.petFactory = new PetFactory();
        this.petFactory.init();
        Bukkit.getPluginManager().registerEvents(this.petFactory, this);
    }

    private void initBossBar() {
        this.bossBar = new BossBarHandler();
    }

    private void loadSound() {
        this.soundFactory = new SoundFactory();
        this.soundFactory.init();
    }

    private void loadHologram() {
        this.hologramFactory = new HologramFactory();
        this.hologramFactory.init();
    }

    private void loadMenu() {
        this.getServer().getScheduler().runTaskTimer(this, new MenuUpdateTask(), 20L, 20L);
    }

    public void loadEnchantment() {
        this.enchantmentFactor = new EnchantmentFactor();
    }

    public void loadPerks() {
        this.perkFactory = new PerkFactory();
    }

    private void loadMedals() {
        this.medalFactory = new MedalFactory();
        this.medalFactory.init();
        getServer().getPluginManager().registerEvents(this.medalFactory, this);
    }

    private void loadBuffs() {
        this.buffFactory = new BuffFactory();
        this.buffFactory.init();
    }

    private void loadGame() {
        this.game = new Game();
        this.game.initRunnable();
    }

    @SneakyThrows
    private void loadMoveHandler() {
        this.movementHandler = new PlayerMoveHandler();
        iSpigot.INSTANCE.addMovementHandler(this.movementHandler);
    }

    public void loadQuest() {
        this.questFactory = new QuestFactory();
    }

    public void loadListener() {
        Collection<Class<?>> classes = ClassUtil.getClassesInPackage(this, "cn.charlotte.pit");
        classes.stream()
                .filter(clazz -> clazz.isAnnotationPresent(AutoRegister.class))
                .filter(Listener.class::isAssignableFrom)
                .map(clazz -> {
                    try {
                        return (Listener) clazz.newInstance();
                    } catch (Exception ignored) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .forEach(listener -> Bukkit.getPluginManager().registerEvents(listener, ThePit.getInstance()));
    }

    private void loadConfig() {
        log.info("正在加载配置...");
        this.pitConfig = new PitConfig(this);
        this.pitConfig.load();
        log.info("配置加载完成!");

        DEBUG_SERVER = this.pitConfig.isDebugServer();
        if (DEBUG_SERVER) {
            this.getServer().getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void permissionCheckOnJoin(PlayerLoginEvent event) {
                    final Player player = event.getPlayer();
                    if (pitConfig.isDebugServerPublic()) {
                        final String name = RankUtil.getPlayerRealColoredName(player.getUniqueId());
                        if (name.contains("&7") || name.contains("§7")) {
                            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "你所在的用户组当前无法进入此分区!");
                        }
                    } else if (!player.isOp() && !player.hasPermission("thepit.admin")) {
                        event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "此分区当前未开放,开放时间请关注官方公告!");
                    }
                }
            }, this);
        }

        if (pitConfig.isRedisEnable()) {
            jedis = new JedisPool(
                    new GenericObjectPoolConfig(),
                    pitConfig.getRedisAddress(),
                    pitConfig.getRedisPort(),
                    Protocol.DEFAULT_TIMEOUT,
                    pitConfig.getRedisPassword(),
                    false
            );
        }
    }

    private void loadDatabase() {
        log.info("正在加载 MongoDB...");
        this.mongoDB = new MongoDB();
        this.mongoDB.connect();
        log.info("MongoDB 加载完成!");
    }

    private void loadNpc() {
        log.info("正在加载 NPCFactory...");
        this.npcFactory = new NpcFactory();
        log.info("NPCFactory 加载完成!");
    }

    public void loadEvents() {
        log.info("正在加载事件...");
        this.eventFactory = new EventFactory();
        log.info("事件加载完成!");
    }

    private void loadDepend() {
        DependencyManager dependencyManager = new DependencyManager(this, new ReflectionClassLoader(this));
        dependencyManager.loadDependencies(
                new Dependency(
                        "annotations",
                        "org.jetbrains",
                        "annotations",
                        "13.0",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "kotlin-stdlib-common",
                        "org.jetbrains.kotlin",
                        "kotlin-stdlib-common",
                        "1.4.32",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "kotlin-stdlib-jdk7",
                        "org.jetbrains.kotlin",
                        "kotlin-stdlib-jdk7",
                        "1.4.32",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "kotlin-stdlib",
                        "org.jetbrains.kotlin",
                        "kotlin-stdlib",
                        "1.4.32",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "kotlin-stdlib-jdk8",
                        "org.jetbrains.kotlin",
                        "kotlin-stdlib-jdk8",
                        "1.4.32",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "Junit",
                        "junit",
                        "junit",
                        "4.11",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "Apache Http Client",
                        "org.apache.httpcomponents",
                        "httpclient",
                        "4.4",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "Pool2",
                        "org.apache.commons",
                        "commons-pool2",
                        "2.4.2",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "Apache Http Core",
                        "org.apache.httpcomponents",
                        "httpcore",
                        "4.4",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "Apache Logging",
                        "commons-logging",
                        "commons-logging",
                        "1.2",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "MongoDB",
                        "org.mongodb",
                        "mongo-java-driver",
                        "3.12.2",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "Jedis",
                        "redis.clients",
                        "jedis",
                        "2.9.0",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "MongoJack",
                        "org.mongojack",
                        "mongojack",
                        "4.8.1",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "JackSon-Annotations",
                        "com.fasterxml.jackson.core",
                        "jackson-annotations",
                        "2.10.3",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "JackSon-Databind",
                        "com.fasterxml.jackson.core",
                        "jackson-databind",
                        "2.10.3",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "JackSon-Core",
                        "com.fasterxml.jackson.core",
                        "jackson-core",
                        "2.10.3",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "JackSon-DataType",
                        "com.fasterxml.jackson.datatype",
                        "jackson-datatype-jsr310",
                        "2.10.3",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "Bson4Jackson",
                        "de.undercouch",
                        "bson4jackson",
                        "2.9.2",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "Redisson",
                        "org.redisson",
                        "redisson",
                        "3.0.1",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "Netty-common",
                        "io.netty",
                        "netty-common",
                        "4.0.42.Final",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "Netty-codec",
                        "io.netty",
                        "netty-codec",
                        "4.0.42.Final",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "netty-buffer",
                        "io.netty",
                        "netty-buffer",
                        "4.0.42.Final",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "netty-transport",
                        "io.netty",
                        "netty-transport",
                        "4.0.42.Final",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "netty-handler",
                        "io.netty",
                        "netty-handler",
                        "4.0.42.Final",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "reactor-core",
                        "io.projectreactor",
                        "reactor-core",
                        "3.3.9.RELEASE",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "rxjava",
                        "io.reactivex.rxjava2",
                        "rxjava",
                        "2.2.19",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "cache-api",
                        "javax.cache",
                        "cache-api",
                        "1.0.0",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "byte-buddy",
                        "net.bytebuddy",
                        "byte-buddy",
                        "1.10.14",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "jboss-marshalling-river",
                        "org.jboss.marshalling",
                        "jboss-marshalling-river",
                        "2.0.9.Final",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "jodd-bean",
                        "org.jodd",
                        "jodd-bean",
                        "5.1.6",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "snakeyaml",
                        "org.yaml",
                        "snakeyaml",
                        "1.26",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "slf4j-api",
                        "org.slf4j",
                        "slf4j-api",
                        "1.7.30",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "jboss-marshalling-river",
                        "org.jboss.marshalling",
                        "jboss-marshalling-river",
                        "2.0.9.Final",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "jboss-marshalling",
                        "org.jboss.marshalling",
                        "jboss-marshalling",
                        "2.0.9.Final",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "jackson-dataformat-yaml",
                        "com.fasterxml.jackson.dataformat",
                        "jackson-dataformat-yaml",
                        "2.11.2",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "netty-all",
                        "io.netty",
                        "netty-all",
                        "4.0.42.Final",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "guava",
                        "com.google.guava",
                        "guava",
                        "29.0-jre",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "asm",
                        "org.ow2.asm",
                        "asm",
                        "7.3.1",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "asm-commons",
                        "org.ow2.asm",
                        "asm-commons",
                        "7.3.1",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "asm-tree",
                        "org.ow2.asm",
                        "asm-tree",
                        "7.3.1",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "asm-util",
                        "org.ow2.asm",
                        "asm-util",
                        "7.3.1",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "nashorn-core",
                        "org.openjdk.nashorn",
                        "nashorn-core",
                        "15.3",
                        LoaderType.REFLECTION
                )
        );
    }

    /**
     * 使用 SLF4J Logger 输出 info 级别日志（旧方法，现推荐使用 CC.log）。
     * @param s 要记录的字符串
     * @deprecated 请改用 CC.log()
     */
    @Deprecated
    public void info(String s) {
        log.info(s);
    }
}
