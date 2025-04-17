package me.vitalframework

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass

/**
 * Specifies that a bean shall only be initialized if [net.md_5.bungee.api.plugin.Plugin] is found on the classpath
 */
@ConditionalOnClass(name = ["net.md_5.bungee.api.plugin.Plugin"])
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresBungee