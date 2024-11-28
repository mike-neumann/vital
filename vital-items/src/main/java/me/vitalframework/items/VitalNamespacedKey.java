package me.vitalframework.items;

import org.bukkit.NamespacedKey;

/**
 * Defines namespaced keys for various purposes within the Vital plugin.
 * This interface centralizes key definitions to avoid conflicts and ensure consistency.
 *
 * @author xRa1ny
 */
public interface VitalNamespacedKey {
    /**
     * A namespaced key for storing a unique identifier on ItemStacks.
     */
    NamespacedKey ITEM_UUID = NamespacedKey.fromString("item-uuid");
}