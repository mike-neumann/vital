package me.xra1ny.vital;

import lombok.Getter;
import lombok.NonNull;

public enum VitalPluginEnvironment {
    SPIGOT_PAPER("plugin.yml"),
    BUNGEECORD("bungee.yml");

    @Getter
    @NonNull
    private final String ymlFileName;

    VitalPluginEnvironment(@NonNull String ymlFileName) {
        this.ymlFileName = ymlFileName;
    }
}