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
     * The namespace used for all Vital plugin-related keys.
     */
    String NAMESPACE = "vital";

    /**
     * A namespaced key for storing a unique identifier on ItemStacks.
     */
    NamespacedKey ITEM_UUID = new NamespacedKey(NAMESPACE, "item-uuid");
}

