package me.vitalframework.items;

import lombok.NonNull;
import me.vitalframework.VitalListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.springframework.stereotype.Component;

/**
 * Listens for player interactions with custom item stacks and handles their interactions accordingly.
 * This class is responsible for detecting player interactions with custom items and invoking their associated actions.
 */
@Component
public class VitalItemListener extends VitalListener.Spigot {
    @NonNull
    private final VitalItemService vitalItemService;

    public VitalItemListener(@NonNull JavaPlugin javaPlugin, @NonNull VitalItemService vitalItemService) {
        super(javaPlugin);

        this.vitalItemService = vitalItemService;
    }

    /**
     * Listens for player interactions with items and handles them.
     */
    @EventHandler
    public void onPlayerInteract(@NonNull PlayerInteractEvent e) {
        if (e.getItem() == null) {
            // Ignore interactions with empty hands (no item in hand).
            return;
        }

        // Find the custom item stack that matches the player's interaction item.
        vitalItemService.getItems()
                .stream()
                .filter(i -> i.equals(e.getItem()))
                .findFirst()
                .ifPresent(itemStack -> itemStack.handleInteraction(e));
    }
}