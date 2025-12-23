package me.vitalframework

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.stereotype.Component

/**
 * Convenience-annotation to mark a class to only be loaded as a bean, when running as a Spigot plugin.
 * If not running as a Spigot plugin, the annotated bean will not be instantiated by spring.
 *
 * Must be used in combination with [Component].
 *
 * ```java
 * @RequiresSpigot
 * @Component
 * public class MySpigotBean {
 *   // ...
 * }
 * ```
 */
@ConditionalOnClass(name = ["org.bukkit.plugin.java.JavaPlugin"])
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresSpigot
