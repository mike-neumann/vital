package me.vitalframework.inventories;

import me.vitalframework.Vital;
import me.vitalframework.VitalListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Listener for handling VitalInventoryMenu related events.
 *
 * @author xRa1ny
 */
@Component
public class VitalInventoryListener extends VitalListener.Spigot {
    /**
     * Handles the event when a player clicks in an inventory.
     *
     * @param e The InventoryClickEvent.
     */
    @EventHandler
    public void onPlayerClickInInventory(InventoryClickEvent e) {
        final var clickedInventory = e.getClickedInventory();
        final var player = (Player) e.getWhoClicked();
        final var vitalInventory = Vital.getContext().getBeansOfType(VitalInventory.class).values().stream()
                .filter(inventory -> inventory.hasInventoryOpen(player))
                .findFirst().orElse(null);

        if (clickedInventory == null) {
            // TODO: navigate to previous menu, currently still buggy
//            if (vitalInventory != null && vitalInventory.getPreviousInventory() != null) {
//                vitalInventory.close(player);
//                vitalInventory.getPreviousInventory().open(player);
//            }

            return;
        }

        if (vitalInventory == null) {
            return;
        }

        final var optionalCurrentItem = Optional.ofNullable(e.getCurrentItem());

        if (optionalCurrentItem.isEmpty()) {
            return;
        }

        vitalInventory.click(e);
        e.setCancelled(true);
    }

    /**
     * Handles the event when a player closes an inventory.
     *
     * @param e The InventoryCloseEvent.
     */
    @EventHandler
    public void onPlayerCloseInventory(InventoryCloseEvent e) {
        final var player = (Player) e.getPlayer();
        final var vitalInventory = Vital.getContext().getBeansOfType(VitalInventory.class).values().stream()
                .filter(inventory -> inventory.hasInventoryOpen(player))
                .findFirst().orElse(null);

        if (vitalInventory == null) {
            return;
        }

        vitalInventory.close(player);
    }
}