package me.vitalframework.inventories

import me.vitalframework.SpigotPlayer
import me.vitalframework.VitalClassUtils.getRequiredAnnotation
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.Range
import org.springframework.stereotype.Component

typealias InventoryItemClickAction = (InventoryClickEvent) -> Unit

open class VitalInventory(val previousInventory: VitalInventory?) {
    val size: Int
    val name: String
    private val _playerInventories = mutableMapOf<SpigotPlayer, Inventory>()
    private val _items = mutableMapOf<Int, ItemStack>()
    private val _actions = mutableMapOf<Pair<SpigotPlayer, Int>, InventoryItemClickAction>()
    val playerInventories: Map<SpigotPlayer, Inventory> get() = _playerInventories
    val items: Map<Int, ItemStack> get() = _items
    val actions: Map<Pair<SpigotPlayer, Int>, InventoryItemClickAction> get() = _actions

    init {
        val info = getRequiredAnnotation<Info>()
        size = info.size
        name = LegacyComponentSerializer.legacySection().serialize(MiniMessage.miniMessage().deserialize(info.name))
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

        for ((i, item) in _items) {
            inventory.setItem(i, item)
        }
    }

    open fun open(player: SpigotPlayer) {
        val inventory = Bukkit.createInventory(player, size, name)

        _playerInventories[player] = inventory

        onOpen(player)
        update(player)
        player.openInventory(inventory)
    }

    fun click(e: InventoryClickEvent) = _actions[e.whoClicked to e.slot]?.invoke(e)

    fun close(player: SpigotPlayer) {
        _playerInventories.remove(player)
        onClose(player)
    }

    open fun onOpen(player: SpigotPlayer) {}
    open fun onUpdate() {}
    open fun onUpdate(player: SpigotPlayer) {}
    open fun onClose(player: SpigotPlayer) {}

    @Component
    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.CLASS)
    annotation class Info(val name: String, val size: @Range(from = 9, to = 54) Int = 9, val background: Material = Material.AIR)
}