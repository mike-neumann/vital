package me.xra1ny.vital.inventories;

import lombok.NonNull;
import lombok.extern.java.Log;
import me.xra1ny.essentia.inject.annotation.Component;
import me.xra1ny.vital.Vital;
import me.xra1ny.vital.VitalComponent;
import org.bukkit.entity.Player;

import java.util.Optional;

@Log
@Component
public class VitalInventoryManager implements VitalComponent {
    private static VitalInventoryManager instance;

    private final Vital<?> vital;

    public VitalInventoryManager(Vital<?> vital) {
        this.vital = vital;
    }

    @Override
    public void onRegistered() {
        instance = this;
    }

    @Override
    public void onUnregistered() {

    }

    /**
     * Opens a registered {@link VitalInventory} for the given {@link Player}.
     *
     * @param player              The {@link Player} to open the given {@link VitalInventory} for.
     * @param vitalInventoryClass The class of the {@link VitalInventory} to open for the given {@link Player}.
     */
    public static void openVitalInventory(@NonNull Player player, @NonNull Class<? extends VitalInventory> vitalInventoryClass) {
        final VitalInventory vitalInventory = Optional.ofNullable(instance.vital.getComponentByType(vitalInventoryClass))
                .orElseThrow(() -> new RuntimeException("attempted opening unregistered inventory %s"
                        .formatted(vitalInventoryClass.getSimpleName())));

        player.openInventory(vitalInventory.getInventory());
    }
}