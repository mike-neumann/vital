package me.vitalframework.inventories;

import lombok.NonNull;
import me.vitalframework.Vital;
import org.bukkit.entity.Player;
import org.springframework.stereotype.Service;

/**
 * The main vital inventory service for registering inventories.
 */
@Service
public class VitalInventoryService {
    /**
     * Opens a registered {@link VitalInventory} for the given {@link Player}.
     *
     * @param player              The {@link Player} to open the given {@link VitalInventory} for.
     * @param vitalInventoryClass The class of the {@link VitalInventory} to open for the given {@link Player}.
     */
    public void openVitalInventory(@NonNull Player player, @NonNull Class<? extends VitalInventory> vitalInventoryClass) {
        final VitalInventory vitalInventory = Vital.getContext().getBean(vitalInventoryClass);

        vitalInventory.open(player);
    }
}