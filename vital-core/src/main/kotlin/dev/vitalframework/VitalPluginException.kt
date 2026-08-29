package dev.vitalframework

/**
 * Internal exception; thrown when an error occurrs during [VitalPlugin] lifecycles.
 */
abstract class VitalPluginException(
    message: String,
) : RuntimeException(message) {
    /**
     * Internal exception; thrown when no main class was found while enabling the plugin.
     */
    class NoMainPluginClass(
        mainClass: Class<*>,
        pluginLoaderClass: Class<*>,
    ) : VitalPluginException(
            "Error while enabling Vital plugin '${mainClass.name}' with loader '${pluginLoaderClass.name}', main class is not annotated with '@VitalPlugin.Info'.",
        )

    /**
     * Internal exception; thrown when multiple classes annotated with '@VitalPlugin.Info' while enabling the plugin.
     */
    class MultipleMainPluginClasses(
        mainClass: Class<*>,
        pluginLoaderClass: Class<*>,
    ) : VitalPluginException(
            "Error while enabling Vital plugin '${mainClass.name}' with loader '${pluginLoaderClass.name}', multiple main classes annotated with '@VitalPlugin.Info' found.",
        )

    /**
     * Internal exception; thrown when the detected main class is not a valid subclass of [VitalPlugin].
     */
    class InvalidMainPluginClassType(
        mainClass: Class<*>,
        pluginLoaderClass: Class<*>,
    ) : VitalPluginException(
            "Error while enabling Vital plugin '${mainClass.name}' with loader '${pluginLoaderClass.name}', main class is not a valid subclass of 'VitalPlugin'.",
        )
}
