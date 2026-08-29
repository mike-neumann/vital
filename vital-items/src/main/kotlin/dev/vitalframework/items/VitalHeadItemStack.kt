package dev.vitalframework.items

import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.util.UUID

/**
 * Convenience-class to build head [ItemStack] instances using the Builder-Pattern.
 * Check the function to view how to use it.
 */
class VitalHeadItemStack(
    itemUuid: UUID? = null,
) : VitalItemStack(itemUuid) {
    var owningPlayer: OfflinePlayer? = null

    override fun build(): ItemStack {
        val itemStack =
            VitalItemStack
                .builder()
                .type(Material.PLAYER_HEAD)
                .build()
        val itemMeta = itemStack.itemMeta as SkullMeta
        itemMeta.owningPlayer = owningPlayer
        itemStack.itemMeta = itemMeta

        return itemStack
    }

    companion object {
        @JvmOverloads
        @JvmStatic
        fun builder(itemUuid: UUID? = null): VitalHeadItemStack = VitalHeadItemStack(itemUuid)
    }
}
