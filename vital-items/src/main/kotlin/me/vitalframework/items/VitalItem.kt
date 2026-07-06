package me.vitalframework.items

import me.vitalframework.SpigotPlayer
import me.vitalframework.Vital
import me.vitalframework.VitalCoreModule.Companion.getRequiredAnnotation
import me.vitalframework.VitalHasInfo
import me.vitalframework.items.VitalItemStackBuilder.Companion.itemBuilder
import me.vitalframework.localization.VitalLocalizationModule.Spigot.t
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Defines an interactable item within the Vital-Framework.
 *
 * ```java
 * @VitalItem.Info(
 *   name = "MyItem",
 *   type = Material.STONE
 * )
 * public class MyItem extends VitalItem {
 *   @Override
 *   public void onLeftClick(PlayerInteractEvent e) {
 *     // ...
 *   }
 *
 *   @Override
 *   public void onRightClick(PlayerInteractEvent e) {
 *     // ...
 *   }
 *
 *   @Override
 *   public void onCooldown(PlayerInteractEvent e) {
 *     // ...
 *   }
 *
 *   @Override
 *   public void onCooldownExpire(Player player) {
 *     // ...
 *   }
 *
 *   @Override
 *   public void onCooldownTick(Player player) {
 *     // ...
 *   }
 * }
 * ```
 */
open class VitalItem : VitalHasInfo {
    override val info = mutableMapOf(Info::class.java to javaClass.getRequiredAnnotation<Info>())

    /**
     * This id is required to identify a single [VitalItem] during event processing and delegation.
     */
    val uniqueId: UUID = UUID.randomUUID()

    /**
     * The cooldowns for each player of this item.
     * Once the cooldown for a player expires, the player will be removed from this [Map].
     */
    val playerCooldown = mutableMapOf<UUID, Int>()

    /**
     * Gets the [ItemStack] for the given [player].
     */
    fun getItemStack(player: SpigotPlayer) =
        itemBuilder(uniqueId) {
            // first set default values, then try to localize them
            val info = getInfo(Info::class.java)
            type = info.type
            name = info.name
            amount = info.amount
            lore = info.lore
            itemFlags = info.itemFlags
            unbreakable = info.unbreakable

            if (info.enchanted) {
                enchantments[Enchantment.FORTUNE] = 1
            }

            if (Vital.isVitalModuleEnabled("vital-localization")) {
                name = player.t(info.name)
                lore = info.lore.flatMap { player.t(it).lines() }.toTypedArray()
                afterInit = {
                    it.itemMeta =
                        it.itemMeta.apply {
                            persistentDataContainer[VitalNamespacedKey.ITEM_LOCALIZED, PersistentDataType.BOOLEAN] =
                                true
                            persistentDataContainer[VitalNamespacedKey.ITEM_LOCALIZATION_KEY, PersistentDataType.STRING] =
                                info.name
                            persistentDataContainer[VitalNamespacedKey.ITEM_LORE_LOCALIZATION_KEYS, PersistentDataType.LIST.strings()] =
                                info.lore.toList()
                        }
                }
            } else {
                afterInit = {
                    it.itemMeta =
                        it.itemMeta.apply {
                            persistentDataContainer[VitalNamespacedKey.ITEM_LOCALIZED, PersistentDataType.BOOLEAN] =
                                false
                        }
                }
            }
        }

    /**
     * Internal function to handle the given [PlayerInteractEvent] for this item.
     * This function handles all lifecycle functions of this item and the cooldown logic.
     */
    fun handleInteraction(e: PlayerInteractEvent) {
        if (!playerCooldown.containsKey(e.player.uniqueId)) {
            playerCooldown[e.player.uniqueId] = 0
        }

        if (playerCooldown[e.player.uniqueId]!! >= 1) {
            return onCooldown(e)
        }

        when (e.action) {
            Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK -> onLeftClick(e)
            else -> onRightClick(e)
        }

        val info = getInfo(Info::class.java)
        playerCooldown[e.player.uniqueId] = info.cooldown
    }

    // TODO
    // this does not work with items if the server restarts
    // the uniqueId field is volatile and will be regenerated
    override fun equals(other: Any?): Boolean {
        if (other !is ItemStack && other !is VitalItem) {
            return false
        }

        if (other is ItemStack) {
            if (other.itemMeta == null) {
                return false
            }

            val itemUuid = other.itemMeta.persistentDataContainer[VitalNamespacedKey.ITEM_UUID, PersistentDataType.STRING]
            return uniqueId == itemUuid?.let { UUID.fromString(it) }
        }

        return uniqueId == (other as VitalItem).uniqueId
    }

    /**
     * Lifecycle function; called when this item is right-clicked by a player and the player's cooldown is not active.
     */
    open fun onLeftClick(e: PlayerInteractEvent) {}

    /**
     * Lifecycle function; called when this item is left-clicked by a player and the player's cooldown is not active.
     */
    open fun onRightClick(e: PlayerInteractEvent) {}

    /**
     * Lifecycle function; called when this item is left- or right-clicked by a player and the player's cooldown is currently active.
     */
    open fun onCooldown(e: PlayerInteractEvent) {}

    /**
     * Lifecycle function; called when this item's cooldown expires for a player.
     * This function is also called when a player doesn't have this item equipped.
     */
    open fun onCooldownExpire(player: SpigotPlayer) {}

    /**
     * Lifecycle function; called when the cooldown task ticks the cooldown for this item and a given player.
     * This function is also called when a player doesn't have this item equipped.
     */
    open fun onCooldownTick(player: SpigotPlayer) {}

    override fun toString(): String = "VitalItem(uniqueId=$uniqueId, info=$info, playerCooldown=$playerCooldown)"

    /**
     * Defines the info for a [VitalItem].
     */
    @Component
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Info(
        val type: Material,
        val name: String,
        val lore: Array<String> = [],
        val cooldown: Int = 0,
        val itemFlags: Array<ItemFlag> = [],
        val amount: Int = 1,
        val enchanted: Boolean = false,
        val unbreakable: Boolean = true,
    )
}
