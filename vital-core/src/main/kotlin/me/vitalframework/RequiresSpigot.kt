package me.vitalframework

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass

/**
 * Indicates that the annotated class, function, or property is dependent on the Spigot API.
 *
 * This annotation is used as a conditional for Spring Boot, ensuring that beans, configurations,
 * or methods are only processed if the Spigot `JavaPlugin` class is available on the classpath.
 *
 * Commonly applied to components or configurations that are specifically meant to function
 * within a Spigot server environment.
 */
@ConditionalOnClass(name = ["org.bukkit.plugin.java.JavaPlugin"])
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresSpigot 