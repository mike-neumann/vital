package me.xra1ny.vital;

import java.lang.annotation.Annotation;
import java.util.Optional;

/**
 * Interface for Vital components that are associated with annotations.
 * Provides methods for retrieving and validating annotations on components.
 *
 * @param <T> The type of annotation associated with the component.
 * @author xRa1ny
 */
public interface RequiresAnnotation<T extends Annotation> {
    /**
     * Retrieves the required annotation associated with the component.
     *
     * @return The required annotation.
     * @throws RuntimeException If the required annotation is not found on the component.
     */
    default T getRequiredAnnotation() {
        return Optional.ofNullable(getClass().getAnnotation(requiredAnnotationType()))
                .orElseThrow(() -> new RuntimeException("%s must be annotated with @%s"
                        .formatted(getClass().getSimpleName(), requiredAnnotationType().getSimpleName())));
    }

    /**
     * Specifies the class type of the required annotation for the component.
     *
     * @return The class type of the required annotation.
     */
    Class<T> requiredAnnotationType();
}