package me.vitalframework.configs.annotation;

import me.vitalframework.configs.VitalConfig;
import me.vitalframework.configs.processor.VitalConfigFileProcessor;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines meta-information for the annotated {@link VitalConfig}.
 *
 * @author xRa1ny
 */
@Component
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface VitalConfigInfo {
    /**
     * Defines the file name for the annotated config.
     *
     * @return The file name
     * @apiNote Includes the file extension (e.g test.yml; test.properties)
     */
    String name();

    /**
     * Defines the file processor used by this config.
     *
     * @return The processor used by this config.
     * @apiNote file processors are used to process config files.
     */
    Class<? extends VitalConfigFileProcessor> processor();
}