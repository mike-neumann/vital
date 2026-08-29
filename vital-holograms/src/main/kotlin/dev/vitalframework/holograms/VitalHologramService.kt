package dev.vitalframework.holograms

import dev.vitalframework.SpigotPlayer
import dev.vitalframework.SpigotPlugin
import dev.vitalframework.VitalCoreModule.Companion.logger
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.Item
import org.bukkit.inventory.ItemStack
import java.util.UUID
import java.util.function.Function
import java.util.function.Supplier
import kotlin.collections.reversed

/**
 * Global service to create and delete holograms.
 * Every created hologram will be stored globally in [VitalHologramRepository].
 */
class VitalHologramService(
    private val plugin: SpigotPlugin,
    private val vitalHologramRepository: VitalHologramRepository,
) {
    private val logger = logger()

    /**
     * Internal function; creates the [ArmorStand] entities for the given [lines].
     */
    private fun createHologramLineArmorStands(
        baseArmorStand: Entity,
        lines: List<VitalHologram.Line>,
    ): List<ArmorStand> =
        lines.reversed().mapIndexed { i, line ->
            val loggingContext = "Line: line '${line.text}', material '${line.material}'"
            logger.debug("Creating new line. $loggingContext")

            val newLineArmorStand =
                baseArmorStand.world
                    .spawn(baseArmorStand.location.clone().add(0.0, .25 * i, 0.0), ArmorStand::class.java) {
                        it.isVisible = false
                        it.isInvisible = true
                        it.isMarker = true
                        it.setGravity(false)

                        val lineText = line.text?.let { MiniMessage.miniMessage().deserialize(it) }
                        if (lineText != null) {
                            it.isCustomNameVisible = true
                            it.customName(lineText)
                        }

                        if (line.material != null) {
                            val lineItem =
                                it.world.dropItem(it.location, ItemStack(line.material)) {
                                    it.isUnlimitedLifetime = true
                                    it.setCanPlayerPickup(false)
                                    it.setCanMobPickup(false)
                                }
                            it.addPassenger(lineItem)
                        }
                    }

            logger.debug("New line created. $loggingContext")
            newLineArmorStand
        }

    /**
     * Updates all lines of the given [hologram].
     * If the hologram doesn't exist, this function will throw [IllegalStateException].
     */
    fun updateHologram(hologram: VitalHologram) {
        val lines = hologram.lines.map { it.get() }
        val loggingContext = "Context: location '${hologram.location}', lines '$lines'."
        val baseArmorStand =
            Bukkit.getEntity(hologram.armorStandUniqueId)
                ?: let {
                    logger.debug(
                        "Cannot update hologram, entity with uuid '${hologram.armorStandUniqueId}' does not exist, attempting to load chunk and retrying.",
                    )
                    hologram.location.chunk.load()

                    Bukkit.getEntity(hologram.armorStandUniqueId)
                        ?: throw IllegalStateException(
                            "Cannot update hologram, entity with uuid '${hologram.armorStandUniqueId}' does not exist.",
                        )
                }

        val lineArmorStands =
            hologram.lineArmorStandUniqueIds
                .map {
                    Bukkit.getEntity(it) as? ArmorStand?
                        ?: throw VitalHologramException.InvalidEntity(hologram.id, it)
                }.toMutableList()
        if (lineArmorStands.size != lines.size) {
            logger.debug("Line size has changed, will recreate all line holograms. $loggingContext")

            for (lineArmorStand in lineArmorStands) {
                lineArmorStand.remove()
            }

            lineArmorStands.clear()
            lineArmorStands.addAll(createHologramLineArmorStands(baseArmorStand, lines))
        }

        logger.debug("Updating all '${lineArmorStands.size}' lines. $loggingContext")
        for ((lineArmorStand, line) in lineArmorStands.zip(lines)) {
            val loggingContext = "$loggingContext - Line: text '${line.text}', material '${line.material}'."
            logger.debug("Updating line. $loggingContext")

            lineArmorStand.isVisible = false
            lineArmorStand.isInvisible = true
            lineArmorStand.isMarker = true
            lineArmorStand.isCustomNameVisible = true
            lineArmorStand.setGravity(false)

            val oldLineText = lineArmorStand.customName()?.let { PlainTextComponentSerializer.plainText().serialize(it) }
            if (oldLineText != line.text) {
                logger.debug("Line has changed, will update. $loggingContext")
                val newLine = line.text?.let { MiniMessage.miniMessage().deserialize(it) }
                lineArmorStand.customName(newLine)
            }

            val oldLineItem = lineArmorStand.passengers.filterIsInstance<Item>().firstOrNull()
            if (oldLineItem?.itemStack?.type != line.material) {
                logger.debug("Material has changed, will update, new material is '${line.material}'. $loggingContext")
                lineArmorStand.removePassenger(oldLineItem!!)
                oldLineItem.remove()

                if (line.material != null) {
                    val newLineItem =
                        lineArmorStand.world.dropItem(lineArmorStand.location, ItemStack(line.material)) {
                            it.isUnlimitedLifetime = true
                            it.setCanPlayerPickup(false)
                            it.setCanMobPickup(false)
                        }

                    lineArmorStand.addPassenger(newLineItem)
                }
            }

            logger.debug("Line update complete. $loggingContext")
        }

        logger.debug("All '${lineArmorStands.size}' lines have been updated. $loggingContext")
    }

    /**
     * Creates a hologram at the given [location] which displays all given [lines] that is visible to all players on the server.
     * The created hologram is stored in [VitalHologramRepository].
     */
    fun createGlobalHologram(
        location: Location,
        vararg linesFunctions: Supplier<VitalHologram.Line>,
    ): VitalHologram {
        val lines = linesFunctions.map { it.get() }
        val loggingContext = "Context: lines '$lines', location: '$location'."
        logger.debug("Creating global hologram. $loggingContext")

        val armorStand =
            location.world!!.spawn(location, ArmorStand::class.java) {
                it.isVisible = false
                it.isInvisible = true
                it.isMarker = true
                it.setGravity(false)
            }
        val lineArmorStandUniqueIds = createHologramLineArmorStands(armorStand, lines).map { it.uniqueId }
        val hologram =
            vitalHologramRepository.save(
                VitalHologram(UUID.randomUUID(), linesFunctions.toList(), location, armorStand.uniqueId, lineArmorStandUniqueIds),
            )

        logger.debug("Global hologram created. $loggingContext")
        return hologram
    }

    /**
     * Creates a hologram at the given [location] which displays the given [lines] that is visible to only the passed [player].
     * The created hologram is stored in [VitalHologramRepository].
     */
    fun createPlayerHologram(
        player: SpigotPlayer,
        location: Location,
        vararg linesFunctions: Function<SpigotPlayer, VitalHologram.Line>,
    ): VitalPlayerHologram {
        val lines = linesFunctions.map { it.apply(player) }
        val loggingContext = "Context: player '$player', lines '$lines', location: '$location'."

        logger.debug("Creating per player hologram. $loggingContext")
        val armorStand =
            location.world!!.spawn(location, ArmorStand::class.java) {
                it.isVisible = false
                it.isInvisible = true
                it.isMarker = true
                it.setGravity(false)
            }

        val linesArmorStandUniqueIds = createHologramLineArmorStands(armorStand, lines).map { it.uniqueId }
        val hologram =
            VitalPlayerHologram(
                UUID.randomUUID(),
                linesFunctions.toList(),
                location,
                armorStand.uniqueId,
                linesArmorStandUniqueIds,
                player.uniqueId,
            )

        // hide for everyone except the creator
        for (player in Bukkit.getOnlinePlayers().filter { it.uniqueId != player.uniqueId }) {
            hideHologram(player, hologram)
        }

        vitalHologramRepository.save(hologram)
        logger.debug("Per player hologram created. $loggingContext")
        return hologram
    }

    /**
     * Hides a hologram by the given [armorStandUniqueId] and [lineArmorStandUniqueIds] for the given [player].
     */
    fun hideHologram(
        player: SpigotPlayer,
        armorStandUniqueId: UUID,
        lineArmorStandUniqueIds: List<UUID>,
    ) {
        val loggingContext =
            "Context: player '$player', armor stand uuid '$armorStandUniqueId', " +
                "line armor stand uuids '$lineArmorStandUniqueIds'."

        logger.debug("Hiding hologram. $loggingContext")
        player.hideEntity(plugin, Bukkit.getEntity(armorStandUniqueId)!!)

        for (lineArmorStandUniqueId in lineArmorStandUniqueIds) {
            logger.debug("Hiding hologram for player '$player'. $loggingContext")
            player.hideEntity(plugin, Bukkit.getEntity(lineArmorStandUniqueId)!!)
        }
    }

    /**
     * Hides the given [hologram] for the given [player].
     */
    fun hideHologram(
        player: SpigotPlayer,
        hologram: VitalHologram,
    ) {
        hideHologram(player, hologram.armorStandUniqueId, hologram.lineArmorStandUniqueIds)
    }

    /**
     * Shows a hologram by the given [armorStandUniqueId] and [lineArmorStandUniqueIds] for the given [player].
     * If the hologram with the given [armorStandUniqueId] and [lineArmorStandUniqueIds] has been hidden using [hideHologram] before,
     * this function will make it visible again.
     */
    fun showHologram(
        player: SpigotPlayer,
        armorStandUniqueId: UUID,
        lineArmorStandUniqueIds: List<UUID>,
    ) {
        val loggingContext =
            "Context: player '$player', armor stand uuid '$armorStandUniqueId', " +
                "line armor stand uuids '$lineArmorStandUniqueIds'."
        logger.debug("Showing hologram. $loggingContext")
        player.showEntity(plugin, Bukkit.getEntity(armorStandUniqueId)!!)

        for (lineArmorStandUniqueId in lineArmorStandUniqueIds) {
            logger.debug("Showing hologram for player '$player'. $loggingContext")
            player.showEntity(plugin, Bukkit.getEntity(lineArmorStandUniqueId)!!)
        }
    }

    /**
     * Shows the given [hologram] for the given [player].
     * If given [hologram] has been hidden using [hideHologram] before,
     * this function will make it visible again.
     */
    fun showHologram(
        player: SpigotPlayer,
        hologram: VitalHologram,
    ) {
        showHologram(player, hologram.armorStandUniqueId, hologram.lineArmorStandUniqueIds)
    }

    /**
     * Deletes a hologram by the given [armorStandUniqueId] and [lineArmorStandUniqueIds].
     * The hologram will also be deleted from [VitalHologramRepository].
     */
    fun deleteHologram(
        armorStandUniqueId: UUID,
        lineArmorStandUniqueIds: List<UUID>,
    ) {
        val loggingContext = "Context: armor stand uuid '$armorStandUniqueId', line armor stand uuids '$lineArmorStandUniqueIds'."
        val armorStand = Bukkit.getEntity(armorStandUniqueId)
        if (armorStand != null) {
            logger.debug("Deleting hologram. $loggingContext")
        } else {
            throw NullPointerException()
        }

        val lineArmorStands = lineArmorStandUniqueIds.map { Bukkit.getEntity(it)!! }
        armorStand.remove()

        for (lineArmorStand in lineArmorStands) {
            lineArmorStand.remove()
        }
    }

    /**
     * Deletes the given [hologram].
     * The hologram will also be deleted from [VitalHologramRepository].
     */
    fun deleteHologram(hologram: VitalHologram) {
        deleteHologram(hologram.armorStandUniqueId, hologram.lineArmorStandUniqueIds)
    }

    /**
     * Hides all other known holograms that are registered in [VitalHologramRepository] and are not owned by the given [player].
     * Note that this function will only work correctly, if all holograms are loaded into [VitalHologramRepository].
     * If not, this function will fail to hide other holograms since Vital is not aware of their existence.
     */
    fun hideOtherPlayerHolograms(player: SpigotPlayer) {
        val loggingContext = "Context: player '$player'."
        logger.debug("Hiding all other holograms for player '$player'. $loggingContext")
        val holograms = vitalHologramRepository.findAll<VitalPlayerHologram>().filter { it.playerUniqueId != player.uniqueId }
        for (hologram in holograms) {
            logger.debug("Hiding hologram '${hologram.id}' for player '$player'. $loggingContext")
            hideHologram(player, hologram)
        }
    }
}
