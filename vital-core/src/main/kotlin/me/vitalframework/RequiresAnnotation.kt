package me.vitalframework

@JvmDefaultWithCompatibility
interface RequiresAnnotation<T : Annotation> {
    fun getRequiredAnnotation() = javaClass.getAnnotation(requiredAnnotationType())
        ?: throw RuntimeException("${javaClass.getSimpleName()} must be annotated with '@${requiredAnnotationType().getSimpleName()}'")

    fun requiredAnnotationType(): Class<T>
}