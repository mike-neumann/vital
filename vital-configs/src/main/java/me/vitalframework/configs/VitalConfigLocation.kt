package me.vitalframework.configs

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.WorldCreator

/**
 * Wrapper class to store location data to a config file.
 */
class VitalConfigLocation {
    companion object {
        fun of(location: Location): VitalConfigLocation {
            val vitalConfigLocation = VitalConfigLocation()

            vitalConfigLocation.world = location.world!!.name
            vitalConfigLocation.x = location.x
            vitalConfigLocation.y = location.y
            vitalConfigLocation.z = location.z
            vitalConfigLocation.pitch = location.pitch
            vitalConfigLocation.yaw = location.yaw

            return vitalConfigLocation
        }
    }

    @VitalConfig.Property(String::class)
    var world: String? = null

    @VitalConfig.Property(Double::class)
    var x: Double = 0.0

    @VitalConfig.Property(Double::class)
    var y: Double = 0.0

    @VitalConfig.Property(Double::class)
    var z: Double = 0.0

    @VitalConfig.Property(Float::class)
    var pitch: Float = 0f

    @VitalConfig.Property(Float::class)
    var yaw: Float = 0f

    fun toLocation(): Location {
        var world = Bukkit.getWorld(this.world!!)

        if (world == null) {
            // load world if null
            world = WorldCreator(this.world!!)
                .createWorld()
        }

        return Location(world, x, y, z, yaw, pitch)
    }
}