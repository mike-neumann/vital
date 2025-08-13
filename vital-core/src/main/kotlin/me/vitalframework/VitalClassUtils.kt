package me.vitalframework

object VitalClassUtils {
    /**
     * Retrieves the required annotation of a specified type from the current object's class.
     *
     * This function checks whether the current object's class is annotated with the specified annotation.
     * If the annotation is not present, an exception is thrown.
     *
     * @param T The type of annotation to retrieve. Must extend [Annotation].
     * @return The annotation of type [T] if it exists on the object's class.
     * @throws RuntimeException If the object's class is not annotated with the specified annotation type.
     */
    inline fun <reified T : Annotation> Any.getRequiredAnnotation() =
        javaClass.getAnnotation(T::class.java)
            ?: throw RuntimeException("${javaClass.simpleName} must be annotated with '@${T::class.java.name}'")

    /**
     * Retrieves all annotations of a specified type applied to the current class.
     * Throws a RuntimeException if no annotations of the specified type are found.
     *
     * @return A list of annotations of type [T] present on the current class.
     * @throws RuntimeException if no annotations of type [T] are found on the class.
     */
    inline fun <reified T : Annotation> Any.getRequiredAnnotations(): List<T> =
        javaClass.getAnnotationsByType(T::class.java).toList().also {
            if (it.isEmpty()) throw RuntimeException("${javaClass.getSimpleName()} must be annotated with '@${T::class.java.name}'")
        }
}
