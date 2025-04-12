package cn.charlotte.pit

import org.bukkit.Bukkit

object PitMain {
    private var hook: PitHook? = null

    @JvmStatic
    fun start() {
        Bukkit.getScheduler().runTask(ThePit.getInstance()) {
            hook = PitHook
            hook!!.init()
        }
    }
}
