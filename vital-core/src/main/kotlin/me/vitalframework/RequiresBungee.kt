package me.vitalframework

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass

@ConditionalOnClass(name = ["net.md_5.bungee.api.plugin.Plugin"])
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresBungee