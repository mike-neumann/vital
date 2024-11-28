package me.vitalframework.holograms;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.vitalframework.VitalComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a hologram in the Vital-Framework.
 *
 * @author xRa1ny
 */
@Getter
public class VitalHologram implements VitalComponent {
    @NonNull
    private final UUID uniqueId = UUID.randomUUID();

    @Setter
    private String name;

    private ArmorStand base;

    @Setter
    @NonNull
    private List<ArmorStand> baseLines = new ArrayList<>();

    @Setter
    @NonNull
    private List<String> lines = new ArrayList<>();

    @Setter
    @NonNull
    private Location location;

    @Setter
    private Material displayType;

    public VitalHologram(@NonNull String name, @NonNull Location location, Material displayType, @NonNull String... lines) {
        this.name = name;
        this.lines.addAll(List.of(lines));
        this.location = location;
        this.displayType = displayType;
        update();
    }

    public void remove() {
        for (var entity : base.getPassengers()) {
            entity.remove();
        }

        for (var armorStand : baseLines) {
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
            for (var entity : base.getPassengers()) {
                entity.remove();
            }

            final var item = location.getWorld().dropItem(base.getEyeLocation(), new ItemStack(displayType));

            item.setPickupDelay(Integer.MAX_VALUE);
            base.addPassenger(item);
        }

        final var initialBaseLineSize = baseLines.size();

        for (var i = lines.size() - 1; i > -1; i--) {
            final var line = LegacyComponentSerializer.legacySection().serialize(MiniMessage.miniMessage().deserialize(lines.get(i)));
            final var lineLocation = location.clone().add(0, lines.size(), 0);
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