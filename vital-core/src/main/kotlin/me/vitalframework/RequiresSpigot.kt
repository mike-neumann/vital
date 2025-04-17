package me.vitalframework

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass

/**
 * Specifies that a bean shall only be initialized if [org.bukkit.plugin.java.JavaPlugin] is found on the classpath
 */
@ConditionalOnClass(name = ["org.bukkit.plugin.java.JavaPlugin"])
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresSpigot 