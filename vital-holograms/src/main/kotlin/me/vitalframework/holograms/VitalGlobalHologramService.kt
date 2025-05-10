package me.vitalframework.holograms

import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.springframework.stereotype.Service
import java.util.*

/**
 * Service class for managing and interacting with global holograms in the game world.
 *
 * A global hologram is a collection of invisible marker armor stands that visually display
 * multiple lines of text at a specified location in the game world. This service allows for
 * the creation, retrieval, and removal of these holograms while ensuring their persistence
 * through a repository.
 */
@Service
class VitalGlobalHologramService(val globalHologramRepository: VitalGlobalHologramRepository) {
    /**
     * Creates and spawns a global hologram at the specified location with the given name and lines of content.
     *
     * This method generates an invisible marker armor stand to act as the anchor for the hologram,
     * and additional invisible marker armor stands for each line of the hologram's text. The hologram's
     * details are then persisted by saving them to the hologram repository.
     *
     * @param name The unique name of the hologram.
     * @param lines A list of strings representing the lines of text to display in the hologram.
     *              Each line is processed and formatted appropriately.
     * @param location The location in the game world where the hologram will be created.
     */
    fun createGlobalHologram(name: String, lines: List<String>, location: Location) {
        // now we can actually spawn the hologram.
        val armorStand = location.world!!.spawn(location, ArmorStand::class.java) {
            it.isVisible = false
            it.isMarker = true
        }
        val lineArmorStandUniqueIds = lines.reversed().mapIndexed { i, line ->
            // convert the minimessage formatted line into a legacy section formatted line.
            val formattedLine = LegacyComponentSerializer.legacySection().serialize(MiniMessage.miniMessage().deserialize(line))

            location.world!!.spawn(location.clone().add(0.0, .25 * i, 0.0), ArmorStand::class.java) {
                it.isVisible = false
                it.isMarker = true
                it.isCustomNameVisible = true
                it.customName(MiniMessage.miniMessage().deserialize(formattedLine))
            }.uniqueId
        }

        globalHologramRepository.save(
            VitalGlobalHologram(UUID.randomUUID(), name, lines, location, armorStand.uniqueId, lineArmorStandUniqueIds)
        )
    }

    /**
     * Retrieves all global holograms managed by the repository.
     *
     * This method returns a collection of `VitalGlobalHologram` instances, which represent global holograms
     * that have been created and are currently being tracked within the system. These holograms use
     * hidden entities such as armor stands to display multiple lines of string content at specific
     * locations in the game world.
     *
     * @return A collection of all global holograms currently stored in the repository.
     */
    fun getAllGlobalHolograms() = globalHologramRepository.entities

    /**
     * Retrieves a global hologram associated with the specified unique identifier.
     *
     * @param uniqueId The unique identifier of the global hologram to retrieve.
     * @return An instance of the global hologram if it exists, or null if no hologram is found for the given identifier.
     */
    fun getGlobalHologram(uniqueId: UUID) = globalHologramRepository.get(uniqueId)

    /**
     * Retrieves a global hologram by its name.
     *
     * This method queries the global hologram repository to find a hologram
     * with the specified name. If no matching hologram is found, the method
     * will return `null`.
     *
     * @param name The name of the global hologram to retrieve.
     * @return The global hologram corresponding to the specified name, or `null` if it does not exist.
     */
    fun getGlobalHologram(name: String) = globalHologramRepository.get(name)

    /**
     * Removes a global hologram from the game world and the repository.
     *
     * This method ensures the persistent removal of a specified `globalHologram` by deleting both its
     * main armor stand and its associated line-specific armor stands from the world. It then removes the hologram from
     * the repository, ensuring that it is no longer tracked by the application. If the specified hologram does not exist
     * in the repository, the method is a no-op.
     *
     * @param globalHologram The instance of `VitalGlobalHologram` to be removed. It must exist in the repository.
     */
    fun removeGlobalHologram(globalHologram: VitalGlobalHologram) {
        if (!globalHologramRepository.exists(globalHologram)) return
        val armorStand = Bukkit.getEntity(globalHologram.armorStandUniqueId)!!
        val lineArmorStands = globalHologram.lineArmorStandUniqueIds.map { Bukkit.getEntity(it)!! }

        armorStand.remove()

        for (lineArmorStand in lineArmorStands) {
            lineArmorStand.remove()
        }

        globalHologramRepository.delete(globalHologram)
    }
}