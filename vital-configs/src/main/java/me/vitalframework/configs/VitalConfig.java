package me.vitalframework.configs;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @apiNote Must be annotated with {@link Info}.
 */
@Slf4j
public abstract class VitalConfig {
    private FileProcessor vitalConfigFileProcessor;

    public VitalConfig() {
        final var info = Optional.ofNullable(getClass().getAnnotation(Info.class))
                .orElseThrow(() -> new RuntimeException("config needs to be annotated with @ConfigInfo!"));

        load(info.name(), info.processor());
    }

    public static void injectField(@NonNull Object accessor, @NonNull Field field, Object value) {
        try {
            // force field to be accessible even if private
            // this is needed for injection...
            field.setAccessible(true);
            field.set(accessor, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException("error while injecting field %s with %s"
                    .formatted(field.getName(), String.valueOf(value)));
        }
    }

    public void save() {
        try {
            vitalConfigFileProcessor.save(vitalConfigFileProcessor.serialize(this));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("error while saving config");
        }
    }

    private void load(@NonNull String fileName, @NonNull Class<? extends FileProcessor> processor) {
        try {
            final var file = createFile(fileName);

            // attempt to create default processor instance.
            final var defaultConstructor = processor.getDeclaredConstructor(File.class);

            vitalConfigFileProcessor = defaultConstructor.newInstance(file);

            try {
                // after everything has worked without problem, inject field of our config with the values now retrievable...
                injectFields(vitalConfigFileProcessor.load(getClass()));
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("error while injecting fields for config %s with processor %s"
                        .formatted(fileName, processor.getSimpleName()));
            }
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException("error while creating config file processor %s for config %s"
                    .formatted(processor.getSimpleName(), fileName));
        }
    }

    @NonNull
    private File createFile(@NonNull String fileName) {
        final var file = new File(fileName);

        if (!file.exists()) {
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }

            try {
                final var fileCreated = file.createNewFile();

                if (fileCreated) {
                    log.info("{} config file created", fileName);
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("error while creating config file %s"
                        .formatted(fileName));
            }
        }

        return file;
    }

    private void injectFields(@NonNull Map<String, ?> serializedContentMap) {
        serializedContentMap
                .forEach((key, value) -> {
                    final var optionalField = Optional.ofNullable(vitalConfigFileProcessor.getFieldByProperty(getClass(), key));

                    optionalField.ifPresent(field -> injectField(VitalConfig.this, field, value));
                });
    }

    /**
     * Defines meta-information for the annotated {@link VitalConfig}.
     *
     * @author xRa1ny
     */
    @Component
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Info {
        /**
         * Defines the file name for the annotated config.
         *
         * @return The file name
         * @apiNote Includes the file extension (e.g test.yml; test.properties)
         */
        String name();

        /**
         * Defines the file processor used by this config.
         *
         * @return The processor used by this config.
         * @apiNote file processors are used to process config files.
         */
        Class<? extends FileProcessor> processor();
    }

    /**
     * Defines a field within a config extending class to be a key.
     *
     * @author xRa1ny
     * @apiNote Not to be confused with .properties files, this annotation is also used for every other config type supported by vital.
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Property {
        /**
         * Defines the class types this annotated field manages.
         *
         * @return The classes this annotated field manages.
         * @apiNote When annotating a list or map, specify their generic types.
         */
        Class<?>[] value();
    }

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
                    .filter(field -> field.isAnnotationPresent(VitalConfig.Property.class))
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
                    .filter(field -> !field.isAnnotationPresent(VitalConfig.Property.class))
                    .toList();
        }

        /**
         * Fetches a field of the given type by its property string.
         *
         * @param type     The type to fetch the field from.
         * @param property The property key of the annotated field.
         * @return The fetched field; or null.
         */

        default Field getFieldByProperty(@NonNull Class<?> type, @NonNull String property) {
            return getPropertyFieldsFromType(type).stream()
                    .filter(field -> field.getName().equals(property))
                    .findFirst()
                    .orElse(null);
        }
    }
}