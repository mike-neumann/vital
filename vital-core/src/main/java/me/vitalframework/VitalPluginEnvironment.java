package me.vitalframework;

import lombok.Getter;
import lombok.NonNull;

/**
 * Defines different plugin environments needed for automatic plugin ".yml" creation
 */
public enum VitalPluginEnvironment {
    /**
     * Needed for spigot plugins.
     */
    SPIGOT_PAPER("plugin.yml"),

    /**
     * Needed for bungeecord plugins.
     */
    BUNGEECORD("bungee.yml");

    @Getter
    private final String ymlFileName;

    VitalPluginEnvironment(@NonNull String ymlFileName) {
        this.ymlFileName = ymlFileName;
    }
}