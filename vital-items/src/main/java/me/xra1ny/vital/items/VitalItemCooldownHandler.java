package me.xra1ny.vital.items;

import lombok.NonNull;
import me.xra1ny.essentia.inject.annotation.Component;
import me.xra1ny.vital.tasks.VitalRepeatableTask;
import me.xra1ny.vital.tasks.annotation.VitalRepeatableTaskInfo;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

/**
 * A class responsible for managing cooldowns of VitalItemStack items.
 * This class extends VitalRepeatableTask to periodically reduce cooldowns.
 *
 * @author xRa1ny
 */
@Component
@VitalRepeatableTaskInfo(value = 50)
public class VitalItemCooldownHandler extends VitalRepeatableTask.Spigot {
    private final VitalItemManager itemManager;

    /**
     * Creates a new VitalItemStackCooldownHandler.
     *
     * @param javaPlugin The JavaPlugin instance.
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

    /**
     * Called when this component is registered.
     * Starts the cooldown handler task.
     */
    @Override
    public void onRegistered() {
        start();
    }

    /**
     * Called when this component is unregistered.
     * Stops the cooldown handler task.
     */
    @Override
    public void onUnregistered() {
        stop();
    }

    /**
     * Specifies the required annotation type for this component.
     *
     * @return The annotation type required for this component.
     */
    @Override
    public Class<VitalRepeatableTaskInfo> requiredAnnotationType() {
        return VitalRepeatableTaskInfo.class;
    }
}