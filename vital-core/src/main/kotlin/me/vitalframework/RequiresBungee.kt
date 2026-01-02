package me.vitalframework

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.stereotype.Component

/**
 * Convenience-annotation to mark a class to only be loaded as a bean, when running as a BungeeCord plugin.
 * If not running as a BungeeCord plugin, the annotated bean will not be instantiated by spring.
 *
 * Must be used in combination with [Component].
 *
 * ```java
 * @RequiresBungee
 * @Component
 * public class MyBungeeBean {
 *   // ...
 * }
 * ```
 */
@ConditionalOnClass(name = ["net.md_5.bungee.api.plugin.Plugin"])
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresBungee
