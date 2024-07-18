package me.xra1ny.vital.configs.annotation;

import me.xra1ny.vital.configs.processor.FileProcessor;
import me.xra1ny.vital.configs.VitalConfig;
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
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
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
    Class<? extends FileProcessor> processor();
}