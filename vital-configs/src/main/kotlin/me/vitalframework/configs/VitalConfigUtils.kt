package me.vitalframework.configs

import me.vitalframework.configs.VitalConfig.Property
import java.lang.reflect.Field

/**
 * Utility object that provides methods for configuration-related tasks such as reading, injecting,
 * and filtering fields of a class, with support for fields annotated with custom annotations.
 */
object VitalConfigUtils {
    /**
     * Reads the value of the given field from the provided accessor object.
     *
     * @param accessor The instance of the object from which the field value is to be read.
     * @param field The field to be accessed and read.
     * @return The value of the field, or null if the field is not set or accessible.
     * @throws VitalConfigException.ReadField If an exception occurs while attempting to read the field.
     */
    fun readField(
        accessor: Any,
        field: Field,
    ): Any? =
        try {
            field.isAccessible = true
            field[accessor]
        } catch (e: Exception) {
            throw VitalConfigException.ReadField(field, e)
        }

    /**
     * Injects a value into the specified field of the given object.
     * The method forcibly sets the field accessible, even if it is private,
     * to allow value injection. If an error occurs, a `VitalConfigException.InjectField` is thrown.
     *
     * @param accessor The object containing the field to inject into.
     * @param field The field that will be injected with the given value.
     * @param value The value to inject into the specified field.
     * @throws VitalConfigException.InjectField if the injection fails.
     */
    fun injectField(
        accessor: Any,
        field: Field,
        value: Any?,
    ) = try {
        // force field to be accessible even if private
        // this is needed for injection...
        field.isAccessible = true
        field[accessor] = value
    } catch (e: Exception) {
        throw VitalConfigException.InjectField(field, value, e)
    }

    /**
     * Retrieves all fields from the specified class that are annotated with the [Property] annotation.
     *
     * @param clazz The class from which the annotated fields should be retrieved.
     * @return A list of fields that are annotated with the [Property] annotation.
     */
    fun getPropertyFieldsFromType(clazz: Class<*>) = clazz.declaredFields.filter { it.isAnnotationPresent(Property::class.java) }

    /**
     * Filters and retrieves the declared fields of a class that are not annotated with the `@Property` annotation.
     *
     * This function examines all fields declared in the specified class and excludes any fields
     * annotated with the `@Property` annotation. It returns a list of fields that do not have this annotation.
     *
     * @param clazz The `Class` object of the type from which the non-property fields are to be retrieved.
     *              It represents*/
    fun getNonPropertyFieldsFromType(clazz: Class<*>) = clazz.declaredFields.filter { !it.isAnnotationPresent(Property::class.java) }

    /**
     * Retrieves the first field from a class that matches the specified property name.
     * Searches through the fields filtered by a custom criterion defined in `getPropertyFieldsFromType`.
     *
     * @param clazz The target class from which fields are to be retrieved.
     * @param property The name of the property to match against field names.
     * @return The first matching field if found, or null otherwise.
     */
    fun getFieldByProperty(
        clazz: Class<*>,
        property: String,
    ) = getPropertyFieldsFromType(clazz).firstOrNull { it.name == property }
}
