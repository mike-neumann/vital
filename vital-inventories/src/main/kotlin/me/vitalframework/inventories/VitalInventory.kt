package me.vitalframework.inventories

import me.vitalframework.SpigotPlayer
import me.vitalframework.VitalClassUtils.getRequiredAnnotation
import me.vitalframework.items.itemBuilder
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.*
import org.springframework.stereotype.Component

typealias InventoryItemClickAction = (InventoryClickEvent) -> Unit

/**
 * Represents a highly customizable inventory system that allows managing player inventory views,
 * actions, and updates for a variety of inventory types.
 *
 * This class provides mechanisms to handle player inventory interactions, including setting items
 * in specific slots, reacting to click events, managing transitions between inventories, and executing
 * custom actions during inventory events such as opening, updating, and closing.
 *
 * The `VitalInventory` is extended to create specific inventory systems with tailored behavior
 * by overriding its hooks and methods. Annotations can be used to define metadata and specify
 * default configurations for the inventory.
 */
open class VitalInventory {
    val type: Info.Type
    val name: String
    val background: Material
    private val _previousInventories = mutableMapOf<SpigotPlayer, VitalInventory>()
    private val _playerInventories = mutableMapOf<SpigotPlayer, InventoryView>()
    private val _items = mutableMapOf<Int, ItemStack>()
    private val _actions = mutableMapOf<Pair<SpigotPlayer, Int>, InventoryItemClickAction>()
    val previousInventories: Map<SpigotPlayer, VitalInventory> get() = _previousInventories
    val playerInventories: Map<SpigotPlayer, InventoryView> get() = _playerInventories
    val items: Map<Int, ItemStack> get() = _items
    val actions: Map<Pair<SpigotPlayer, Int>, InventoryItemClickAction> get() = _actions

    init {
        val info = getRequiredAnnotation<Info>()
        type = info.type
        name = info.name
        background = info.background
    }

    /**
     * Sets an item in the given inventory slot for a specific player and associates an optional action
     * to be triggered when the item is clicked.
     *
     * @param slot The inventory slot where the item should be placed.
     * @param itemStack The ItemStack to set in the specified slot.
     * @param player The SpigotPlayer for whom the item is being set.
     * @param action An optional action to execute when the item is clicked. Defaults to an empty action.
     */
    @JvmOverloads
    fun setItem(slot: Int, itemStack: ItemStack, player: SpigotPlayer, action: InventoryItemClickAction = {}) {
        _items[slot] = itemStack
        _actions[player to slot] = action
    }

    /**
     * Checks whether the specified player currently has an inventory open
     * that is managed by this instance.
     *
     * @param player The SpigotPlayer to check for an open inventory.
     * @return True if the player has an open inventory managed by this instance, false otherwise.
     */
    fun hasInventoryOpen(player: SpigotPlayer) = _playerInventories.containsKey(player)

    /**
     * Updates all player inventories managed by this instance and performs custom update logic.
     *
     * This method first invokes the `onUpdate` method to allow subclasses or implementations
     * to execute custom logic when an update is triggered. It then iterates through all players
     * associated with the current inventory, updating each player's inventory state individually
     * by invoking the overridden `update(player)` method.
     *
     * Typical use cases include refreshing inventory contents, updating UI elements, or applying
     * state changes across all player inventories.
     */
    fun update() {
        onUpdate()

        for ((player, _) in _playerInventories) {
            update(player)
        }
    }

    /**
     * Updates the current inventory for the specified player.
     *
     * This method refreshes the player's inventory by resetting the background
     * items, updating the items in specific slots, and invoking the `onUpdate`
     * hook to allow for custom behavior during the update process.
     *
     * @param player The SpigotPlayer whose inventory is being updated. If the player
     *               does not have an associated inventory, the method will return without
     *               performing any actions.
     */
    open fun update(player: SpigotPlayer) {
        if (player !in _playerInventories) return
        val inventory = _playerInventories[player]!!

        onUpdate(player)

        for (i in 0..<inventory.topInventory.size) {
            inventory.setItem(i, itemBuilder {
                type = background
            })
        }

        for ((i, item) in _items) {
            inventory.setItem(i, item)
        }
    }

    /**
     * Opens the current inventory for a specified player, optionally replacing a previous inventory.
     * This function manages the transition between inventories, updates the player's view,
     * and executes any logic needed upon opening the inventory.
     *
     * @param player The Spigot player instance for whom the inventory is being opened.
     * @param previousInventory The previously opened VitalInventory instance, if any. This inventory
     *                           will be closed before opening the current one.
     */
    @Suppress("UnstableApiUsage")
    open fun open(player: SpigotPlayer, previousInventory: VitalInventory? = null) {
        previousInventory?.close(player)
        val inventoryView = type.menuType.create(player, MiniMessage.miniMessage().deserialize(name))

        if (previousInventory != null) {
            _previousInventories[player] = previousInventory
        }
        _playerInventories[player] = inventoryView

        onOpen(player)
        update(player)
        player.openInventory(inventoryView)
    }

    /**
     * Handles a click event in the inventory by invoking the corresponding action for the specific
     * player and slot, if an action is defined.
     *
     * @param e The inventory click event containing information about the player who clicked,
     *          the slot clicked, and other relevant details.
     */
    fun click(e: InventoryClickEvent) = _actions[e.whoClicked to e.slot]?.invoke(e)

    /**
     * Closes the inventory for the specified player and handles associated cleanup actions.
     *
     * @param player The Spigot player instance for whom the inventory is being closed.
     */
    fun close(player: SpigotPlayer) {
        _playerInventories.remove(player)
        player.closeInventory()
        onClose(player)
    }

    /**
     * Called when an inventory is opened for a specific player. This method
     * can be overridden to provide custom behavior or logic upon opening the inventory.
     *
     * @param player The player for whom the inventory is being opened.
     */
    protected open fun onOpen(player: SpigotPlayer) {}

    /**
     * Called to perform updates related to the inventory or its state.
     *
     * This method is invoked by the `update` function within the containing class
     * and acts as a hook for executing custom logic whenever the inventory needs
     * to be refreshed or updated.
     *
     * Subclasses can override this method to implement custom behavior for the
     * inventory update process. The default implementation is empty.
     */
    protected open fun onUpdate() {}

    /**
     * Called when the inventory should be updated for a specific player.
     * This method can be overridden to define custom update logic for the inventory.
     *
     * @param player The player whose inventory is being updated.
     */
    protected open fun onUpdate(player: SpigotPlayer) {}

    /**
     * Handles the behavior that should occur when the inventory is closed for the specified player.
     *
     * @param player The player for whom the inventory is being closed. Represents the Spigot player instance
     *               associated with the inventory.
     */
    protected open fun onClose(player: SpigotPlayer) {}

    /**
     * Annotation used to define metadata for inventory types in the Vital framework.
     *
     * @property type Specifies the type of the inventory, corresponding to a predefined menu layout.
     * @property name Defines a unique identifier or name for the inventory.
     * @property background Specifies the default filler material for the inventory slots, defaulting to Material.AIR.
     */
    @Component
    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.CLASS)
    annotation class Info(
        val type: Type,
        val name: String,
        val background: Material = Material.AIR,
    ) {
        @Suppress("UnstableApiUsage")
        enum class Type(val menuType: MenuType.Typed<*, *>) {
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
            STONECUTTER(MenuType.STONECUTTER)
        }
    }
}