package me.xra1ny.vital;

import lombok.Getter;
import lombok.NonNull;

/**
 * Defines different plugin environments needed for automatic plugin ".yml" creation with command processing.
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
    @NonNull
    private final String ymlFileName;

    VitalPluginEnvironment(@NonNull String ymlFileName) {
        this.ymlFileName = ymlFileName;
    }
}