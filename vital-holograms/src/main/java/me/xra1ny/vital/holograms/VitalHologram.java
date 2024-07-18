package me.xra1ny.vital.holograms;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import me.xra1ny.vital.VitalComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.kyori.adventure.text.Component.text;

/**
 * Represents a hologram in the Vital-Framework.
 *
 * @author xRa1ny
 */
public class VitalHologram implements VitalComponent {

    /**
     * The name of this hologram.
     */
    @Getter
    @Setter
    @NonNull
    private String name;

    /**
     * The base armor stand of this hologram.
     */
    @Getter
    @NonNull
    private ArmorStand base;

    /**
     * The armor stand lines of this hologram.
     */
    @Getter
    @Setter
    @NonNull
    private List<ArmorStand> baseLines = new ArrayList<>();

    /**
     * The lines of this hologram.
     */
    @Getter
    @Setter
    @NonNull
    private List<String> lines = new ArrayList<>();

    /**
     * The location of this hologram.
     */
    @Getter
    @Setter
    @NonNull
    private Location location;

    /**
     * The item this hologram displays.
     */
    @Getter
    @Setter
    @NonNull
    private Material displayType;

    /**
     * Constructs a new VitalHologram instance.
     *
     * @param name        The name of the hologram.
     * @param location    The location where the hologram should be displayed.
     * @param displayType The material type to display as an item (nullable).
     * @param lines       The lines of text to display in the hologram.
     */
    @SneakyThrows
    public VitalHologram(@NonNull String name, @NonNull Location location, @Nullable Material displayType, @NonNull String... lines) {
        this.name = name;
        this.lines.addAll(List.of(lines));
        this.location = location;
        this.displayType = displayType;
    }

    /**
     * Removes this hologram and its associated entities.
     */
    public void remove() {
        for (Entity entity : base.getPassengers()) {
            entity.remove();
        }

        for (ArmorStand armorStand : baseLines) {
            armorStand.remove();
        }

        base.remove();
    }

    /**
     * Updates the hologram by recreating its elements.
     */
    public void update() {
        if (base == null) {
            base = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        }

        base.setVisible(false);
        base.setMarker(true);
        base.teleport(location);

        if (displayType != null) {
            for (Entity entity : base.getPassengers()) {
                entity.remove();
            }

            final Item item = location.getWorld().dropItem(base.getEyeLocation(), new ItemStack(displayType));

            item.setPickupDelay(Integer.MAX_VALUE);
            base.addPassenger(item);
        }

        final int initialBaseLineSize = baseLines.size();

        for (int i = lines.size() - 1; i > -1; i--) {
            final String line = LegacyComponentSerializer.legacySection().serialize(MiniMessage.miniMessage().deserialize(lines.get(i)));
            final Location lineLocation = location.clone().add(0, lines.size(), 0);
            final ArmorStand armorStand;

            if (i >= initialBaseLineSize) {
                armorStand = (ArmorStand) location.getWorld().spawnEntity(lineLocation.subtract(0, 2 + (.25 * i), 0), EntityType.ARMOR_STAND);
                baseLines.add(armorStand);
            } else {
                armorStand = baseLines.get(i);
                armorStand.teleport(lineLocation);
            }

            armorStand.setVisible(false);
            armorStand.setMarker(true);
            armorStand.customName(text(line));
            armorStand.setCustomNameVisible(true);
        }
    }

    @Override
    public void onRegistered() {

    }

    @Override
    public void onUnregistered() {

    }
}