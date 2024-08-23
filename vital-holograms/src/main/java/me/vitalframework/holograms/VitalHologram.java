package me.vitalframework.holograms;

import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.vitalframework.VitalComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a hologram in the Vital-Framework.
 *
 * @author xRa1ny
 */
public class VitalHologram implements VitalComponent {
    @Getter
    private final UUID uniqueId = UUID.randomUUID();

    @Getter
    @Setter
    private String name;

    @Getter
    private ArmorStand base;

    @Getter
    @Setter
    private List<ArmorStand> baseLines = new ArrayList<>();

    @Getter
    @Setter
    private List<String> lines = new ArrayList<>();

    @Getter
    @Setter
    private Location location;

    @Getter
    @Setter
    private Material displayType;

    public VitalHologram(@NonNull String name, @NonNull Location location, @Nullable Material displayType, @NonNull String... lines) {
        this.name = name;
        this.lines.addAll(List.of(lines));
        this.location = location;
        this.displayType = displayType;
        update();
    }

    public void remove() {
        for (Entity entity : base.getPassengers()) {
            entity.remove();
        }

        for (ArmorStand armorStand : baseLines) {
            armorStand.remove();
        }

        base.remove();
    }

    public void update() {
        if (base == null) {
            base = location.getWorld().spawn(location, ArmorStand.class);
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
                armorStand = location.getWorld().spawn(lineLocation.subtract(0, 2 + (.25 * i), 0), ArmorStand.class);
                baseLines.add(armorStand);
            } else {
                armorStand = baseLines.get(i);
                armorStand.teleport(lineLocation);
            }

            armorStand.setVisible(false);
            armorStand.setMarker(true);
            armorStand.setCustomName(line);
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