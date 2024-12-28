package me.vitalframework.processor

/**
 * Generic Interface responsible for holding the contents for automatic `plugin.yml` creation between multiple Annotation Processor modules.
 */
object VitalPluginInfoHolder {
    /**
     * Cache variable storing information for later plugin.yml generation during compile-time.
     */
    @JvmField
    val PLUGIN_INFO: StringBuilder = StringBuilder()
}