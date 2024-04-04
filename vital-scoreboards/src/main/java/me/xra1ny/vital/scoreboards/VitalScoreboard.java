package me.xra1ny.vital.scoreboards;

import me.xra1ny.vital.VitalComponent;

/**
 * An abstract base class for representing scoreboards in the Vital plugin framework.
 * Subclasses should implement their own custom scoreboard logic and interactions.
 * This class implements the {@link VitalComponent} interface and provides empty
 * implementations for the registration and unregistration methods.
 *
 * @author xRa1ny
 */
abstract class VitalScoreboard implements VitalComponent {
    @Override
    public final void onRegistered() {

    }

    @Override
    public final void onUnregistered() {

    }
}