package me.vitalframework

object VitalClassUtils {
    inline fun <reified T : Annotation> Any.getRequiredAnnotation() = javaClass.getAnnotation(T::class.java)
        ?: throw RuntimeException("${javaClass.simpleName} must be annotated with '@${T::class.java.name}'")

    inline fun <reified T : Annotation> Any.getRequiredAnnotations(): List<T> =
        javaClass.getAnnotationsByType(T::class.java).toList().also {
            if (it.isEmpty()) throw RuntimeException("${javaClass.getSimpleName()} must be annotated with '@${T::class.java.name}'")
        }
}