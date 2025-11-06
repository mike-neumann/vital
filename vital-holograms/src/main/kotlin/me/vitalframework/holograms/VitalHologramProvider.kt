package me.vitalframework.holograms

/**
 * Represents a provider interface for Vital to load holograms from when needed.
 * Since Vital doesn't know how the consuming project stores its holograms, a provider must be used to load them.
 *
 * Vital currently only uses hologram providers to correctly display [VitalPerPlayerHologram]'s.
 */
fun interface VitalHologramProvider<T : VitalHologram<*>> {
    fun provide(): List<T>
}
