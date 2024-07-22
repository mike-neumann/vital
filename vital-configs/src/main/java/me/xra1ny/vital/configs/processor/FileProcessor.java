package me.xra1ny.vital.configs.processor;

import jakarta.annotation.Nullable;
import lombok.NonNull;
import me.xra1ny.vital.configs.VitalConfig;
import me.xra1ny.vital.configs.annotation.Property;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Describes an object which is capable of processing the contents of a given config file.
 *
 * @author xRa1ny
 */
public interface FileProcessor {
    /**
     * Loads the file processed by this processor.
     *
     * @param type The {@link VitalConfig} subtype of this config file.
     * @return The serialized content of this config.
     * @throws Exception If any error occurs while loading the config.
     */
    Map<String, ?> load(@NonNull Class<?> type) throws Exception;

    /**
     * Reads a config value by the specified key.
     *
     * @param key The key.
     * @return The value the given config key holds.
     */
    Object read(@NonNull String key);

    /**
     * Reads a config value by the specified key, if that value was not found, returns a default value.
     *
     * @param key The key.
     * @param def The default value.
     * @return The value of the given config key.
     */
    Object read(@NonNull String key, @NonNull Object def);

    /**
     * Writes the given serialized content to this config.
     *
     * @param serializedContentMap The serialized content to write to this config.
     */
    void write(@NonNull Map<String, ?> serializedContentMap);

    /**
     * Writes the given object to this config.
     *
     * @param object The object.
     */
    void write(@NonNull Object object);

    /**
     * Writes the given value to this config.
     *
     * @param key   The key of this value.
     * @param value The value.
     * @throws Exception If any error occurs while writing the value.
     */
    void write(@NonNull String key, @NonNull Object value) throws Exception;

    /**
     * Saves the given serialized content to this config.
     *
     * @param serializedContentMap The serialized content.
     * @throws Exception If any error occurs while saving the content.
     */
    void save(@NonNull Map<String, ?> serializedContentMap) throws Exception;

    /**
     * Serializes the given object for config usage.
     *
     * @param object The object to serialize.
     * @return The serialized object.
     * @throws Exception If any error occurs while serialization.
     */
    Map<String, ?> serialize(@NonNull Object object) throws Exception;

    /**
     * Deserializes the given serialized map to the specified object type.
     *
     * @param serializedContentMap The serialized content.
     * @param type                 The type to deserialize to.
     * @return The deserialized object.
     * @throws Exception If any error occurs while deserializing.
     */
    Object deserialize(@NonNull Map<String, ?> serializedContentMap, @NonNull Class<Object> type) throws Exception;

    /**
     * Gets all property fields of the given type.
     *
     * @param type The type to fetch all property fields from.
     * @return All property fields of the given type.
     */
    @NonNull
    default List<Field> getPropertyFieldsFromType(@NonNull Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Property.class))
                .toList();
    }

    /**
     * Gets all non property fields of the given type.
     *
     * @param type The type to fetch all non property fields from.
     * @return All non property fields of the given type.
     */
    @NonNull
    default List<Field> getNonPropertyFieldsFromType(@NonNull Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
                .filter(field -> !field.isAnnotationPresent(Property.class))
                .toList();
    }

    /**
     * Fetches a field of the given type by its property string.
     *
     * @param type     The type to fetch the field from.
     * @param property The property key of the annotated field.
     * @return The fetched field; or null.
     */
    @Nullable
    default Field getFieldByProperty(@NonNull Class<?> type, @NonNull String property) {
        return getPropertyFieldsFromType(type).stream()
                .filter(field -> field.getName().equals(property))
                .findFirst()
                .orElse(null);
    }
}