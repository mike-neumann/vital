package me.vitalframework.commands

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass

/**
 * Annotation to indicate that a class or function requires the presence of the
 * VitalCommandsSubModule class on the classpath to be processed or executed.
 *
 * This annotation serves as a conditional mechanism, ensuring that the annotated
 * component is only loaded or operated when the VitalCommandsSubModule is available
 * in the runtime environment.
 *
 * The annotation can be applied at the class or function level.
 */
@ConditionalOnClass(name = ["me.vitalframework.commands.VitalCommandsSubModule"])
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresVitalCommands
