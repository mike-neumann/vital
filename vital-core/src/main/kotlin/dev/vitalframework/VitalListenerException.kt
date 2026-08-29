package dev.vitalframework

/**
 * Internal exception; thrown during [VitalListener] lifecycles.
 */
abstract class VitalListenerException(
    message: String,
    cause: Throwable,
) : RuntimeException(message, cause) {
    /**
     * Internal exception; thrown when an exception occurs during listener registration.
     */
    class Register(
        listenerClass: Class<out VitalListener>,
        cause: Throwable,
    ) : VitalListenerException("Error while registering Vital listener class '${listenerClass.simpleName}'.", cause)
}
