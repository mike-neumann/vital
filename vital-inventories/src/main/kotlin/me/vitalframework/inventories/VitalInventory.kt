package me.vitalframework.inventories

import me.vitalframework.SpigotPlayer
import me.vitalframework.Vital
import me.vitalframework.VitalCoreSubModule.Companion.getRequiredAnnotation
import me.vitalframework.items.VitalItemStackBuilder.Companion.itemBuilder
import me.vitalframework.localization.VitalLocalizationSubModule.Spigot.getTranslatedText
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.MenuType
import org.springframework.stereotype.Component
import java.util.UUID
import kotlin.reflect.KClass

typealias InventoryItemClickAction = (InventoryClickEvent) -> Unit

/**
 * Defines an inventory menu within the Vital-Framework.
 * An inventory menu is a GUI used to display information to a player, and process a player's inputs.
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
open class VitalInventory {
    private val _items = mutableMapOf<Int, ItemStack>()
    private val _previousInventories = mutableMapOf<UUID, VitalInventory>()
    private val _playerInventories = mutableMapOf<UUID, InventoryView>()
    private val _actions = mutableMapOf<UUID, MutableMap<Int, InventoryItemClickAction>>()

    /**
     * The type of inventory to be used defined by [Info.type].
     */
    val type
        get() = getInfo().type

    /**
     * The name of this inventory defined by [Info.name].
     */
    val name
        get() = getInfo().name

    /**
     * The background this inventory should set when it's opened defined by [Info.background].
     */
    val background
        get() = getInfo().background

    /**
     * All [SpigotPlayer]s as a [UUID] and their mapping to a specific [VitalInventory] instance as their previous inventory.
     * When a player closes his current inventory and requests to view his previous one, this map is used to fetch it.
     */
    val previousInventories: Map<UUID, VitalInventory>
        get() = _previousInventories

    /**
     * All [SpigotPlayer]s as a [UUID] and their current [InventoryView] instance for this inventory.
     * This map will only contain the players that have this inventory open at the time.
     */
    val playerInventories: Map<UUID, InventoryView>
        get() = _playerInventories

    /**
     * All [ItemStack]s and their set slot for this inventory.
     * If the [setItem] function was never called during inventory setup in [onUpdate], this map will be empty.
     */
    val items: Map<Int, ItemStack>
        get() = _items

    /**
     * All [SpigotPlayer]s and their mapping to an [InventoryItemClickAction] mapped to a specific slot in the inventory.
     */
    val actions: Map<UUID, Map<Int, InventoryItemClickAction>>
        get() = _actions

    /**
     * Sets an [ItemStack] in the current inventory.
     * May have an [InventoryItemClickAction] attached to it, to react to user-input.
     */
    @JvmOverloads
    fun setItem(
        slot: Int,
        itemStack: ItemStack,
        player: SpigotPlayer,
        action: InventoryItemClickAction = {},
    ) {
        _items[slot] = itemStack
        _actions[player.uniqueId]!![slot] = action
    }

    /**
     * Checks if the given [SpigotPlayer] currently has this inventory open.
     */
    fun hasInventoryOpen(player: SpigotPlayer) = _playerInventories.containsKey(player.uniqueId)

    /**
     * Updates all inventories for all [SpigotPlayer]s currently viewing this inventory ([_playerInventories]).
     */
    fun update() {
        onUpdate()

        for ((uniqueId, _) in _playerInventories) {
            val player = Bukkit.getPlayer(uniqueId) ?: continue
            update(player)
        }
    }

    /**
     * Updates this inventory for the specified [SpigotPlayer].
     */
    open fun update(player: SpigotPlayer) {
        if (player.uniqueId !in _playerInventories) return
        val inventory = _playerInventories[player.uniqueId]!!

        onUpdate(player)

        for (i in 0..<inventory.topInventory.size) {
            inventory.setItem(
                i,
                itemBuilder {
                    type = background
                },
            )
        }

        for ((i, item) in _items) {
            inventory.setItem(i, item)
        }
    }

    /**
     * Opens this inventory for the specified [SpigotPlayer].
     * May have a previous inventory attached to it, to make reverse traversal possible.
     */
    @Suppress("UnstableApiUsage")
    open fun open(
        player: SpigotPlayer,
        previousInventory: VitalInventory? = null,
    ) {
        previousInventory?.close(player)
        val inventoryView =
            type.menuType.create(
                player,
                MiniMessage.miniMessage().deserialize(
                    if ("vital-localization" in Vital.vitalSubModules) player.getTranslatedText(name) else name,
                ),
            )

        if (previousInventory != null) {
            _previousInventories[player.uniqueId] = previousInventory
        }
        _playerInventories[player.uniqueId] = inventoryView
        _actions[player.uniqueId] = mutableMapOf()

        onOpen(player)
        update(player)
        player.openInventory(inventoryView)
    }

    /**
     * Internal function to handle a click event for this inventory.
     */
    open fun click(e: InventoryClickEvent) {
        _actions[e.whoClicked.uniqueId]?.get(e.slot)?.invoke(e)
        onClick(e)
    }

    /**
     * Closes this inventory for the specified [SpigotPlayer].
     */
    open fun close(player: SpigotPlayer) {
        // the inventory MUST be closed first, to include previous inventory functionality
        _playerInventories.remove(player.uniqueId)
        player.closeInventory()

        // then we can clean up this inventory for the closing player, so we don't leak memory'
        _previousInventories.remove(player.uniqueId)
        _actions.remove(player.uniqueId)
        _items.clear()

        // finally, call the onClose hook to allow for custom behavior during the close process
        onClose(player)
    }

    /**
     * Called when this inventory is opened for a specific [SpigotPlayer].
     */
    protected open fun onOpen(player: SpigotPlayer) {}

    /**
     * Called when this inventory is to be updated.
     * This function should only be used to set items, that should be global for all players.
     */
    protected open fun onUpdate() {}

    /**
     * Called when this inventory is to be updated for a specific player.
     * This function should only be used to set items, that are tied to a specific player.
     */
    protected open fun onUpdate(player: SpigotPlayer) {}

    /**
     * Called when an item or empty slot is clicked within the inventory.
     */
    protected open fun onClick(e: InventoryClickEvent) {}

    /**
     * Called when this inventory is closed for a specific player.
     */
    protected open fun onClose(player: SpigotPlayer) {}

    companion object {
        /**
         * Retrieves the VitalInventory.Info annotation associated with this class.
         */
        @JvmStatic
        fun Class<out VitalInventory>.getInfo(): Info = getRequiredAnnotation<Info>()

        /**
         * Retrieves the VitalInventory.Info annotation associated with this class.
         */
        @JvmStatic
        fun KClass<out VitalInventory>.getInfo(): Info = java.getInfo()

        /**
         * Retrieves the VitalInventory.Info annotation associated with this instance.
         */
        @JvmStatic
        fun VitalInventory.getInfo(): Info = javaClass.getInfo()
    }

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
