package me.vitalframework.cloudnet4.bridge

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass

/**
 * Annotation to indicate that a specific class or function requires the presence of the
 * `CloudNet4BridgeSubModule` within the application context.
 *
 * This annotation should be used when certain components rely on the integration provided
 * by the `CloudNet4BridgeSubModule`. The component or functionality annotated with this
 * will only be loaded or executed if the `CloudNet4BridgeSubModule` class is available on
 * the classpath.
 *
 * The presence of this annotation ensures that any dependencies related to the
 * `CloudNet4BridgeSubModule` are already initialized and accessible within the context,
 * preventing potential issues caused by missing integrations.
 *
 * Targets: Can be applied to classes or functions requiring CloudNet4Bridge integration.
 */
@ConditionalOnClass(name = ["me.vitalframework.cloudnet4.bridge.CloudNet4BridgeSubModule"])
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresVitalCloudNet4Bridge
