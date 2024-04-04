package me.xra1ny.vital.inventories;

import lombok.NonNull;
import me.xra1ny.essentia.inject.annotation.Component;
import me.xra1ny.vital.VitalListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

/**
 * Listener for handling VitalInventoryMenu related events.
 *
 * @author xRa1ny
 */
@Component
public class VitalInventoryListener implements VitalListener.Spigot {
    /**
     * Handles the event when a player opens an inventory.
     *
     * @param e The InventoryOpenEvent.
     */
    @EventHandler
    public void onPlayerOpenInventory(@NonNull InventoryOpenEvent e) {
        final InventoryHolder inventoryHolder = e.getInventory().getHolder();
        final Player player = (Player) e.getPlayer();

        if (inventoryHolder instanceof VitalInventory vitalInventory) {
            vitalInventory.onOpen(player);

            vitalInventory.onUpdate(player);
            vitalInventory.update();

            if(vitalInventory instanceof VitalPagedInventory vitalPagedInventory) {
                vitalPagedInventory.setPage(1, player);
            }
        }
    }

    /**
     * Handles the event when a player clicks in an inventory.
     *
     * @param e The InventoryClickEvent.
     */
    @EventHandler
    public void onPlayerClickInInventory(@NonNull InventoryClickEvent e) {
        final InventoryHolder inventoryHolder = e.getInventory().getHolder();
        final Optional<Inventory> optionalClickedInventory = Optional.ofNullable(e.getClickedInventory());
        final Player player = (Player) e.getWhoClicked();

        if (inventoryHolder instanceof VitalInventory vitalInventory) {
            // If the User clicks outside of Inventory Window, close it
            final Optional<Inventory> optionalPreviousInventory = Optional.ofNullable(vitalInventory.getPreviousInventory());

            if (optionalClickedInventory.isEmpty() && optionalPreviousInventory.isPresent()) {
                final Inventory previousInventory = optionalPreviousInventory.get();

                player.openInventory(previousInventory);

                return;
            }

            final Optional<ItemStack> optionalCurrentItem = Optional.ofNullable(e.getCurrentItem());

            if (optionalCurrentItem.isEmpty()) {
                return;
            }

            vitalInventory.handleClick(e);
            e.setCancelled(true);
        }
    }

    /**
     * Handles the event when a player closes an inventory.
     *
     * @param e The InventoryCloseEvent.
     */
    @EventHandler
    public void onPlayerCloseInventory(@NonNull InventoryCloseEvent e) {
        final InventoryHolder inventoryHolder = e.getInventory().getHolder();
        final Player player = (Player) e.getPlayer();

        if (inventoryHolder instanceof VitalInventory vitalInventory) {
            vitalInventory.onClose(player);
        }
    }
}