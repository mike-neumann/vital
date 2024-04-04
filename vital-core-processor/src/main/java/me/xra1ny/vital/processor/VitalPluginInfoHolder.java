package me.xra1ny.vital.processor;

/**
 * Generic Interface responsible for holding the contents for automatic `plugin.yml` creation between multiple Annotation Processor modules.
 *
 * @author xRa1ny
 */
public interface VitalPluginInfoHolder {
    /**
     * Cache variable storing information for later plugin.yml generation during compile-time.
     */
    StringBuilder PLUGIN_INFO = new StringBuilder();
}