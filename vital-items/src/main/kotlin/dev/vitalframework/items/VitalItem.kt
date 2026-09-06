package dev.vitalframework.items

import dev.vitalframework.SpigotPlayer
import dev.vitalframework.VitalCoreModule.Companion.getRequiredAnnotation
import dev.vitalframework.VitalCoreModule.Companion.logger
import dev.vitalframework.VitalHasInfo
import dev.vitalframework.VitalPlugin
import dev.vitalframework.localization.VitalLocalizationModule.Spigot.t
import org.bukkit.Material
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

    private val logger = logger()

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
    fun getItemStack(player: SpigotPlayer): ItemStack {
        // first set default values, then try to localize them
        val info = getInfo(Info::class.java)
        val builder =
            VitalItemStack
                .builder(uniqueId)
                .type(info.type)
                .name(info.name)
                .amount(info.amount)
                .lore(info.lore)
                .itemFlags(info.itemFlags)
                .unbreakable(info.unbreakable)
                .glint(if (info.glint) true else null)

        if (VitalPlugin.instance.isVitalModuleEnabled("vital-localization")) {
            builder.name(player.t(info.name))
            builder.lore(info.lore.flatMap { player.t(it).lines() }.toTypedArray())
            builder.afterInit {
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
            builder.afterInit {
                it.itemMeta =
                    it.itemMeta.apply {
                        persistentDataContainer[VitalNamespacedKey.ITEM_LOCALIZED, PersistentDataType.BOOLEAN] =
                            false
                    }
            }
        }

        return builder.build()
    }

    /**
     * Internal function to handle the given [PlayerInteractEvent] for this item.
     * This function handles all lifecycle functions of this item and the cooldown logic.
     */
    fun handleInteraction(e: PlayerInteractEvent) {
        val loggingContext = "Context: Vital item '$this', e '$e', player '${e.player}'."
        logger.debug("Handling interaction for Vital item. $loggingContext")

        if (!playerCooldown.containsKey(e.player.uniqueId)) {
            logger.debug("Player does not yet have a cooldown for this Vital item, setting to '0'. $loggingContext")
            playerCooldown[e.player.uniqueId] = 0
        }

        val playerCooldown = playerCooldown[e.player.uniqueId]!!
        if (playerCooldown >= 1) {
            logger.debug(
                "Current cooldown of '$playerCooldown' exceeds '0', calling 'onCooldown(PlayerInteractEvent)' lifecycle. $loggingContext",
            )
            return onCooldown(e)
        }

        when (e.action) {
            Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK -> {
                logger.debug("Item was left-clicked, calling 'onLeftClick(PlayerInteractEvent)' lifecycle. $loggingContext")
                onLeftClick(e)
            }
            else -> {
                logger.debug("Item was right-clicked, calling 'onRightClick(PlayerInteractEvent)' lifecycle. $loggingContext")
                onRightClick(e)
            }
        }

        val info = getInfo(Info::class.java)
        logger.debug("Setting cooldown back to '${info.cooldown}'. $loggingContext")
        this.playerCooldown[e.player.uniqueId] = info.cooldown
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
    open fun onLeftClick(e: PlayerInteractEvent) {
        logger.debug("Lifecycle function 'onLeftClick(PlayerInteractEvent)' was not overridden for Vital item '$this'.")
    }

    /**
     * Lifecycle function; called when this item is left-clicked by a player and the player's cooldown is not active.
     */
    open fun onRightClick(e: PlayerInteractEvent) {
        logger.debug("Lifecycle function 'onRightClick(PlayerInteractEvent)' was not overridden for Vital item '$this'.")
    }

    /**
     * Lifecycle function; called when this item is left- or right-clicked by a player and the player's cooldown is currently active.
     */
    open fun onCooldown(e: PlayerInteractEvent) {
        logger.debug("Lifecycle function 'onCooldown(PlayerInteractEvent)' was not overridden for Vital item '$this'.")
    }

    /**
     * Lifecycle function; called when this item's cooldown expires for a player.
     * This function is also called when a player doesn't have this item equipped.
     */
    open fun onCooldownExpire(player: SpigotPlayer) {
        logger.debug("Lifecycle function 'onCooldownExpire(Player)' was not overridden for Vital item '$this'.")
    }

    /**
     * Lifecycle function; called when the cooldown task ticks the cooldown for this item and a given player.
     * This function is also called when a player doesn't have this item equipped.
     */
    open fun onCooldownTick(player: SpigotPlayer) {
        logger.debug("Lifecycle function 'onCooldownTick(Player)' was not overridden for Vital item '$this'.")
    }

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
        val glint: Boolean = false,
        val unbreakable: Boolean = true,
    )
}
