package me.vitalframework

/**
 * Internal exception; thrown during [VitalModule] lifecycles.
 */
abstract class VitalModuleException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause) {
    /**
     * Internal exception; thrown when an exception occurs while enable a module.
     */
    class Enable(
        moduleName: String,
        cause: Throwable,
    ) : VitalModuleException("Error while enabling Vital module '$moduleName'.", cause)

    /**
     * Internal exception; thrown when an exception occurs while disabling a module.
     */
    class Disable(
        moduleName: String,
        cause: Throwable,
    ) : VitalModuleException("Error while disabling Vital module '$moduleName'.", cause)
}
