package me.vitalframework.configs

import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.context.annotation.Conditional
import org.springframework.core.type.AnnotatedTypeMetadata
import org.springframework.stereotype.Component
import org.springframework.util.ClassUtils

/**
 * Convenience-condition to mark a class to only be loaded as a bean, when the vital-configs submodule is used.
 * If not running with the vital-configs submodule, the annotated bean will not be instantiated by spring.
 *
 * Must be used in combination with [Conditional] and any [Component] stereotype.
 *
 * ```java
 * @Conditional(RequiresVitalConfigs.class)
 * @Component
 * public class MyVitalConfigsBean {
 *   // ...
 * }
 * ```
 */
class RequiresVitalConfigs : Condition {
    override fun matches(
        context: ConditionContext,
        metadata: AnnotatedTypeMetadata,
    ): Boolean = ClassUtils.isPresent("me.vitalframework.configs.VitalConfigsSubModule", context.classLoader)
}
