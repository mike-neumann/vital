package me.vitalframework.items;

import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import me.vitalframework.tasks.VitalRepeatableTask;
import org.bukkit.plugin.java.JavaPlugin;
import org.springframework.stereotype.Component;

/**
 * A class responsible for managing cooldowns of VitalItemStack items.
 * This class extends VitalRepeatableTask to periodically reduce cooldowns.
 *
 * @author xRa1ny
 */
@Component
@VitalRepeatableTask.Info(interval = 50)
public class VitalItemCooldownHandler extends VitalRepeatableTask.Spigot {
    private final VitalItemService itemService;

    public VitalItemCooldownHandler(@NonNull JavaPlugin javaPlugin, VitalItemService itemService) {
        super(javaPlugin);

        this.itemService = itemService;
    }

    /**
     * Called when the task starts.
     */
    @Override
    public void onStart() {
        // Initialization or actions when the task starts.
    }

    /**
     * Called when the task stops.
     */
    @Override
    public void onStop() {
        // Cleanup or actions when the task stops.
    }

    /**
     * Called on each tick of the repeatable task.
     * Reduces cooldowns for registered VitalItemStack items.
     */
    @Override
    public void onTick() {
        for (var vitalItem : itemService.getItems()) {
            // Reduce Cooldown
            for (var entry : vitalItem.getPlayerCooldownMap().entrySet()) {
                if (entry.getValue() <= 0) {
                    continue;
                }

                vitalItem.getPlayerCooldownMap().put(entry.getKey(), entry.getValue() - 50);

                if (vitalItem.equals(entry.getKey().getInventory().getItemInMainHand())) {
                    vitalItem.onCooldownTick(entry.getKey());
                }

                if (vitalItem.getCooldown(entry.getKey()) <= 0) {
                    // cooldown has expired, call on expired.
                    vitalItem.onCooldownExpire(entry.getKey());
                }
            }
        }
    }

    @PostConstruct
    public void init() {
        start();
    }
}