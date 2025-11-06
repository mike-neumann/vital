package me.vitalframework.holograms

import me.vitalframework.SpigotPlayer
import me.vitalframework.SpigotPlugin
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Service class to create and delete instances of [VitalHologram].
 */
@Service
class VitalHologramService(
    private val plugin: SpigotPlugin,
    private val vitalPerPlayerHologramProviders: List<VitalHologramProvider<VitalPerPlayerHologram>>,
) {
    /**
     * Creates a hologram at the given [location] which displays all given [lines] that is visible to all players on the server.
     */
    fun createGlobalHologram(
        lines: List<String>,
        location: Location,
    ): VitalGlobalHologram {
        val armorStand =
            location.world!!.spawn(location, ArmorStand::class.java) {
                it.isVisible = false
                it.isInvisible = true
                it.isMarker = true
            }
        val lineArmorStandUniqueIds = lines.createArmorStands(location).map { it.uniqueId }
        return VitalGlobalHologram(UUID.randomUUID(), lines, location, armorStand.uniqueId, lineArmorStandUniqueIds)
    }

    /**
     * Creates a hologram at the given [location] which displays the given [lines] that is visible to only the passed [player].
     */
    fun createPerPlayerHologram(
        player: SpigotPlayer,
        lines: List<String>,
        location: Location,
    ): VitalPerPlayerHologram {
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
        player.hideEntity(plugin, Bukkit.getEntity(armorStandUniqueId)!!)

        for (lineArmorStandUniqueId in lineArmorStandUniqueIds) {
            player.hideEntity(plugin, Bukkit.getEntity(lineArmorStandUniqueId)!!)
        }
    }

    /**
     * Hides the given [hologram] for the given [player].
     */
    fun hideHologram(
        player: SpigotPlayer,
        hologram: VitalHologram<*>,
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
        player.showEntity(plugin, Bukkit.getEntity(armorStandUniqueId)!!)

        for (lineArmorStandUniqueId in lineArmorStandUniqueIds) {
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
        hologram: VitalHologram<*>,
    ) {
        showHologram(player, hologram.armorStandUniqueId, hologram.lineArmorStandUniqueIds)
    }

    /**
     * Deletes a hologram by the given [armorStandUniqueId] and [lineArmorStandUniqueIds].
     */
    fun deleteHologram(
        armorStandUniqueId: UUID,
        lineArmorStandUniqueIds: List<UUID>,
    ) {
        val armorStand = Bukkit.getEntity(armorStandUniqueId)!!
        val lineArmorStands = lineArmorStandUniqueIds.map { Bukkit.getEntity(it)!! }

        armorStand.remove()

        for (lineArmorStand in lineArmorStands) {
            lineArmorStand.remove()
        }
    }

    /**
     * Deletes the given [hologram].
     */
    fun deleteHologram(hologram: VitalHologram<*>) {
        deleteHologram(hologram.armorStandUniqueId, hologram.lineArmorStandUniqueIds)
    }

    /**
     * Utility extension-function to convert a list on content-lines to armor stands.
     * This function will spawn the armor stands and return them as a list.
     *
     * Additionally, an [action] can be performed for each spawned armor stand.
     */
    private fun List<String>.createArmorStands(
        location: Location,
        action: (ArmorStand) -> Unit = {},
    ): List<ArmorStand> =
        reversed().mapIndexed { i, line ->
            // convert the minimessage formatted line into a legacy section formatted line.
            val formattedLine =
                LegacyComponentSerializer.legacySection().serialize(MiniMessage.miniMessage().deserialize(line))

            val armorStand =
                location.world!!
                    .spawn(location.clone().add(0.0, .25 * i, 0.0), ArmorStand::class.java) {
                        it.isVisible = false
                        it.isInvisible = true
                        it.isMarker = true
                        it.isCustomNameVisible = true
                        it.customName(MiniMessage.miniMessage().deserialize(formattedLine))
                    }
            action(armorStand)
            armorStand
        }

    /**
     * Hides all other [VitalPerPlayerHologram]'s for the given [player].
     * This function uses [VitalHologramProvider] to provide all [VitalPerPlayerHologram]'s.
     *
     * By default, no providers are known. The consuming project will need to implement its own [VitalHologramProvider],
     * since Vital doesn't know how the consuming project stores its holograms.
     * Some may use vital-configs for that, while others may use a database.
     */
    fun hideOtherPerPlayerHolograms(player: SpigotPlayer) {
        for (provider in vitalPerPlayerHologramProviders) {
            val otherHolograms = provider.provide().filter { it.playerUniqueId != player.uniqueId }
            for (hologram in otherHolograms) {
                hideHologram(player, hologram)
            }
        }
    }
}
