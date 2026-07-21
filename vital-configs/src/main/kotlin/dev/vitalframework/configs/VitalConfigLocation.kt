package dev.vitalframework.configs

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.WorldCreator

/**
 * Wrapper class to store a [Location] in a [VitalConfig].
 * Normal Bukkit locations can't be serialized, that's why this class exists.
 */
class VitalConfigLocation {
    companion object {
        @JvmStatic
        fun of(location: Location) =
            VitalConfigLocation().apply {
                world = location.world!!.name
                x = location.x
                y = location.y
                z = location.z
                pitch = location.pitch
                yaw = location.yaw
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
        if (world == null) world = WorldCreator(this.world!!).createWorld()
        return Location(world, x, y, z, yaw, pitch)
    }
}
