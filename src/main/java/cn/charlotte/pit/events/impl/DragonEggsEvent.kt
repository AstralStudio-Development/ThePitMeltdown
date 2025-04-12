package cn.charlotte.pit.events.impl

import cn.charlotte.pit.ThePit
import cn.charlotte.pit.config.NewConfiguration
import cn.charlotte.pit.data.PlayerProfile
import cn.charlotte.pit.events.IEvent
import cn.charlotte.pit.events.INormalEvent
import cn.charlotte.pit.util.chat.CC
import cn.charlotte.pit.util.hologram.Hologram
import cn.charlotte.pit.util.hologram.HologramAPI
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import java.util.*
import java.util.logging.Level

/**
 * @author Araykal
 * @since 2025/1/17
 */
class DragonEggsEvent : IEvent, INormalEvent, Listener {
    private var eggLocation: Location? = null
    private var clicks: Int = 0
    private var firstHologram: Hologram? = null
    private var secondHologram: Hologram? = null
    private var isActive = false
    private var isClick = false
    private val plugin = ThePit.getInstance()

    companion object {
        private const val MAX_CLICKS = 230
        private const val CLICK_THRESHOLD = 50
        private const val SEARCH_RADIUS = 10
        private const val MAX_ATTEMPTS = 20
    }

    override fun getEventInternalName(): String = "dragon_egg"

    override fun getEventName(): String = "&5龙蛋"

    override fun requireOnline(): Int = NewConfiguration.eventOnlineRequired[eventInternalName]!!

    private fun registerEvents() {
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    private fun unregisterEvents() {
        HandlerList.unregisterAll(this)
    }

    private fun prepareNewLocation() {
        despawnHolograms()
        eggLocation?.block?.type = Material.AIR
    }

    private fun calculateOffset(origin: Location, random: Random): Int {
        return random.nextInt(31) - SEARCH_RADIUS
    }

    override fun onActive() {
        eggLocation = plugin.pitConfig.eggLoc ?: run {
            Bukkit.broadcastMessage(CC.translate("&5&l龙蛋！ &7活动区域未设置，请联系管理员设置！"))
            plugin.eventFactory.inactiveEvent(this)
            return
        }
        isActive = true
        isClick = true
        registerEvents()
        CC.boardCast(CC.translate("&5&l龙蛋！ &d龙蛋已在中心点位刷新,请前往点击！"))
        setEggLocation(eggLocation!!)
        playSoundToOnlinePlayers(Sound.ENDERDRAGON_GROWL, 1.5f, 1.5f)
        Bukkit.getScheduler().runTaskLater(plugin, {
            if (isActive) {
                plugin.eventFactory.inactiveEvent(this)
            }
        }, 8 * 20 * 60)
    }

    private fun setEggLocation(location: Location) {
        prepareNewLocation()
        eggLocation = location
        eggLocation!!.block.type = Material.DRAGON_EGG
        reCreateHologram(location)
    }

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        if (!isActive || event.clickedBlock?.type != Material.DRAGON_EGG) return
        event.isCancelled = true
        if (!isClick) {
            return
        }
        val player = event.player
        val p = PlayerProfile.getPlayerProfileByUuid(player.uniqueId)
        if (p == null || p == PlayerProfile.NONE_PROFILE) {
            plugin.logger.warning("[DragonEggsEvent] Failed to load profile for ${player.name} on interact.")
            return
        }

        val random = Random()
        val randomMultiplier = random.nextInt(3) + 1
        val (coins, exp) = when (clicks) {
            0 -> Pair(3.0 * randomMultiplier, 3.0 * (random.nextInt(5) + 1))
            else -> Pair(clicks * 0.5 * randomMultiplier, clicks * 0.5 * (random.nextInt(5) + 1))
        }

        if (clicks < MAX_CLICKS) {
            p.coins += coins
            p.experience += exp
            p.grindCoins(coins)
            Bukkit.getScheduler().runTask(plugin) { p.applyExperienceToPlayer(player) }
            Bukkit.getScheduler().runTaskAsynchronously(plugin) { p.save(null) }
            player.playSound(player.location, Sound.NOTE_PLING, 1.5f, 1.5f)
            player.sendMessage(CC.translate("&5&l龙蛋！ &7点击龙蛋 获得 &e${String.format("%.2f", coins)} &6金币 &e${String.format("%.2f", exp)} &b经验&7"))
            addClicks()
            handleClickEvents()
        } else {
            player.sendMessage(CC.translate("&5&l龙蛋！ &7最大点击次数已达到！"))
        }
    }

    private fun handleClickEvents() {
        if (clicks >= MAX_CLICKS) {
            if (isActive) plugin.eventFactory.inactiveEvent(this)
        } else if (clicks % CLICK_THRESHOLD == 0) {
            isClick = false
            setNewLocation()
        }
    }

    private fun setNewLocation() {
        prepareNewLocation()
        try {
            val newLoc = findRandomLocation(eggLocation ?: plugin.pitConfig.eggLoc!!)
            setEggLocation(newLoc)
            CC.boardCast("&5&l龙蛋！ &7龙蛋已被移动到了新的位置！")
            playSoundToOnlinePlayers(Sound.ENDERMAN_TELEPORT, 1.5f, 1.5f)
            isClick = true
        } catch (e: Exception) {
            plugin.logger.log(Level.SEVERE, "[DragonEggsEvent] Failed to find or set new egg location! Ending event.", e)
            if (isActive) plugin.eventFactory.inactiveEvent(this)
        }
    }

    private fun playSoundToOnlinePlayers(sound: Sound, volume: Float, pitch: Float) {
        Bukkit.getOnlinePlayers().forEach {
            it.playSound(it.location, sound, volume, pitch)
        }
    }

    private fun reCreateHologram(location: Location) {
        despawnHolograms()
        try {
            firstHologram = HologramAPI.createHologram(location.clone().add(0.5, 2.4, 0.5), "§a$clicks")
            secondHologram = HologramAPI.createHologram(location.clone().add(0.5, 2.0, 0.5), "§e§l点击")
            firstHologram?.spawn()
            secondHologram?.spawn()
        } catch (e: Exception) {
            plugin.logger.log(Level.WARNING, "[DragonEggsEvent] Failed to create holograms", e)
        }
    }

    private fun despawnHolograms() {
        try {
            firstHologram?.deSpawn()
            secondHologram?.deSpawn()
        } catch (e: Exception) {
            plugin.logger.log(Level.WARNING, "[DragonEggsEvent] Error despawning holograms", e)
        }
        firstHologram = null
        secondHologram = null
    }

    private fun addClicks() {
        clicks++
        try {
            firstHologram?.text = "§a$clicks"
        } catch (e: Exception) {
            plugin.logger.log(Level.WARNING, "[DragonEggsEvent] Failed to update hologram text", e)
        }
    }

    override fun onInactive() {
        if (!isActive) return
        isActive = false
        unregisterEvents()
        cleanup()
        playSoundToOnlinePlayers(Sound.ENDERDRAGON_DEATH, 1.5f, 1.5f)
        CC.boardCast(CC.translate("&5&l龙蛋！ &7活动已结束！"))
    }

    private fun cleanup() {
        try {
            eggLocation?.block?.type = Material.AIR
        } catch (e: Exception) {
            plugin.logger.log(Level.WARNING, "[DragonEggsEvent] Error removing dragon egg block", e)
        }
        despawnHolograms()
        eggLocation = null
        clicks = 0
        isClick = false
    }

    private fun findRandomLocation(origin: Location): Location {
        val random = Random()
        var attempts = 0
        var newX: Double = origin.x
        var newZ: Double = origin.z
        var newLocation: Location
        val world = origin.world ?: Bukkit.getWorlds().first()

        while (attempts < MAX_ATTEMPTS) {
            newX = origin.x + calculateOffset(origin, random)
            newZ = origin.z + calculateOffset(origin, random)
            newLocation = Location(world, newX, origin.y, newZ)

            if (newLocation.block.type == Material.AIR) {
                return newLocation
            }
            attempts++
        }
        plugin.logger.warning("[DragonEggsEvent] Failed to find a suitable AIR location after $MAX_ATTEMPTS attempts. Using last tried location.")
        return Location(world, newX, origin.y, newZ)
    }
}