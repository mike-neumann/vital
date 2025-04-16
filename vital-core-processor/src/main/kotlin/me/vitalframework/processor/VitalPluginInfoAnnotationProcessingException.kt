package me.vitalframework.processor

abstract class VitalPluginInfoAnnotationProcessingException(message: String, cause: Throwable? = null) : RuntimeException(message, cause) {
    class NoMainClass :
        VitalPluginInfoAnnotationProcessingException("No Main Plugin Class Found! Main Plugin Class Must Be Annotated With '@Vital.Info'")

    class GeneratePluginYml(cause: Throwable) : VitalPluginInfoAnnotationProcessingException(
        "error while generating plugin yml, if this error persists, please open an issue on vital's github page",
        cause
    )

    class GeneratePluginConfigurationClass(cause: Throwable) :
        VitalPluginInfoAnnotationProcessingException("Error while generating PluginConfiguration class", cause)
}