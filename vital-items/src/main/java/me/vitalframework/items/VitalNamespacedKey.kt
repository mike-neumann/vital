package me.vitalframework.items

import org.bukkit.NamespacedKey

/**
 * Defines namespaced keys for various purposes within the Vital plugin.
 * This interface centralizes key definitions to avoid conflicts and ensure consistency.
 */
object VitalNamespacedKey {
    /**
     * A namespaced key for storing a unique identifier on ItemStacks.
     */
    @JvmField
    val ITEM_UUID: NamespacedKey? = NamespacedKey.fromString("item-uuid")
}