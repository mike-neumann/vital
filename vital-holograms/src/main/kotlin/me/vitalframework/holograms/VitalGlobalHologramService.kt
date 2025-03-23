package me.vitalframework.holograms

import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.springframework.stereotype.Service
import java.util.*

@Service
class VitalGlobalHologramService(val globalHologramRepository: VitalGlobalHologramRepository) {
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
                it.customName = formattedLine
            }.uniqueId
        }

        globalHologramRepository.save(
            VitalGlobalHologram(UUID.randomUUID(), name, lines, location, armorStand.uniqueId, lineArmorStandUniqueIds)
        )
    }

    fun getAllGlobalHolograms() = globalHologramRepository.entities
    fun getGlobalHologram(uniqueId: UUID) = globalHologramRepository.get(uniqueId)
    fun getGlobalHologram(name: String) = globalHologramRepository.get(name)

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