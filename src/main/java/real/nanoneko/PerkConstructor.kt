package real.nanoneko

import real.nanoneko.register.IMagicLicense

/**
 * @author Araykal
 * @since 2025/1/31
 */
object PerkConstructor {
    private val perks: MutableList<Class<*>> = mutableListOf()

    fun getPerks(): List<Class<*>> {
        return perks
    }

    fun addPerk(enchantment: Class<*>) {
        perks.add(enchantment)
    }

    fun removePerk(enchantment: Class<*>) {
        perks.remove(enchantment)
    }
}