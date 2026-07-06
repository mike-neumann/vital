package me.vitalframework.items

import org.bukkit.NamespacedKey

/**
 * Internal class; stores internal [NamespacedKey] instances for [VitalItem] and localization via `vital-localization`.
 */
object VitalNamespacedKey {
    @JvmStatic
    val ITEM_UUID = NamespacedKey("vital", "item-uuid")

    @JvmStatic
    val ITEM_LOCALIZED = NamespacedKey("vital", "item-localized")

    @JvmStatic
    val ITEM_LOCALIZATION_KEY = NamespacedKey("vital", "item-localization-key")

    @JvmStatic
    val ITEM_LORE_LOCALIZATION_KEYS = NamespacedKey("vital", "item-lore-localization-keys")
}
