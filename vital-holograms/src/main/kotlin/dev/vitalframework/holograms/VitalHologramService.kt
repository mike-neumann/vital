package dev.vitalframework.holograms

import dev.vitalframework.SpigotPlayer
import dev.vitalframework.SpigotPlugin
import dev.vitalframework.VitalCoreModule.Companion.logger
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.inventory.ItemStack
import java.util.UUID

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
     * Creates a hologram at the given [location] which displays all given [lines] that is visible to all players on the server.
     * The created hologram is stored in [VitalHologramRepository].
     */
    fun createGlobalHologram(
        lines: List<VitalHologram.Line>,
        location: Location,
    ): VitalGlobalHologram {
        val loggingContext = "Context: lines '$lines', location: '$location'."
        logger.debug("Creating global hologram. $loggingContext")

        val armorStand =
            location.world!!.spawn(location, ArmorStand::class.java) {
                it.isVisible = false
                it.isInvisible = true
                it.isMarker = true
            }
        val lineArmorStandUniqueIds = lines.createArmorStands(location).map { it.uniqueId }
        val hologram =
            vitalHologramRepository.save(
                VitalGlobalHologram(UUID.randomUUID(), lines, location, armorStand.uniqueId, lineArmorStandUniqueIds),
            )

        logger.debug("Global hologram created. $loggingContext")

        return hologram
    }

    /**
     * Creates a hologram at the given [location] which displays the given [lines] that is visible to only the passed [player].
     * The created hologram is stored in [VitalHologramRepository].
     */
    fun createPerPlayerHologram(
        player: SpigotPlayer,
        lines: List<VitalHologram.Line>,
        location: Location,
    ): VitalPerPlayerHologram {
        val loggingContext = "Context: player '$player', lines '$lines', location: '$location'."

        logger.debug("Creating per player hologram. $loggingContext")
        val armorStand =
            location.world!!.spawn(location, ArmorStand::class.java) {
                it.isVisible = false
                it.isInvisible = true
                it.isMarker = true
            }

        val hologram =
            VitalPerPlayerHologram(
                UUID.randomUUID(),
                lines,
                location,
                armorStand.uniqueId,
                lines.createArmorStands(location).map { it.uniqueId },
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
     * Internal function; used to convert a list on content-lines to armor stands.
     * This function will spawn the armor stands and return them as a list.
     *
     * Additionally, an [action] can be performed for each spawned armor stand.
     */
    private fun List<VitalHologram.Line>.createArmorStands(
        location: Location,
        action: (ArmorStand) -> Unit = {},
    ): List<ArmorStand> {
        val loggingContext = "Context: location '$location', lines '$this'."
        logger.debug("Creating armor stands for each hologram line. $loggingContext")
        return reversed().mapIndexed { i, line ->
            logger.debug("Creating armor stand for hologram line '${line.text}'. $loggingContext")
            // convert the minimessage formatted line into a legacy section formatted line.
            val formattedLine =
                LegacyComponentSerializer.legacySection().serialize(MiniMessage.miniMessage().deserialize(line.text ?: ""))

            val armorStand =
                location.world!!
                    .spawn(location.clone().add(0.0, .25 * i, 0.0), ArmorStand::class.java) {
                        it.isVisible = false
                        it.isInvisible = true
                        it.isMarker = true

                        if (formattedLine.isNotEmpty()) {
                            it.isCustomNameVisible = true
                            it.customName(MiniMessage.miniMessage().deserialize(formattedLine))
                        }

                        if (line.material != null) {
                            it.addPassenger(
                                it.world.dropItem(it.location, ItemStack(line.material)) {
                                    it.isUnlimitedLifetime = true
                                    it.setCanPlayerPickup(false)
                                    it.setCanMobPickup(false)
                                },
                            )
                        }
                    }
            action(armorStand)
            armorStand
        }
    }

    /**
     * Hides all other known holograms that are registered in [VitalHologramRepository] and are not owned by the given [player].
     * Note that this function will only work correctly, if all holograms are loaded into [VitalHologramRepository].
     * If not, this function will fail to hide other holograms since Vital is not aware of their existence.
     */
    fun hideOtherPerPlayerHolograms(player: SpigotPlayer) {
        val loggingContext = "Context: player '$player'."
        logger.debug("Hiding all other holograms for player '$player'. $loggingContext")
        val holograms = vitalHologramRepository.findAll<VitalPerPlayerHologram>().filter { it.playerUniqueId != player.uniqueId }
        for (hologram in holograms) {
            logger.debug("Hiding hologram '${hologram.id}' for player '$player'. $loggingContext")
            hideHologram(player, hologram)
        }
    }
}
