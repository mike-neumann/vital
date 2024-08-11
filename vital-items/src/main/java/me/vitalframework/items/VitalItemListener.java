package me.vitalframework.items;

import lombok.NonNull;
import me.vitalframework.VitalListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.springframework.stereotype.Component;

/**
 * Listens for player interactions with custom item stacks and handles their interactions accordingly.
 * This class is responsible for detecting player interactions with custom items and invoking their associated actions.
 *
 * @author xRa1ny
 */
@Component
public class VitalItemListener extends VitalListener.Spigot {
    /**
     * The management system for custom item stacks.
     */
    private final VitalItemManager vitalItemManager;

    /**
     * Constructs a new instance of the VitalItemStackListener.
     *
     * @param vitalItemManager The management system for custom item stacks.
     */
    public VitalItemListener(@NonNull VitalItemManager vitalItemManager) {
        this.vitalItemManager = vitalItemManager;
    }

    /**
     * Listens for player interactions with items and handles them.
     *
     * @param e The PlayerInteractEvent triggered by the player's interaction.
     */
    @EventHandler
    public void onPlayerInteract(@NonNull PlayerInteractEvent e) {
        if (e.getItem() == null) {
            // Ignore interactions with empty hands (no item in hand).
            return;
        }

        // Find the custom item stack that matches the player's interaction item.
        vitalItemManager.getItems()
                .stream()
                .filter(i -> i.equals(e.getItem()))
                .findFirst()
                .ifPresent(itemStack -> itemStack.handleInteraction(e));
    }
}