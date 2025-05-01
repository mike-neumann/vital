package me.vitalframework.inventories

import me.vitalframework.SpigotPlayer
import me.vitalframework.VitalClassUtils.getRequiredAnnotation
import me.vitalframework.items.itemBuilder
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.Range
import org.springframework.stereotype.Component

typealias InventoryItemClickAction = (InventoryClickEvent) -> Unit

open class VitalInventory {
    val size: Int
    val name: String
    val background: Material
    private val _previousInventories = mutableMapOf<SpigotPlayer, VitalInventory>()
    private val _playerInventories = mutableMapOf<SpigotPlayer, Inventory>()
    private val _items = mutableMapOf<Int, ItemStack>()
    private val _actions = mutableMapOf<Pair<SpigotPlayer, Int>, InventoryItemClickAction>()
    val previousInventories: Map<SpigotPlayer, VitalInventory> get() = _previousInventories
    val playerInventories: Map<SpigotPlayer, Inventory> get() = _playerInventories
    val items: Map<Int, ItemStack> get() = _items
    val actions: Map<Pair<SpigotPlayer, Int>, InventoryItemClickAction> get() = _actions

    init {
        val info = getRequiredAnnotation<Info>()
        size = info.size
        name = info.name
        background = info.background
    }

    @JvmOverloads
    fun setItem(slot: Int, itemStack: ItemStack, player: SpigotPlayer, action: InventoryItemClickAction = {}) {
        _items[slot] = itemStack
        _actions[player to slot] = action
    }

    fun hasInventoryOpen(player: SpigotPlayer) = _playerInventories.containsKey(player)

    fun update() {
        onUpdate()

        for ((player, _) in _playerInventories) {
            update(player)
        }
    }

    open fun update(player: SpigotPlayer) {
        if (player !in _playerInventories) return
        val inventory = _playerInventories[player]!!

        onUpdate(player)

        for (i in 0..<size) {
            inventory.setItem(i, itemBuilder {
                type = background
            })
        }

        for ((i, item) in _items) {
            inventory.setItem(i, item)
        }
    }

    open fun open(player: SpigotPlayer, previousInventory: VitalInventory? = null) {
        previousInventory?.close(player)
        val inventory = Bukkit.createInventory(player, size, MiniMessage.miniMessage().deserialize(name))

        if (previousInventory != null) {
            _previousInventories[player] = previousInventory
        }
        _playerInventories[player] = inventory

        onOpen(player)
        update(player)
        player.openInventory(inventory)
    }

    fun click(e: InventoryClickEvent) = _actions[e.whoClicked to e.slot]?.invoke(e)

    fun close(player: SpigotPlayer) {
        _playerInventories.remove(player)
        player.closeInventory()
    }

    protected open fun onOpen(player: SpigotPlayer) {}
    protected open fun onUpdate() {}
    protected open fun onUpdate(player: SpigotPlayer) {}
    protected open fun onClose(player: SpigotPlayer) {}

    @Component
    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.CLASS)
    annotation class Info(val name: String, val size: @Range(from = 9, to = 54) Int = 9, val background: Material = Material.AIR)
}