package me.xra1ny.vital.annotation;

import me.xra1ny.vital.VitalPluginEnvironment;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies metadata for the plugin's main class.
 *
 * @author xRa1ny
 * @apiNote If combined with vital-core-processor dependency as annotation processor, can automatically generate the plugin.yml on compile-time.
 */
@Component
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface VitalPluginInfo {
    /**
     * Defines the name of this plugin.
     *
     * @return The name of this plugin.
     */
    String name();

    /**
     * Defines the description of this plugin.
     *
     * @return The description of this plugin.
     */
    String description() default "A Vital Plugin";

    /**
     * Defines the api version this plugin uses.
     *
     * @return The api version this plugin uses.
     * @apiNote api-version may be identical to server environment version.
     */
    String apiVersion() default "1.20";

    /**
     * Defines the version of this plugin.
     *
     * @return The version of this plugin.
     */
    String version() default "1.0";

    /**
     * The author/s of this plugin.
     *
     * @return The author/s of this plugin.
     */

    String authors() default "";

    /**
     * Defines this vital plugin instance environment for automatic plugin yml generation.
     *
     * @return The environment used for this vital plugin instance.
     */
    VitalPluginEnvironment environment();

    /**
     * Defines the locations where spring should look for configuration files
     *
     * @return The locations of any configuration files for spring
     */
    String[] springConfigLocations() default "classpath:application.properties";
}