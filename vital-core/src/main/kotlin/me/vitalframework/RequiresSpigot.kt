package me.vitalframework

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass

@ConditionalOnClass(name = ["org.bukkit.plugin.java.JavaPlugin"])
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresSpigot 