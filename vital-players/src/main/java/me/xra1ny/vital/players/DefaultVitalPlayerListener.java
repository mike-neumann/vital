package me.xra1ny.vital.players;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class DefaultVitalPlayerListener extends VitalPlayerListener<VitalPlayer> {
    /**
     * Creates a new instance of VitalPlayerListener.
     *
     * @param javaPlugin            The JavaPlugin instance associated with the listener.
     * @param vitalPlayerManager The VitalUserManagement instance to manage VitalPlayer components.
     * @param vitalPlayerTimeout
     */
    public DefaultVitalPlayerListener(@NotNull JavaPlugin javaPlugin, @NotNull VitalPlayerManager<VitalPlayer> vitalPlayerManager, int vitalPlayerTimeout) {
        super(javaPlugin, vitalPlayerManager, vitalPlayerTimeout);
    }

    @Override
    protected Class<VitalPlayer> vitalPlayerType() {
        return VitalPlayer.class;
    }
}
