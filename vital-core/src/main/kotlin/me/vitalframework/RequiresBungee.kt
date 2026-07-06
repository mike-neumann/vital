package me.vitalframework

import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.context.annotation.Conditional
import org.springframework.core.type.AnnotatedTypeMetadata
import org.springframework.stereotype.Component
import org.springframework.util.ClassUtils

/**
 * Convenience-condition to mark a class to only be loaded as a bean, when running as a BungeeCord plugin.
 * If not running as a BungeeCord plugin, the annotated bean will not be instantiated by Spring.
 *
 * Must be used in combination with [Conditional] and any [Component] stereotype.
 *
 * ```java
 * @Conditional(RequiresBungee.class)
 * @Component
 * public class MyBungeeBean {
 *   // ...
 * }
 * ```
 */
class RequiresBungee : Condition {
    override fun matches(
        context: ConditionContext,
        metadata: AnnotatedTypeMetadata,
    ): Boolean = ClassUtils.isPresent("net.md_5.bungee.api.plugin.Plugin", context.classLoader)
}
