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

        for (i in 0..<inventory.topInventory.size) {
            inventory.setItem(i, itemBuilder {
                type = background
            })
        }

        for ((i, item) in _items) {
            inventory.setItem(i, item)
        }
    }

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

    fun click(e: InventoryClickEvent) = _actions[e.whoClicked to e.slot]?.invoke(e)

    fun close(player: SpigotPlayer) {
        _playerInventories.remove(player)
        player.closeInventory()
        onClose(player)
    }

    protected open fun onOpen(player: SpigotPlayer) {}
    protected open fun onUpdate() {}
    protected open fun onUpdate(player: SpigotPlayer) {}
    protected open fun onClose(player: SpigotPlayer) {}

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