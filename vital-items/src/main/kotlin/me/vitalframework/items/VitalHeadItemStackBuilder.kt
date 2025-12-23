package me.vitalframework.items

import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.util.UUID

/**
 * Convenience-class to build head [ItemStack] instances using the Builder-Pattern.
 * Check the function to view how to use it.
 */
class VitalHeadItemStackBuilder : VitalItemStackBuilder() {
    var owningPlayer: OfflinePlayer? = null

    companion object {
        /**
         * Convenience-function to create new head [ItemStack] instances using the Builder-Pattern.
         *
         * ```java
         * VitalHeadItemStackBuilder.headBuilder(null, it -> {
         *   it.set...();
         *   it.set...();
         *   it.set...();
         *   return kotlin.Unit.INSTANCE;
         * });
         * ```
         */
        @JvmStatic
        inline fun headBuilder(
            itemUuid: UUID? = null,
            init: VitalHeadItemStackBuilder.() -> Unit,
        ): ItemStack {
            val headItemStackBuilder = VitalHeadItemStackBuilder().apply { init() }

            return itemBuilder(itemUuid) {
                type = Material.PLAYER_HEAD
                name = headItemStackBuilder.name
                amount = headItemStackBuilder.amount
                unbreakable = headItemStackBuilder.unbreakable
                lore = headItemStackBuilder.lore
                itemFlags = headItemStackBuilder.itemFlags
                enchantments = headItemStackBuilder.enchantments
            }.apply {
                itemMeta = (itemMeta as SkullMeta).apply { owningPlayer = headItemStackBuilder.owningPlayer }
            }
        }
    }
}
