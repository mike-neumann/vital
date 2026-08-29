package dev.vitalframework.processor

import dev.vitalframework.VitalPlugin
import dev.vitalframework.VitalPluginException

abstract class VitalPluginInfoAnnotationProcessingException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause) {
    class NoMainClass :
        VitalPluginInfoAnnotationProcessingException(
            "No main class found! Main class must be annotated with '@VitalPlugin.Info'.",
        )

    class MultipleMainClasses(
        vararg classNames: String,
    ) : VitalPluginInfoAnnotationProcessingException(
            "Multiple main classes found: '[${classNames.joinToString()}]' only one of them must be annotated with '@VitalPlugin.Info'. " +
                "Easiest fix: Remove all other '@VitalPlugin.Info' annotations from your classes, so only one class with it exists.",
        )

    class GeneratePluginYml(
        cause: Throwable,
    ) : VitalPluginInfoAnnotationProcessingException(
            "Error while generating plugin yml, if this error persists, please open an issue on Vital's GitHub page",
            cause,
        )

    class GeneratePluginConfigurationClass(
        cause: Throwable,
    ) : VitalPluginInfoAnnotationProcessingException("Error while generating PluginConfiguration class", cause)

    /**
     * Internal exception; thrown when the detected main class is not a valid subclass of [VitalPlugin].
     */
    class InvalidMainPluginClassType(
        mainClassName: String,
    ) : VitalPluginException("Error while processing main class '$mainClassName', class is not a valid subtype of 'VitalPlugin'.")
}
