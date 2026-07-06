package me.vitalframework.inventories

import me.vitalframework.SpigotPlayer
import me.vitalframework.Vital
import me.vitalframework.VitalCoreModule.Companion.getRequiredAnnotation
import me.vitalframework.VitalHasInfo
import me.vitalframework.items.VitalItemStackBuilder.Companion.itemBuilder
import me.vitalframework.localization.VitalLocalizationModule.Spigot.t
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.MenuType
import org.springframework.stereotype.Component
import java.util.UUID

typealias InventoryItemClickAction = (InventoryClickEvent) -> Unit

/**
 * Defines an inventory menu within the Vital-Framework.
 * An inventory menu is a GUI used to display information to a player, and process a player's inputs.
 *
 * By default, the class will be a bean.
 *
 * ```java
 * @VitalInventory.Info(
 *   name = "MyInventory",
 *   type = VitalInventory.Type.GENERIC_9X1,
 *   background = Material.COBBLESTONE
 * )
 * public class MyInventory extends VitalInventory {
 *   @Override
 *   public void onUpdate() {
 *     // This function is called when someone or something triggers an inventory update.
 *     // It should not be used to set player-specific items, only global ones.
 *   }
 *
 *   @Override
 *   public void onUpdate(Player player) {
 *     // This function is called when someone or something triggers an inventory update with a player.
 *     // It should be used to set player-specific items, not global ones.
 *   }
 *
 *   // ...
 * }
 * ```
 */
open class VitalInventory : VitalHasInfo {
    override val info = mutableMapOf<Class<out Annotation>, Annotation>(Info::class.java to javaClass.getRequiredAnnotation<Info>())

    val items = mutableMapOf<UUID, MutableMap<Int, ItemStack?>>()
    val previousInventories = mutableMapOf<UUID, VitalInventory>()
    val playerInventories = mutableMapOf<UUID, InventoryView>()
    val actions = mutableMapOf<UUID, MutableMap<Int, InventoryItemClickAction>>()

    val background =
        itemBuilder {
            val info = getInfo(Info::class.java)
            type = info.background
        }

    /**
     * Gets all items for the given [playerUniqueId].
     * This function will never return `null`, even if the given [playerUniqueId] doesn't have this inventory open.
     */
    fun getItems(playerUniqueId: UUID) = items[playerUniqueId] ?: mutableMapOf()

    /**
     * Gets all mapped actions for the given [playerUniqueId].
     * This function will never return `null`, even if the given [playerUniqueId] doesn't have this inventory open.
     */
    fun getActions(playerUniqueId: UUID) = actions[playerUniqueId] ?: mutableMapOf()

    /**
     * Gets the actual [InventoryView] for the given [playerUniqueId].
     * This function will return `null` if the given [playerUniqueId] doesn't have this inventory open.
     */
    fun getInventory(playerUniqueId: UUID) = playerInventories[playerUniqueId]

    /**
     * Checks if the given [SpigotPlayer] currently has this inventory open.
     */
    fun hasInventoryOpen(player: SpigotPlayer) = playerInventories.containsKey(player.uniqueId)

    /**
     * Sets the given [item] in the [slot] for the inventory of the given [player].
     * Once the player opens this inventory, the actual [InventoryView] will be populated with all items set via this function.
     * Additionally, an item may have an [action] attached to it.
     */
    @JvmOverloads
    fun setItem(
        player: SpigotPlayer,
        slot: Int,
        item: ItemStack?,
        action: InventoryItemClickAction = {},
    ) {
        val items = getItems(player.uniqueId).toMutableMap()
        items[slot] = item
        this.items[player.uniqueId] = items

        val actions = getActions(player.uniqueId)
        actions[slot] = action
        this.actions[player.uniqueId] = actions
    }

    /**
     * Updates this inventory for all players currently viewing it.
     * Calling this function will call the [onUpdate] lifecycle function.
     *
     * For each player currently viewing this inventory,
     * the [onUpdate] player lifecycle function will be called.
     */
    fun update() {
        onUpdate()

        for ((uniqueId, _) in playerInventories) {
            val player = Bukkit.getPlayer(uniqueId) ?: continue
            update(player)
        }
    }

    /**
     * Updates this inventory for the given [player].
     * All previously set items via [setItem] will be populated in the inventory for the given [player].
     * If the player doesn't have this inventory open, this function does nothing.
     */
    open fun update(player: SpigotPlayer) {
        val inventory = getInventory(player.uniqueId) ?: return

        onUpdate(player)

        for (i in 0..<inventory.topInventory.size) {
            inventory.setItem(i, background)
        }

        val items = getItems(player.uniqueId)
        for ((i, item) in items) {
            inventory.setItem(i, item)
        }
    }

    /**
     * Opens this inventory for the given [player] and updates it via [update].
     * If a [previousInventory] was given, clicking outside of this inventory view will open the previous inventory.
     */
    @Suppress("UnstableApiUsage")
    open fun open(
        player: SpigotPlayer,
        previousInventory: VitalInventory? = null,
    ) {
        val info = getInfo(Info::class.java)

        previousInventory?.close(player)
        player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW)
        val inventoryView =
            info.type.menuType.create(
                player,
                MiniMessage.miniMessage().deserialize(
                    if (Vital.isVitalModuleEnabled("vital-localization")) player.t(info.name) else info.name,
                ),
            )

        if (previousInventory != null) {
            previousInventories[player.uniqueId] = previousInventory
        }
        playerInventories[player.uniqueId] = inventoryView

        onOpen(player)
        update(player)
        player.openInventory(inventoryView)
    }

    /**
     * Internal function to handle a [InventoryClickEvent] for this inventory.
     * If the clicked item has an action attached to it via [setItem], the action will be triggered.
     */
    open fun click(e: InventoryClickEvent) {
        actions[e.whoClicked.uniqueId]?.get(e.slot)?.invoke(e)
        onClick(e)
    }

    /**
     * Closes this inventory for the given [player].
     * This function will clean up the state for this inventory
     * and eventually call the [onClose] lifecycle function for the given [player].
     */
    open fun close(player: SpigotPlayer) {
        // the inventory MUST be closed first, to include previous inventory functionality
        playerInventories.remove(player.uniqueId)
        player.closeInventory()

        // then we can clean up this inventory for the closing player, so we don't leak memory'
        previousInventories.remove(player.uniqueId)
        actions.remove(player.uniqueId)
        items.remove(player.uniqueId)

        // finally, call the onClose hook to allow for custom behavior during the close process
        onClose(player)
    }

    /**
     * Lifecycle function; called when this inventory is opened for the given [player].
     */
    protected open fun onOpen(player: SpigotPlayer) {}

    /**
     * Lifecycle function; called when this inventory is updated for all players via [update].
     * Override this function to set items that are always the same for each player (no language specifics, same item, etc. )
     */
    protected open fun onUpdate() {}

    /**
     * Lifecycle function; called when this inventory is updated for the given [player].
     * Override this function to set items that are unique for each player (language specific, different item, etc. ).
     */
    protected open fun onUpdate(player: SpigotPlayer) {}

    /**
     * Lifecycle function; called when an item in this inventory is clicked and the [InventoryClickEvent] is handled via [click].
     * This function will also get called when an empty slot is clicked.
     */
    protected open fun onClick(e: InventoryClickEvent) {}

    /**
     * Lifecycle function; called when this inventory is closed for the given [player].
     */
    protected open fun onClose(player: SpigotPlayer) {}

    /**
     * Defines the info for a [VitalInventory].
     */
    @Component
    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.CLASS)
    annotation class Info(
        val type: Type,
        val name: String,
        val background: Material = Material.AIR,
    )

    /**
     * Defines the supported [MenuType]s a [VitalInventory] can use.
     */
    @Suppress("UnstableApiUsage")
    enum class Type(
        val menuType: MenuType.Typed<*, *>,
    ) {
        GENERIC_9X1(MenuType.GENERIC_9X1),
        GENERIC_9X2(MenuType.GENERIC_9X2),
        GENERIC_9X3(MenuType.GENERIC_9X3),
        GENERIC_9X4(MenuType.GENERIC_9X4),
        GENERIC_9X5(MenuType.GENERIC_9X5),
        GENERIC_9X6(MenuType.GENERIC_9X6),
        GENERIC_3X3(MenuType.GENERIC_3X3),
        ANVIL(MenuType.ANVIL),
        BEACON(MenuType.BEACON),
        BLAST_FURNACE(MenuType.BLAST_FURNACE),
        BREWING_STAND(MenuType.BREWING_STAND),
        CRAFTING(MenuType.CRAFTING),
        ENCHANTMENT(MenuType.ENCHANTMENT),
        FURNACE(MenuType.FURNACE),
        GRINDSTONE(MenuType.GRINDSTONE),
        HOPPER(MenuType.HOPPER),
        LECTERN(MenuType.LECTERN),
        LOOM(MenuType.LOOM),
        MERCHANT(MenuType.MERCHANT),
        SHULKER_BOX(MenuType.SHULKER_BOX),
        SMITHING(MenuType.SMITHING),
        SMOKER(MenuType.SMOKER),
        CARTOGRAPHY_TABLE(MenuType.CARTOGRAPHY_TABLE),
        STONECUTTER(MenuType.STONECUTTER),
    }
}
