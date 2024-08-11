package me.vitalframework.configs;

import lombok.NonNull;
import me.vitalframework.configs.annotation.Property;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;

/**
 * Wrapper class to store location data to a config file.
 *
 * @author xRa1ny
 */
public class ConfigLocation {
    @Property(String.class)
    public String world;

    @Property(double.class)
    public double x;

    @Property(double.class)
    public double y;

    @Property(double.class)
    public double z;

    @Property(float.class)
    public float pitch;

    @Property(float.class)
    public float yaw;

    @NonNull
    public static ConfigLocation of(@NonNull Location location) {
        final ConfigLocation configLocation = new ConfigLocation();

        configLocation.world = location.getWorld().getName();
        configLocation.x = location.getX();
        configLocation.y = location.getY();
        configLocation.z = location.getZ();
        configLocation.pitch = location.getPitch();
        configLocation.yaw = location.getYaw();

        return configLocation;
    }

    @NonNull
    public Location toLocation() {
        World world = Bukkit.getWorld(this.world);

        if (world == null) {
            // load world if null
            world = new WorldCreator(this.world)
                    .createWorld();
        }

        return new Location(world, x, y, z, yaw, pitch);

    }
}