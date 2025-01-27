package me.vitalframework.inventories

import me.vitalframework.RequiresAnnotation
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.Range
import org.springframework.stereotype.Component

open class VitalInventory(previousInventory: VitalInventory?) : RequiresAnnotation<VitalInventory.Info> {
    private val size: Int
    private val name: String
    private val playerInventories = mutableMapOf<Player, Inventory>()
    private val items = mutableMapOf<Int, ItemStack>()
    private val actions = mutableMapOf<Pair<Player, Int>, (InventoryClickEvent) -> Unit>()
    private val previousInventory: VitalInventory?

    init {
        val info = getRequiredAnnotation()

        size = info.size
        name = info.name
        this.previousInventory = previousInventory
    }

    override fun requiredAnnotationType() = Info::class.java

    @JvmOverloads
    fun setItem(slot: Int, itemStack: ItemStack, player: Player, action: (InventoryClickEvent) -> Unit = {}) {
        items.put(slot, itemStack)
        actions.put(player to slot, action)
    }

    fun hasInventoryOpen(player: Player) = playerInventories.containsKey(player)

    fun update() {
        onUpdate()

        playerInventories.forEach { (player, inventory) ->
            update(player)
        }
    }

    open fun update(player: Player) {
        val inventory: Inventory = playerInventories[player]!!

        onUpdate(player)

        items.forEach { (i, itemStack) ->
            inventory.setItem(i, itemStack)
        }
    }

    open fun open(player: Player) {
        val inventory = Bukkit.createInventory(player, size, name)

        playerInventories.put(player, inventory)

        onOpen(player)
        update(player)
        player.openInventory(inventory)
    }

    fun click(e: InventoryClickEvent) {
        val action = actions[e.whoClicked to e.slot]

        action?.invoke(e)
    }

    fun close(player: Player) {
        playerInventories.remove(player)
        onClose(player)
    }

    /**
     * used for when this inventory is opened for any player
     */
    fun onOpen(player: Player) {
    }

    /**
     * used for when setting static items (non player information)
     */
    fun onUpdate() {
    }

    /**
     * used for when needing to set items that hold player specific information
     */
    fun onUpdate(player: Player) {
    }

    /**
     * used for when this inventory is closed for an opened player
     */
    fun onClose(player: Player) {
    }

    /**
     * Annotation used to provide information about an inventory.
     */
    @Component
    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.CLASS)
    annotation class Info(
        /**
         * The title of this inventory menu.
         */
        val name: String,
        /**
         * The size in slots of this inventory menu. Default is 9 (one row).
         */
        val size: @Range(from = 9, to = 54) Int = 9,
        /**
         * The material used as the background of this inventory menu. Default is AIR.
         *
         * @return The background material.
         */
        val background: Material = Material.AIR,
    )
}