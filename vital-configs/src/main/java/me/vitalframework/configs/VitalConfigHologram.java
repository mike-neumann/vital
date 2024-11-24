package me.vitalframework.configs;

import lombok.NonNull;
import me.vitalframework.holograms.VitalHologram;
import org.bukkit.Material;

import java.util.List;

/**
 * Wrapper class to store vital hologram data to a config file.
 *
 * @author xRa1ny
 */
public class VitalConfigHologram {
    @VitalConfig.Property(String.class)
    public String name;

    @VitalConfig.Property(String.class)
    public List<String> lines;

    @VitalConfig.Property(VitalConfigLocation.class)
    public VitalConfigLocation location;

    @VitalConfig.Property(Material.class)
    public Material displayType;

    @NonNull
    public static VitalConfigHologram of(@NonNull VitalHologram vitalHologram) {
        final var hologram = new VitalConfigHologram();

        hologram.name = vitalHologram.getName();
        hologram.lines = vitalHologram.getLines();
        hologram.location = VitalConfigLocation.of(vitalHologram.getLocation());
        hologram.displayType = vitalHologram.getDisplayType();

        return hologram;
    }

    @NonNull
    public VitalHologram toHologram() {
        return new VitalHologram(name, location.toLocation(), displayType, lines.toArray(String[]::new));
    }
}