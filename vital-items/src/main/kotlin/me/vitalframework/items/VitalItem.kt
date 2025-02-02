package me.vitalframework.items

import me.vitalframework.RequiresAnnotation
import me.vitalframework.SpigotPlayer
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.springframework.stereotype.Component
import java.util.*

open class VitalItem : ItemStack(), RequiresAnnotation<VitalItem.Info> {
    val initialCooldown: Int
    val playerCooldown = mutableMapOf<SpigotPlayer, Int>()

    init {
        val info = getRequiredAnnotation();
        val itemStack = itemBuilder {
            type = info.type
            name = info.name
            amount = info.amount
            lore = info.lore.toMutableList()
            itemFlags = info.itemFlags.toMutableList()
            unbreakable = info.unbreakable
        }
        val meta = itemStack.itemMeta!!

        if (info.enchanted) {
            meta.addEnchant(Enchantment.LUCK, 1, true)
        }

        itemMeta = meta
        type = itemStack.type
        amount = itemStack.amount
        this.initialCooldown = info.cooldown
    }

    override fun requiredAnnotationType() = Info::class.java

    fun handleInteraction(e: PlayerInteractEvent) {
        if (!playerCooldown.containsKey(e.player)) {
            playerCooldown[e.player] = 0
        }

        if (playerCooldown[e.player]!! >= 1) {
            onCooldown(e)
            return
        }
        val action = e.action

        when (action) {
            Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK -> onLeftClick(e)
            else -> onRightClick(e)
        }

        playerCooldown[e.player] = initialCooldown
    }

    override fun equals(other: Any?): Boolean {
        if (other !is ItemStack) {
            return false
        }

        if (other.itemMeta == null) {
            return other == this
        }

        if (!other.itemMeta!!.persistentDataContainer.has(
                VitalNamespacedKey.ITEM_UUID,
                PersistentDataType.STRING
            )
        ) {
            return toString() == other.toString()
                .replace("${other.type} x ${other.amount}", "${other.type} x 1")
        }
        val uuid = UUID.fromString(
            itemMeta!!.persistentDataContainer.get(
                VitalNamespacedKey.ITEM_UUID,
                PersistentDataType.STRING
            )
        )
        val otherUuid = UUID.fromString(
            other.itemMeta!!.persistentDataContainer.get(
                VitalNamespacedKey.ITEM_UUID,
                PersistentDataType.STRING
            )
        )

        return uuid == otherUuid
    }

    override fun toString() = super.toString().replace("$type x $amount", "$type x 1");

    open fun onLeftClick(e: PlayerInteractEvent) {}
    open fun onRightClick(e: PlayerInteractEvent) {}
    open fun onCooldown(e: PlayerInteractEvent) {}
    open fun onCooldownExpire(e: SpigotPlayer) {}
    open fun onCooldownTick(e: SpigotPlayer) {}

    @Component
    @Target(AnnotationTarget.TYPE)
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