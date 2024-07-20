package me.xra1ny.vital.inventories;

import lombok.NonNull;
import me.xra1ny.vital.Vital;
import org.bukkit.entity.Player;
import org.springframework.stereotype.Component;

/**
 * The main vital inventory manager for registering inventories.
 */
@Component
public class VitalInventoryManager {
    /**
     * Opens a registered {@link VitalInventory} for the given {@link Player}.
     *
     * @param player              The {@link Player} to open the given {@link VitalInventory} for.
     * @param vitalInventoryClass The class of the {@link VitalInventory} to open for the given {@link Player}.
     */
    public void openVitalInventory(@NonNull Player player, @NonNull Class<? extends VitalInventory> vitalInventoryClass) {
        final VitalInventory vitalInventory = Vital.getContext().getBean(vitalInventoryClass);

        player.openInventory(vitalInventory.getInventory());
    }
}