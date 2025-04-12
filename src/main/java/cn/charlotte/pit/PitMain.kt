package cn.charlotte.pit

import cn.charlotte.pit.impl.PitInternalImpl
import cn.charlotte.pit.impl.PitInternalImpl.loaded
import dev.meltdown.pit.Meltdown
import org.bukkit.Bukkit

object PitMain {
    private var hook: PitHook? = null

    @JvmStatic
    fun start() {
        Bukkit.getScheduler().runTask(ThePit.getInstance()) {
            ThePit.getInstance().loadListener()
            ThePit.setApi(PitInternalImpl)
            hook = PitHook
            hook!!.init()

            Meltdown.init(ThePit.getInstance())
            loaded = true
        }
    }
}
