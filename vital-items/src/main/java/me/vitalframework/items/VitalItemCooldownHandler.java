package me.vitalframework.items;

import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import me.vitalframework.tasks.annotation.VitalRepeatableTaskInfo;
import me.vitalframework.tasks.VitalRepeatableTask;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * A class responsible for managing cooldowns of VitalItemStack items.
 * This class extends VitalRepeatableTask to periodically reduce cooldowns.
 *
 * @author xRa1ny
 */
@Component
@VitalRepeatableTaskInfo(interval = 50)
public class VitalItemCooldownHandler extends VitalRepeatableTask.Spigot {
    private final VitalItemManager itemManager;

    /**
     * Creates a new VitalItemStackCooldownHandler.
     *
     * @param javaPlugin  The JavaPlugin instance.
     * @param itemManager Vital's item manager.
     */
    public VitalItemCooldownHandler(@NonNull JavaPlugin javaPlugin, VitalItemManager itemManager) {
        super(javaPlugin);

        this.itemManager = itemManager;
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
        for (VitalItem vitalItem : itemManager.getItems()) {
            // Reduce Cooldown
            for (Map.Entry<Player, Integer> entry : vitalItem.getPlayerCooldownMap().entrySet()) {
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