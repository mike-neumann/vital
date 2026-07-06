package me.vitalframework

import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.context.annotation.Conditional
import org.springframework.core.type.AnnotatedTypeMetadata
import org.springframework.stereotype.Component
import org.springframework.util.ClassUtils

/**
 * Convenience-condition to mark a class to only be loaded as a bean, when the vital-scoreboards module is used.
 * If not running with the vital-scoreboards module, the annotated bean will not be instantiated by Spring.
 *
 * Must be used in combination with [Conditional] and any [Component] stereotype.
 *
 * ```java
 * @Conditional(RequiresVitalScoreboards.class)
 * @Component
 * public class MyVitalScoreboardsBean {
 *   // ...
 * }
 * ```
 */
class RequiresVitalScoreboards : Condition {
    override fun matches(
        context: ConditionContext,
        metadata: AnnotatedTypeMetadata,
    ): Boolean = ClassUtils.isPresent("me.vitalframework.scoreboards.VitalScoreboardsModule", context.classLoader)
}
