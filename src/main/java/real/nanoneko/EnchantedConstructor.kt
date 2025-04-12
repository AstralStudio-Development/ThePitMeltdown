package real.nanoneko

import real.nanoneko.register.IMagicLicense

object EnchantedConstructor {
    private val enchantments: MutableList<Class<*>> = mutableListOf()

    fun getEnchantments(): List<Class<*>> {
        return enchantments
    }

    fun addEnchantment(enchantment: Class<*>) {
        enchantments.add(enchantment)
    }

    fun removeEnchantment(enchantment: Class<*>) {
        enchantments.remove(enchantment)
    }
}