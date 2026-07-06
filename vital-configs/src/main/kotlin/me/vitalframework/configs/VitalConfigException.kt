package me.vitalframework.configs

import java.lang.reflect.Field

/**
 * Internal exception; thrown during [VitalConfig] lifecycles.
 */
abstract class VitalConfigException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause) {
    /**
     * Internal exception; thrown when an exception occurs while saving the config via [VitalConfig.save].
     */
    class Save(
        fileName: String,
        cause: Throwable,
    ) : VitalConfigException("Error while saving config '$fileName'", cause)

    /**
     * Internal exception; thrown when an exception occurs while injecting all properties from the config file into the [VitalConfig] instance.
     */
    class InjectFields(
        fileName: String,
        processor: Class<out VitalConfig.Processor<*, *>>,
        cause: Throwable,
    ) : VitalConfigException(
            "Error while injecting fields for config '$fileName' with processor '${processor.simpleName}'",
            cause,
        )

    /**
     * Internal exception; thrown when an exception occurs while injecting a single property from the config file into the [VitalConfig] instance.
     */
    class InjectField(
        field: Field,
        value: Any?,
        cause: Throwable,
    ) : VitalConfigException(
            "Error while injecting field '${field.type.simpleName} ${field.name}' with '$value'",
            cause,
        )

    /**
     * Internal exception; thrown when an exception occurs while instantiating the given [VitalConfig.Info.processor].
     */
    class CreateFileProcessor(
        fileName: String,
        processor: Class<out VitalConfig.Processor<*, *>>,
        cause: Throwable,
    ) : VitalConfigException(
            "Error while creating config file processor '${processor.simpleName}' for config '$fileName'",
            cause,
        )

    /**
     * Internal exception; thrown when an exception occurs while creating the config file.
     */
    class CreateFile(
        fileName: String,
        cause: Throwable,
    ) : VitalConfigException("Error while creating config file '$fileName'", cause)

    /**
     * Internal exception; thrown when an exception occurs while reading the config file.
     */
    class ReadField(
        field: Field,
        cause: Throwable,
    ) : VitalConfigException("Error while reading config field '${field.type.simpleName} ${field.name}'", cause)
}
