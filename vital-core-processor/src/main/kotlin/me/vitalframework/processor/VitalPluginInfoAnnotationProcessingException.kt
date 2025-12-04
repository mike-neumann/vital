package me.vitalframework.processor

abstract class VitalPluginInfoAnnotationProcessingException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause) {
    class NoMainClass :
        VitalPluginInfoAnnotationProcessingException("No main plugin class found! Main plugin class must be annotated with '@Vital.Info'.")

    class MultipleMainClasses(vararg classNames: String) :
        VitalPluginInfoAnnotationProcessingException("Multiple main plugin classes found: '[${classNames.joinToString()}]' only one of them must be annotated with '@Vital.Info'. " +
                "Easiest fix: Remove all other '@Vital.Info' annotations from your classes, so only one class with it exists.")

    class GeneratePluginYml(
        cause: Throwable,
    ) : VitalPluginInfoAnnotationProcessingException(
            "Error while generating plugin yml, if this error persists, please open an issue on Vital's GitHub page",
            cause,
        )

    class GeneratePluginConfigurationClass(
        cause: Throwable,
    ) : VitalPluginInfoAnnotationProcessingException("Error while generating PluginConfiguration class", cause)
}
