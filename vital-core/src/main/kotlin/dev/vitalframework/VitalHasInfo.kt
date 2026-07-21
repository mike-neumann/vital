package dev.vitalframework

/**
 * Should be used for classes that support an info annotation to provide metadata.
 * To allow for multiple info annotations along the hierarchy path,
 * this interface exposes a map of all registered info annotations.
 *
 * To retrieve a single info annotation.
 * ```java
 * final var myInfoAnnotation = getInfo(MyInfoAnnotation.class)
 * ```
 */
interface VitalHasInfo {
    /**
     * All info metadata for this class, indexed by their unique [Class].
     * Can be initialized with [VitalCoreModule.getRequiredAnnotation] or [VitalCoreModule.getRequiredAnnotations].
     */
    val info: MutableMap<out Class<out Annotation>, out Annotation>

    /**
     * Gets the info annotation for the given [type].
     * If no info annotation was found, throws [RuntimeException].
     */
    fun <T : Annotation> getInfo(type: Class<T>): T =
        (info[type] as? T) ?: throw RuntimeException("Class ${javaClass.name} has no known info for type ${type.name}.")
}
