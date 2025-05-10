package me.vitalframework

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass

/**
 * Indicates that the annotated class or method is dependent on the BungeeCord API.
 * This annotation acts as a conditional for Spring Boot, ensuring that a bean
 * or configuration is only initialized if the BungeeCord `Plugin` class is available
 * on the classpath.
 *
 * Used to dynamically include or exclude beans and configurations in environments
 * where the BungeeCord platform dependency is present.
 */
@ConditionalOnClass(name = ["net.md_5.bungee.api.plugin.Plugin"])
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresBungee