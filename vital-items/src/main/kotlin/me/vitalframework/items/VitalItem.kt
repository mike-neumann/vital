package me.vitalframework.items

import me.vitalframework.SpigotPlayer
import me.vitalframework.VitalClassUtils.getRequiredAnnotation
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.springframework.stereotype.Component
import java.util.*

/**
 * Represents a customizable item with unique metadata and behavior within the Vital framework.
 *
 * This class provides a base structure for creating items with cooldown functionality and custom
 * interactions. It includes handling for left-click, right-click, and cooldown-specific events.
 *
 * Classes inheriting from `VitalItem` must be annotated with `@Info` to define the item’s metadata,
 * such as its type, name, lore, amount, and other properties.
 *
 * The class also ensures that each item is uniquely identifiable through its persistent data container.
 */
open class VitalItem {
    val initialCooldown: Int
    val itemStack: ItemStack by lazy {
        itemBuilder {
            val info = this@VitalItem.getRequiredAnnotation<Info>()
            type = info.type
            name = info.name
            amount = info.amount
            lore = info.lore.toMutableList()
            itemFlags = info.itemFlags.toMutableList()
            unbreakable = info.unbreakable

            if (info.enchanted) {
                enchantments[Enchantment.FORTUNE] = 1
            }
        }
    }
    val playerCooldown = mutableMapOf<SpigotPlayer, Int>()

    init {
        val info = getRequiredAnnotation<Info>()
        this.initialCooldown = info.cooldown
    }

    /**
     * Handles a player's interaction event with an item and determines the appropriate response
     * based on the type of action performed. This method also manages the player's cooldown state
     * to prevent repetitive interactions within a specified cooldown period.
     *
     * @param e The [PlayerInteractEvent] representing the player's interaction event. It includes
     *          information such as the action type (e.g., left- or right-click) and the interacting player.
     */
    fun handleInteraction(e: PlayerInteractEvent) {
        if (!playerCooldown.containsKey(e.player)) playerCooldown[e.player] = 0
        if (playerCooldown[e.player]!! >= 1) return onCooldown(e)
        val action = e.action

        when (action) {
            Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK -> onLeftClick(e)
            else -> onRightClick(e)
        }

        playerCooldown[e.player] = initialCooldown
    }

    /**
     * Compares this `VitalItem` instance with another object to determine equality.
     * This method specifically checks if the provided object is an instance of `ItemStack`
     * and verifies their metadata for a unique identifier match.
     *
     * @param other The object to compare with this instance for equality.
     *              May or may not be an instance of `ItemStack`.
     * @return `true` if the `other` object is an `ItemStack` and matches the current item's unique identifier;
     *         `false` otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (other !is ItemStack) return false
        if (other.itemMeta == null) return other == this
        if (!other.itemMeta!!.persistentDataContainer.has(VitalNamespacedKey.ITEM_UUID, PersistentDataType.STRING))
            return toString() == other.toString().replace("${other.type} x ${other.amount}", "${other.type} x 1")
        val uuid = UUID.fromString(itemStack.itemMeta!!.persistentDataContainer[VitalNamespacedKey.ITEM_UUID, PersistentDataType.STRING])
        val otherId = UUID.fromString(other.itemMeta!!.persistentDataContainer[VitalNamespacedKey.ITEM_UUID, PersistentDataType.STRING])

        return uuid == otherId
    }

    override fun toString() = super.toString().replace("${itemStack.type} x ${itemStack.amount}", "${itemStack.type} x 1")

    /**
     * Handles the logic executed when the player performs a left-click while interacting
     * with an item. This method is called based on the player's interaction event.
     *
     * @param e The [PlayerInteractEvent] representing the player's interaction,
     *          including the action performed and related context.
     */
    open fun onLeftClick(e: PlayerInteractEvent) {}

    /**
     * Handles the player's interaction when they right-click.
     * This method is invoked in response to a right-click action performed by the player.
     *
     * @param e The [PlayerInteractEvent] triggered by the player's action.
     */
    open fun onRightClick(e: PlayerInteractEvent) {}

    /**
     * Handles logic when a player interacts with an item while it is on cooldown.
     *
     * This function is called when the player's interaction is blocked due to an active cooldown for the item.
     * It serves as a placeholder for custom behavior that can be implemented by extending this method in subclasses.
     *
     * @param e The [PlayerInteractEvent] associated with the player's interaction attempt.
     */
    open fun onCooldown(e: PlayerInteractEvent) {}

    /**
     * Defines behavior to be executed when the cooldown for a specific player expires.
     *
     * @param player The player for whom the cooldown has expired.
     */
    open fun onCooldownExpire(player: SpigotPlayer) {}

    /**
     * Invoked periodically during the cooldown period for a specified player. This method allows
     * custom behavior to be executed with each tick of the cooldown. The frequency of invocation
     * is determined by the scheduled cooldown handling logic.
     *
     * @param player The player currently undergoing the cooldown.
     */
    open fun onCooldownTick(player: SpigotPlayer) {}

    /**
     * Annotation used to define metadata and behavior for custom items in the Vital framework.
     * This metadata is used to configure item properties, such as name, lore, amount, type, flags,
     * cooldown, and special attributes.
     *
     * @property name The display name of the item.
     * @property lore The descriptive text associated with the item, used to provide additional
     *                information or context to the player.
     * @property amount Specifies the number of items the stack will contain by default.
     * @property type The material type of the item (e.g., COBBLESTONE, DIAMOND_SWORD).
     * @property itemFlags Flags that apply to the item, such as hiding certain attributes
     *                     (e.g., enchantments, unbreakable state, etc.).
     * @property cooldown Defines the cooldown duration in ticks between interactions with the item.
     * @property enchanted Indicates whether the item appears to be enchanted (visual enchantment effect).
     * @property unbreakable When true, the item cannot take durability damage.
     */
    @Component
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Info(
        val name: String,
        val lore: Array<String> = [],
        val amount: Int = 1,
        val type: Material,
        val itemFlags: Array<ItemFlag> = [],
        val cooldown: Int = 0,
        val enchanted: Boolean = false,
        val unbreakable: Boolean = true,
    )
}