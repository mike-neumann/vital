package me.vitalframework.tasks

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass

/**
 * An annotation used to conditionally enable a class or function only if the specified class
 * `me.vitalframework.tasks.VitalTasksSubModule` is present on the classpath. This can be used
 * to control the activation of components or beans based on the availability of the VitalTasks
 * submodule in a project.
 *
 * This annotation is typically applied to classes or functions to conditionally load
 * them into an application context when the required module or functionality exists.
 */
@ConditionalOnClass(name = ["me.vitalframework.tasks.VitalTasksSubModule"])
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresVitalTasks
