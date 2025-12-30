package me.vitalframework.players

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.stereotype.Component

/**
 * Convenience-annotation to mark a class to only be loaded as a bean, when the vital-players submodule is used.
 * If not running with the vital-players submodule, the annotated bean will not be instantiated by spring.
 *
 * Must be used in combination with [Component].
 *
 * ```java
 * @RequiresVitalPlayers
 * @Component
 * public class MyVitalPlayersBean {
 *   // ...
 * }
 * ```
 */
@ConditionalOnClass(name = ["me.vitalframework.players.VitalPlayersSubModule"])
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresVitalPlayers
