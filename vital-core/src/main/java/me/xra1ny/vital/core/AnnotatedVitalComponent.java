package me.xra1ny.vital.core;

import me.xra1ny.vital.core.exception.VitalComponentNotAnnotatedException;

import java.lang.annotation.Annotation;
import java.util.Optional;

/**
 * Interface for Vital components that are associated with annotations.
 * Provides methods for retrieving and validating annotations on components.
 *
 * @param <T> The type of annotation associated with the component.
 * @author xRa1ny
 */
public interface AnnotatedVitalComponent<T extends Annotation> extends VitalComponent {
    /**
     * Retrieves the required annotation associated with the component.
     *
     * @return The required annotation.
     * @throws VitalComponentNotAnnotatedException If the required annotation is not found on the component.
     */
    default T getRequiredAnnotation() {
        final Optional<T> optionalAnnotation = Optional.ofNullable(getClass().getDeclaredAnnotation(requiredAnnotationType()));

        if (optionalAnnotation.isEmpty()) {
            throw new VitalComponentNotAnnotatedException(this, requiredAnnotationType());
        }

        return optionalAnnotation.get();
    }

    /**
     * Specifies the class type of the required annotation for the component.
     *
     * @return The class type of the required annotation.
     */
    Class<T> requiredAnnotationType();
}
