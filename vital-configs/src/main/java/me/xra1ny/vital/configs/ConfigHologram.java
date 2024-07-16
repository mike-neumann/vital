package me.xra1ny.vital.configs;

import lombok.NonNull;
import me.xra1ny.vital.configs.annotation.Property;
import me.xra1ny.vital.holograms.VitalHologram;
import org.bukkit.Material;

import java.util.List;

/**
 * Wrapper class to store vital hologram data to a config file.
 *
 * @author xRa1ny
 */
public class ConfigHologram {
    @Property(String.class)
    public String name;

    @Property(String.class)
    public List<String> lines;

    @Property(ConfigLocation.class)
    public ConfigLocation location;

    @Property(Material.class)
    public Material displayType;

    @NonNull
    public static ConfigHologram of(@NonNull VitalHologram vitalHologram) {
        final ConfigHologram hologram = new ConfigHologram();

        hologram.name = vitalHologram.getName();
        hologram.lines = vitalHologram.getLines();
        hologram.location = ConfigLocation.of(vitalHologram.getLocation());
        hologram.displayType = vitalHologram.getDisplayType();

        return hologram;
    }

    @NonNull
    public VitalHologram toHologram() {
        return new VitalHologram(name, location.toLocation(), displayType, lines.toArray(String[]::new));
    }
}