package me.vitalframework.processor;

import lombok.NonNull;

/**
 * Generic Interface responsible for holding the contents for automatic `plugin.yml` creation between multiple Annotation Processor modules.
 *
 * @author xRa1ny
 */
public interface VitalPluginInfoHolder {
    /**
     * Cache variable storing information for later plugin.yml generation during compile-time.
     */
    @NonNull
    StringBuilder PLUGIN_INFO = new StringBuilder();
}