package me.vitalframework.configs

import java.lang.reflect.Field

abstract class VitalConfigException(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause) {
    class Save(fileName: String, cause: Throwable) : VitalConfigException(
        "error while saving config '${fileName}'",
        cause
    )

    class InjectFields(
        fileName: String,
        processor: Class<out VitalConfig.Processor<*, *>>,
        cause: Throwable,
    ) :
        VitalConfigException(
            "error while injecting fields for config '$fileName' with processor '${processor.simpleName}'",
            cause
        )

    class InjectField(field: Field, value: Any?, cause: Throwable) : VitalConfigException(
        "error while injecting field '${field.type.simpleName} ${field.name}' with '$value'",
        cause
    )

    class CreateFileProcessor(
        fileName: String,
        processor: Class<out VitalConfig.Processor<*, *>>,
        cause: Throwable,
    ) :
        VitalConfigException(
            "error while creating config file processor '${processor.simpleName}' for config '$fileName'",
            cause
        )

    class CreateFile(fileName: String, cause: Throwable) : VitalConfigException(
        "error while creating config file '$fileName'",
        cause
    )

    class ReadField(field: Field, cause: Throwable) : VitalConfigException(
        "error while reading config field '${field.type.simpleName} ${field.name}'",
        cause
    )
}