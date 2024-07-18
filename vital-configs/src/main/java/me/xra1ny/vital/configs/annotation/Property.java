package me.xra1ny.vital.configs.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a field within a config extending class to be a key.
 *
 * @author xRa1ny
 * @apiNote Not to be confused with .properties files, this annotation is also used for every other config type supported by vital.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Property {
    /**
     * Defines the class types this annotated field manages.
     *
     * @return The classes this annotated field manages.
     * @apiNote When annotating a list or map, specify their generic types.
     */
    Class<?>[] value();
}