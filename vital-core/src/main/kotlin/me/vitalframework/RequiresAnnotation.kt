package me.vitalframework

/**
 * Interface to retrieve the required class level annotation of the applied class.
 */
@JvmDefaultWithCompatibility
interface RequiresAnnotation<T : Annotation> {
    fun getRequiredAnnotation(): T = javaClass.getAnnotation(requiredAnnotationType())
        ?: throw RuntimeException("${javaClass.getSimpleName()} must be annotated with '@${requiredAnnotationType().getSimpleName()}'")

    /**
     * Specifies the class type of the required annotation.
     */
    fun requiredAnnotationType(): Class<T>
}