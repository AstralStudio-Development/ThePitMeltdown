package cn.charlotte.pit.data;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.UtilKt;
import cn.charlotte.pit.api.PitInternalHook;
import cn.charlotte.pit.buff.BuffData;
import cn.charlotte.pit.data.sub.*;
import cn.charlotte.pit.event.PitGainCoinsEvent;
import cn.charlotte.pit.event.PitGainRenownEvent;
import cn.charlotte.pit.event.PitStreakKillChangeEvent;
import cn.charlotte.pit.events.genesis.team.GenesisTeam;
import cn.charlotte.pit.medal.impl.challenge.HundredLevelMedal;
import cn.charlotte.pit.quest.AbstractQuest;
import cn.charlotte.pit.util.chat.CC;
import cn.charlotte.pit.util.chat.MessageType;
import cn.charlotte.pit.util.chat.TitleUtil;
import cn.charlotte.pit.util.cooldown.Cooldown;
import cn.charlotte.pit.util.inventory.InventoryUtil;
import cn.charlotte.pit.util.level.LevelUtil;
import cn.charlotte.pit.util.random.RandomUtil;
import cn.charlotte.pit.util.rank.RankUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @Author: EmptyIrony
 * @Date: 2020/12/29 23:04
 */

@JsonIgnoreProperties(ignoreUnknown = true, value = {
        "inArena",
        "streakKills",
        "strikeAssist",
        "combatTimer",
        "editingMode",
        "damageReduced",
        "damageMap",
        "strengthNum",
        "strengthTimer",
        "bountyCooldown",
        "bountyStreak",
        "lastKilledPlayer",
        "killRecap",
        "screenShare",
        "screenShareQQ",
        "mailData",
        "tempInvUsing",
        "nicked",
        "nickPrestige",
        "nickLevel",
        "invBackups",
        "noDamageAnimations",
        "liteStreakKill",
        "lastActionTimestamp",
        "buffData",
        "streakCooldown",
        "streakCount",
        "bot",
        "lastDamageAt"
})
@Data
public class PlayerProfile {

    public final static PlayerProfile NONE_PROFILE = new PlayerProfile(UUID.randomUUID(), "NotLoadPlayer");

    private final static Map<UUID, PlayerProfile> cacheProfile = new HashMap<>();
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(PlayerProfile.class);
    public int prestige;
    public List<String> claimedMail;
    public PlayerMailData mailData;
    @JsonIgnore
    public boolean loaded;
    public String playerName;
    public String uuid;
    public String lowerName;
    public long registerTime;
    public long lastLoginTime;
    public long lastLogoutTime;
    public long totalPlayedTime;
    public long yearPlayedTime;
    public long monthPlayedTime;
    public long weekPlayedTime;
    public long todayPlayedTime;
    public int kills;
    public int assists;
    public int deaths;
    public int highestStreaks;
    public long totalDamage;
    public long meleeTotalDamage;
    public long arrowTotalDamage;
    public long hurtDamage;
    public long meleeHurtDamage;
    public long bowHurtDamage;
    public int meleeAttack;
    public int shootAttack;
    public int meleeHit;
    public int bowHit;
    public int rodUsed;
    public int rodHit;
    public int goldPicked;
    public int fishingNumber;
    public int goldenHeadEaten;
    public double experience;
    public double coins;
    public int renown;
    public int bounty;
    public int actionBounty;
    public double respawnTime;
    public PlayerInv inventory;
    public PlayerEnderChest enderChest;
    public int enderChestRow;

    //每次都遍历查询，效率低下
    //所以专用Map降低大O复杂度
    @Deprecated
    private List<PerkData> unlockedPerk;
    public Map<String, PerkData> unlockedPerkMap = new HashMap<>();
    @Deprecated
    public List<PerkData> boughtPerk;
    public Map<String, PerkData> boughtPerkMap = new HashMap<>();

    public Set<String> usedCdk;
    public Map<Integer, PerkData> chosePerk;
    public double totalExp;
    public List<String> autoBuyButtons;
    public TradeLimit tradeLimit;
    public MedalData medalData;
    public QuestLimit questLimit;
    public OfferData offerData;
    //累计获得硬币
    public double grindedCoins;
    public PlayerOption playerOption;
    public PlayerBanData playerBanData;
    public boolean supporter;
    public boolean supporterGivenByAdmin;
    //补偿信息
    public int remedyLevel;
    public double remedyExp;
    public String remedyDate;
    public int totalFishTimes;
    public int totalFishTreasureTimes;
    public int totalFishTrashTimes;
    //当前的任务
    public QuestData currentQuest;
    //上一次的任务
    public QuestData lastQuest;
    //是否开启了夜晚任务
    public boolean nightQuestEnable;
    public QuestCenter questCenter;
    public GenesisData genesisData;
    public List<String> currentQuestList;
    public double maxHealth;
    public int foodLevel;
    public float moveSpeed;
    public String enchantingItem;
    public String enchantingScience;
    //玩家是否在退出其他pit服务器，用于数据保存用，防止产生脏数据

    public String enchantingBook;
    public boolean login;
    public WipedData wipedData;
    //shouldn't save fields
    public BuffData buffData;
    public boolean inArena;
    public double streakKills;
    public Cooldown combatTimer;
    public boolean editingMode;
    public double damageReduced;
    public Map<UUID, DamageData> damageMap;
    public int strengthNum;
    public Cooldown strengthTimer;
    public UUID lastKilledPlayer;
    public Cooldown bountyCooldown;
    public int bountyStreak;
    public KillRecap killRecap;
    public boolean screenShare;
    public String screenShareQQ;
    public boolean nicked;
    public int nickPrestige;
    public int nickLevel;
    public boolean tempInvUsing;
    public boolean noDamageAnimations;
    public double liteStreakKill;
    public long lastActionTimestamp;

    public double goldStackAddon = 0.0;
    public double goldStackMax = 0.5;

    public double xpStackAddon = 0.0;
    public double xpStackMax = 1.0;

    public List<PlayerInvBackup> invBackups;

    public int todayCompletedUber;
    public long todayCompletedUberLastRefreshed;

    public int profileFormatVersion = 0;

    public Cooldown streakCooldown;
    public int streakCount;

    public boolean bot;

    public long lastDamageAt = -1L;

    public HashMap<String, Double> extraMaxHealth = new HashMap<>();

    public KingsQuestsData kingsQuestsData = new KingsQuestsData();

    public PlayerProfile(UUID uuid, String playerName) {
        //调用默认构造函数，初始化赋值
        this();

        this.uuid = uuid.toString();
        this.playerName = playerName;
        this.lowerName = playerName.toLowerCase();
        this.mailData = new PlayerMailData(uuid, playerName);
    }

    public PlayerProfile() {
        this.inventory = new PlayerInv();
        this.enderChest = new PlayerEnderChest();
        this.killRecap = new KillRecap();
        this.buffData = new BuffData();
        this.combatTimer = new Cooldown(0);
        this.damageMap = new HashMap<>();
        this.unlockedPerk = new ArrayList<>();
        this.boughtPerk = new ArrayList<>();
        this.strengthTimer = new Cooldown(0);
        this.usedCdk = new HashSet<>();
        this.enderChestRow = 3;
        this.respawnTime = 0.1;

        if (ThePit.isDEBUG_SERVER()) {
            this.coins = 10000000;
            this.renown = 100000;
            this.prestige = 20;
            this.experience = LevelUtil.getLevelTotalExperience(20, 120);
        } else {
            this.coins = 5000;
        }

        this.screenShare = false;
        this.screenShareQQ = "none";

        this.chosePerk = new HashMap<>();

        this.autoBuyButtons = new ArrayList<>();

        this.medalData = new MedalData();

        this.tradeLimit = new TradeLimit();
        this.questLimit = new QuestLimit();
        this.offerData = new OfferData();

        this.playerOption = new PlayerOption();
        this.playerBanData = new PlayerBanData();
        this.bountyCooldown = new Cooldown(0);
        this.currentQuestList = new ArrayList<>();
        this.genesisData = new GenesisData();
        this.invBackups = new ArrayList<>();
        this.claimedMail = new ArrayList<>();

        this.nightQuestEnable = false;

        this.supporter = false;
        this.supporterGivenByAdmin = false;

        this.totalFishTimes = 0;
        this.totalFishTreasureTimes = 0;
        this.totalFishTrashTimes = 0;

        this.foodLevel = 20;
        this.maxHealth = 20.0d;
        this.moveSpeed = 0.2F;

        //Level 0 : NoRemedy
        //Level 1 : Remedy + 1 Perk
        //Level 2 : Remedy + 2 Perks
        this.remedyLevel = 0;
        this.remedyExp = 0;
        this.remedyDate = "none";

        this.mailData = new PlayerMailData();

        this.loaded = false;
    }

    public static Map<UUID, PlayerProfile> getCacheProfile() {
        return PlayerProfile.cacheProfile;
    }

    @JsonIgnore
    public static PlayerProfile getPlayerProfileByUuid(UUID uuid) {

        PlayerProfile profile = cacheProfile.get(uuid);
        if (profile == null) {
//            Player player = Bukkit.getPlayer(uuid);
//            if (player != null && player.isOnline()) {
//                player.kickPlayer("你的档案因未知异常没有被加载,请尝试稍等一会儿重新进入游戏!");
//            }
//            throw new RuntimeException(uuid.toString() + "'s Player profile is not loaded");
            return NONE_PROFILE;
        }
        return profile;
    }

    /**
     * 该方法用于查找玩家，如果玩家可能离线时请使用本方法
     * 注意！请异步调用本方法，如果在主线程上调用会抛异常
     *
     * @param uuid 寻找的玩家UUID
     * @return 目标玩家玩家档案，如果该玩家未注册，则返回null
     */
    @JsonIgnore
    public static PlayerProfile getOrLoadPlayerProfileByUuid(UUID uuid) {
        PlayerProfile profile = cacheProfile.get(uuid);
        if (profile != null) {
            return profile;
        }
        return loadPlayerProfileByUuid(uuid);
    }

    public static PlayerProfile loadPlayerProfileByUuid(UUID uuid) {
        if (Bukkit.getServer().isPrimaryThread()) {
            throw new RuntimeException("Shouldn't load profile on primary thread!");
        }

        PlayerProfile playerProfile = ThePit.getInstance()
                .getMongoDB()
                .getProfileCollection()
                .find(Filters.eq("uuid", uuid.toString()))
                .first();

        if (playerProfile != null) {
            //load mail
            loadMail(playerProfile, uuid);

            //load inv backup
            try {
                final FindIterable<PlayerInvBackup> invBackups = ThePit.getInstance()
                        .getMongoDB()
                        .getInvCollection()
                        .find(Filters.eq("uuid", uuid.toString()));

                long lastTime = 0;
                for (PlayerInvBackup backup : invBackups) {
                    if (Math.abs(backup.getTimeStamp() - lastTime) < 10 * 60 * 1000) {
                        lastTime = backup.getTimeStamp();
                        ThePit.getInstance()
                                .getMongoDB()
                                .getInvCollection()
                                .deleteOne(Filters.eq("backupUuid", backup.getBackupUuid()));
                        continue;
                    }
                    lastTime = backup.getTimeStamp();
                    playerProfile.getInvBackups().add(backup);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return playerProfile;
    }

    /**
     * 该方法用于查找玩家，如果玩家可能离线时请使用本方法
     * 注意！请异步调用本方法，如果在主线程上调用会抛异常
     *
     * @param name 目标玩家名字
     * @return 目标玩家玩家档案，如果该玩家未注册，则返回null
     */
    @JsonIgnore
    public static PlayerProfile getOrLoadPlayerProfileByName(String name) {
        for (Map.Entry<UUID, PlayerProfile> entry : cacheProfile.entrySet()) {
            if (entry.getValue().getPlayerName().equalsIgnoreCase(name)) {
                return entry.getValue();
            }
        }
        if (Bukkit.getServer().isPrimaryThread()) {
            throw new RuntimeException("Shouldn't load profile on primary thread!");
        }

        PlayerProfile playerProfile = ThePit.getInstance()
                .getMongoDB()
                .getProfileCollection()
                .find(Filters.eq("lowerName", name.toLowerCase()))
                .first();

        return playerProfile;
    }

    @JsonIgnore
    public static PlayerProfile getPlayerProfileByName(String name) {
        for (Map.Entry<UUID, PlayerProfile> entry : cacheProfile.entrySet()) {
            if (entry.getValue().getPlayerName().equalsIgnoreCase(name)) {
                return entry.getValue();
            }
        }
        Player player = Bukkit.getPlayer(name);
        if (player != null && player.isOnline()) {
            player.sendMessage(CC.translate("&c在加载您的天坑乱斗数据时出现了一个问题,您可以尝试再次进入游戏以重试."));
            player.kickPlayer("加载您的天坑乱斗数据时出现了一个问题,您可以尝试再次进入游戏以重试.");
        }
        throw new RuntimeException(name + "'s Player profile not load");
    }

    public static void saveAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
                if (profile.isLoaded()) {
                    profile.setInventory(InventoryUtil.playerInventoryFromPlayer(player));
                    profile.save(null);
                }
            } catch (Exception e) {
                e.printStackTrace();
                CC.printError(player, e);
            }
        }
    }

    public static void loadMail(PlayerProfile playerProfile, UUID uuid) {
        PlayerMailData mailData = ThePit.getInstance()
                .getMongoDB()
                .getMailCollection()
                .find(Filters.eq("uuid", uuid.toString()))
                .first();

        if (mailData == null) {
            mailData = new PlayerMailData();
            mailData.setName(playerProfile.playerName);
            mailData.setNameLower(playerProfile.lowerName);
            mailData.setUuid(playerProfile.uuid);
        }

        mailData.cleanUp();

        playerProfile.setMailData(mailData);
    }

    public void init() {
        cacheProfile.put(this.getPlayerUuid(), this);
    }

    public PlayerProfile save(Player player) {
        this.totalExp = experience;
        for (int i = 0; i < prestige; i++) {
            this.totalExp = totalExp + LevelUtil.getLevelTotalExperience(i, 120);
        }

        if (!this.loaded) {
            return this;
        }


        saveData(player);
        return this;
    }

    public void saveData(Player player) {
        final long now = System.currentTimeMillis();
        if (player != null){
            this.setInventory(InventoryUtil.playerInventoryFromPlayer(player));
        }
        //if (invBackups.isEmpty() || invBackups.stream().noneMatch(backup -> now - backup.getTimeStamp() < 10 * 60 * 1000)) {
        final PlayerInvBackup backup = new PlayerInvBackup();
        backup.setUuid(this.uuid);
        backup.setTimeStamp(now);
        backup.setBackupUuid(UUID.randomUUID().toString());
        backup.setInv(this.inventory);
        backup.setChest(this.enderChest);
        backup.setTimeStamp(System.currentTimeMillis());

        backup.save();
//        gcBackups(gcBackupIterators(), this, true);
//           gcBackups(invBackups,this,false);
// }

        ThePit.getInstance()
                .getMongoDB()
                .getProfileCollection()
                .replaceOne(Filters.eq("uuid", this.uuid), this, new ReplaceOptions().upsert(true));
    }
    public PlayerProfile load() {

        PlayerProfile profile = PlayerProfile.loadPlayerProfileByUuid(this.getPlayerUuid());
        if (profile == null) {
            this.registerTime = System.currentTimeMillis();
            //refresh quests - start
            this.refreshQuest();
            //refresh quests - end


            this.loaded = true;
            return this;
        }


        //refresh quests - start
        profile.refreshQuest();
        profile.refreshGenesisData();
        //refresh quests - end

        profile.loaded = true;

        Bukkit.getScheduler().runTask(ThePit.getInstance(), () -> {
            try {
                final Player player = Bukkit.getPlayer(profile.getPlayerUuid());
                if (player != null) {
                    if (!player.getName().equals(player.getDisplayName())) {
                        profile.nicked = true;
                        if (profile.prestige <= 0) {
                            profile.nickPrestige = 0;
                        } else {
                            profile.nickPrestige = RandomUtil.random.nextInt(profile.getPrestige()) + 1;
                        }

                        profile.nickLevel = RandomUtil.random.nextInt(profile.getLevel() + 1);
                    }
                }
            } catch (Exception e) {
                Bukkit.getOnlinePlayers()
                        .forEach(player -> CC.printError(player, e));
            }
        });

        return profile;
    }

    private void refreshQuest() {
        if (this.getCurrentQuestList().size() == 0) {
            List<AbstractQuest> quests = ThePit.getInstance()
                    .getQuestFactory()
                    .getQuests();

            List<AbstractQuest> list = new ArrayList<>(quests);
            Collections.shuffle(list);

            for (int i = 0; i < 3; i++) {

                AbstractQuest quest = list.get(i);
                int level = RandomUtil.random.nextInt(quest.getMaxLevel()) + 1;
                this.getCurrentQuestList().add(quest.getQuestInternalName() + ":" + level);

            }
        }
    }

    public boolean isBanned() {
        return this.playerBanData.getEnd() > System.currentTimeMillis();
    }

    public void checkUpdate(double newExperience) {
        final Player player = Bukkit.getPlayer(this.getPlayerUuid());
        if (player != null) {
            applyExperienceToPlayer(player);
            final int newLevel = LevelUtil.getLevelByExp(prestige, newExperience);
            final int oldLevel = this.getLevel();

            if (newLevel > oldLevel) {

                if (newLevel >= 100) {
                    new HundredLevelMedal().setProgress(this, 1);
                }

                final String newLevelTag = LevelUtil.getLevelTag(this.prestige, newExperience);
                final String oldLevelTag = LevelUtil.getLevelTag(this.prestige, this.experience);

                CC.send(MessageType.MISC, player, "&b&l天坑升级! " + oldLevelTag + " &7➠ " + newLevelTag);
                TitleUtil.sendTitle(player, CC.translate("&6&l升 级!"), CC.translate(oldLevelTag + " &7➠ " + newLevelTag), 10, 20, 10);
                player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1);
            }
        }
    }

    @JsonIgnore
    public void applyExperienceToPlayer(Player player) {
        player.setLevel(getLevel());
        if (getLevel() >= 120) {
            this.experience = LevelUtil.getLevelTotalExperience(this.prestige, 120);
            player.setExp(1);
            return;
        }
        player.setExp(LevelUtil.getLevelProgress(this.prestige, this.experience));
    }

    @JsonIgnore
    public int getLevel() {
        return LevelUtil.getLevelByExp(this.prestige, this.experience);
    }

    @JsonIgnore
    public String getPrestigeColor() {
        return LevelUtil.getPrestigeColor(prestige);
    }

    @JsonIgnore
    public String geLevelColor() {
        return LevelUtil.getLevelColor(this.getLevel());
    }

    @JsonIgnore
    public UUID getPlayerUuid() {
        return UUID.fromString(this.uuid);
    }

    @JsonIgnore
    public void grindCoins(double coins) {
        final Player player = Bukkit.getPlayer(this.getPlayerUuid());
        if (player != null) {
            new PitGainCoinsEvent(player, coins).callEvent();
        }
        this.setGrindedCoins(this.getGrindedCoins() + coins);
    }

    @JsonIgnore
    public void refreshGenesisData() {
        if (ThePit.getInstance().getPitConfig().getGenesisSeason() != this.getGenesisData().getSeason()) {
            int boostTier = this.getGenesisData().getBoostTier();
            GenesisTeam team = this.getGenesisData().getTeam();
            this.setGenesisData(new GenesisData());
            this.getGenesisData().setBoostTier(boostTier);
            this.getGenesisData().setTeam(team);
            this.getGenesisData().setSeason(ThePit.getInstance().getPitConfig().getGenesisSeason());
        }
    }

    public boolean wipe(String reason) {
        PlayerProfile profile = new PlayerProfile(this.getPlayerUuid(), this.playerName);

        Player player = Bukkit.getPlayer(UUID.fromString(uuid));
        if (player == null || !player.isOnline()) {
            if (this.isLogin()) {
                return false;
            }
            WipedData wipedData = new WipedData();
            wipedData.setWipedProfile(this);
            wipedData.setReason(reason);
            wipedData.setWipedTimestamp(System.currentTimeMillis());

            profile.setWipedData(wipedData);

            ThePit.getInstance()
                    .getMongoDB()
                    .getProfileCollection()
                    .replaceOne(Filters.eq("uuid", this.uuid), profile, new ReplaceOptions().upsert(true));
        } else {
            if (!this.loaded) {
                return false;
            }
            WipedData wipedData = new WipedData();
            wipedData.setWipedProfile(this);
            wipedData.setWipedTimestamp(System.currentTimeMillis());
            wipedData.setReason(reason);
            profile.setRegisterTime(System.currentTimeMillis());

            profile.setWipedData(wipedData);
            profile.setLoaded(true);

            ThePit.getInstance()
                    .getMongoDB()
                    .getProfileCollection()
                    .replaceOne(Filters.eq("uuid", this.uuid), profile, new ReplaceOptions().upsert(true));

            cacheProfile.put(this.getPlayerUuid(), profile);

            Bukkit.getScheduler().runTask(ThePit.getInstance(), () -> {
                player.kickPlayer("working..");
            });
        }
        return true;
    }

    public boolean unWipe() {
        WipedData wipedData = this.wipedData;
        if (wipedData == null) {
            return false;
        }

        Player player = Bukkit.getPlayer(this.getPlayerUuid());
        if (player != null && player.isOnline()) {
            Bukkit.getScheduler().runTask(ThePit.getInstance(), () -> {
                player.kickPlayer("working..");
            });
        }

        ThePit.getInstance()
                .getMongoDB()
                .getProfileCollection()
                .replaceOne(Filters.eq("uuid", this.uuid), wipedData.getWipedProfile(), new ReplaceOptions().upsert(true));

        return true;
    }

    public String getFormattedName() {
        return getFormattedLevelTag() + " " + RankUtil.getPlayerColoredName(this.getPlayerUuid());
    }

    public String getFormattedNameWithRoman() {
        return getFormattedLevelTagWithRoman() + " " + RankUtil.getPlayerColoredName(this.getPlayerUuid());
    }

    public String getFormattedLevelTag() {
        if (nicked) {
            return LevelUtil.getLevelTag(this.nickPrestige, this.nickLevel);
        }
        return LevelUtil.getLevelTag(this.getPrestige(), this.getLevel());
    }

    public String getFormattedLevelTagWithRoman() {
        if (nicked) {
            return LevelUtil.getLevelTagWithRoman(this.nickPrestige, this.nickLevel);
        }
        return LevelUtil.getLevelTagWithRoman(this.getPrestige(), this.getLevel());
    }

    public void addLiteStreakKill(double value) {
        this.liteStreakKill += value;
        if (liteStreakKill > 1) {
            final int streakKill = (int) liteStreakKill;
            liteStreakKill = liteStreakKill - streakKill;

            this.streakKills += liteStreakKill;
        }
    }

    public int addAndGetStreakNumberShort() {
        if (streakCooldown == null || streakCooldown.hasExpired()) {
            streakCount = 0;
            streakCount++;
            streakCooldown = new Cooldown(10, TimeUnit.SECONDS);
            return streakCount;
        }

        streakCooldown = new Cooldown(10, TimeUnit.SECONDS);
        streakCount++;

        return streakCount;
    }

    @JsonIgnore
    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public void setExperience(double experience) {
        if (loaded) {
            checkUpdate(experience);
        }
        this.experience = experience;
    }

    public void setPrestige(int prestige) {
        if (ThePit.isDEBUG_SERVER()) {
            prestige = 20;
        } else {
            this.prestige = prestige;
        }
    }

    public void setRenown(int renown) {
        final Player player = Bukkit.getPlayer(this.getPlayerUuid());
        if (player != null && renown > this.renown) {
            new PitGainRenownEvent(player, renown - this.renown).callEvent();
        }
        this.renown = renown;
    }

    public void setBounty(int bounty) {
        if (bounty >= 5000) {
//            final Player player = Bukkit.getPlayer(this.getPlayerUuid());
//            if (player != null) {
//                new MaxBountyMedal().setProgress(PlayerProfile.getPlayerProfileByUuid(player.getUniqueId()), 1);
//            }
        }
        this.bounty = bounty;
    }

    public void setInventory(PlayerInv inv) {
        if (this.tempInvUsing) {
            return;
        }

        this.inventory = inv;
    }

    @Deprecated
    public List<PerkData> getUnlockedPerk() {
        return this.unlockedPerk;
    }

    @Deprecated
    public List<PerkData> getBoughtPerk() {
        return this.boughtPerk;
    }

    public boolean isSupporter() {
        PitInternalHook api = ThePit.getApi();
        if (api == null) return false;

        if (supporter && !api.getRemoveSupportWhenNoPermission()) return true;

        Player player = Bukkit.getPlayerExact(playerName);
        if (player == null) return false;

        return player.hasPermission(api.getPitSupportPermission());
    }

    public void setInArena(boolean inArena) {
        if (inArena && !this.inArena) {
            final Player player = Bukkit.getPlayer(getPlayerUuid());
            if (player != null) {
                UtilKt.releaseItem(player);
            }
        }

        this.inArena = inArena;
    }

    public void setStreakKills(double kills) {
        final PitStreakKillChangeEvent event = new PitStreakKillChangeEvent(this, this.streakKills, kills);
        if (kills > 0) {
            event.callEvent();
        }
        if (event.isCancelled()) {
            return;
        }

        this.streakKills = kills;
    }

    public double getExtraMaxHealthValue() {
        return extraMaxHealth
                .values()
                .stream()
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    public boolean isInArena() {
        return inArena;
    }

    public boolean isLoaded() {
        return loaded;
    }
}
