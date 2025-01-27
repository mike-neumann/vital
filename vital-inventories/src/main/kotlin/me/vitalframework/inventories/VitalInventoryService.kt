package me.vitalframework.inventories;

import lombok.NonNull;
import me.vitalframework.Vital;
import org.bukkit.entity.Player;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * The main vital inventory service for registering inventories.
 */
@Service
public class VitalInventoryService {
    /**
     * Opens a registered {@link VitalInventory} for the given {@link Player}.
     */
    public void openInventory(@NonNull Player player, @NonNull Class<? extends VitalInventory> vitalInventoryClass) {
        final var vitalInventory = Vital.INSTANCE.getContext().getBean(vitalInventoryClass);

        vitalInventory.open(player);
    }

    @NonNull
    public Collection<? extends VitalInventory> getInventories(@NonNull Class<? extends VitalInventory> vitalInventoryClass) {
        try {
            return Vital.INSTANCE.getContext().getBeansOfType(vitalInventoryClass).values();
        } catch (Exception e) {
            return List.of();
        }
    }

    @NonNull
    public Collection<? extends VitalInventory> getInventories() {
        return getInventories(VitalInventory.class);
    }


    public <T extends VitalInventory> T getInventory(@NonNull Class<T> vitalInventoryClass) {
        try {
            return Vital.INSTANCE.getContext().getBean(vitalInventoryClass);
        } catch (Exception e) {
            return null;
        }
    }

    public void updateInventories() {
        getInventories().forEach(VitalInventory::update);
    }
}