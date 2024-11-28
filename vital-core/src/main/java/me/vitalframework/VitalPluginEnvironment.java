package me.vitalframework;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Defines different plugin environments needed for automatic plugin ".yml" creation
 */
@Getter
@RequiredArgsConstructor
public enum VitalPluginEnvironment {
    /**
     * Needed for spigot plugins.
     */
    SPIGOT_PAPER("plugin.yml"),

    /**
     * Needed for bungeecord plugins.
     */
    BUNGEECORD("bungee.yml");

    @NonNull
    private final String ymlFileName;
}