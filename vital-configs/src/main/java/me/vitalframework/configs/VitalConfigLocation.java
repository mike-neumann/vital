package me.vitalframework.configs;

import lombok.NonNull;
import me.vitalframework.configs.annotation.VitalConfigProperty;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WorldCreator;

/**
 * Wrapper class to store location data to a config file.
 *
 * @author xRa1ny
 */
public class VitalConfigLocation {
    @VitalConfigProperty(String.class)
    public String world;

    @VitalConfigProperty(double.class)
    public double x;

    @VitalConfigProperty(double.class)
    public double y;

    @VitalConfigProperty(double.class)
    public double z;

    @VitalConfigProperty(float.class)
    public float pitch;

    @VitalConfigProperty(float.class)
    public float yaw;

    @NonNull
    public static VitalConfigLocation of(@NonNull Location location) {
        final var vitalConfigLocation = new VitalConfigLocation();

        vitalConfigLocation.world = location.getWorld().getName();
        vitalConfigLocation.x = location.getX();
        vitalConfigLocation.y = location.getY();
        vitalConfigLocation.z = location.getZ();
        vitalConfigLocation.pitch = location.getPitch();
        vitalConfigLocation.yaw = location.getYaw();

        return vitalConfigLocation;
    }

    @NonNull
    public Location toLocation() {
        var world = Bukkit.getWorld(this.world);

        if (world == null) {
            // load world if null
            world = new WorldCreator(this.world)
                    .createWorld();
        }

        return new Location(world, x, y, z, yaw, pitch);

    }
}