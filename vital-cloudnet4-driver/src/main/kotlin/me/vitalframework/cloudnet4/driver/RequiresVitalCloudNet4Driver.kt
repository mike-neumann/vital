package me.vitalframework.cloudnet4.driver

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass

/**
 * Annotation indicating that the annotated class or function requires the presence
 * of the `CloudNet4DriverSubModule` within the application context.
 *
 * The `CloudNet4DriverSubModule` is a Vital submodule that integrates with the CloudNet 4 driver,
 * providing functionalities for managing and interacting with the CloudNet infrastructure.
 *
 * This annotation can be used to conditionally enable components or features based on the presence
 * of the CloudNet 4 driver submodule within the environment.
 *
 * The annotation is typically applied at the class or function level and contributes
 * to the configuration and functionality of the application.
 */
@ConditionalOnClass(name = ["me.vitalframework.cloudnet4.driver.VitalCloudNet4DriverSubModule"])
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresVitalCloudNet4Driver
