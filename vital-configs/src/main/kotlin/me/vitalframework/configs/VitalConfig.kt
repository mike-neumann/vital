package me.vitalframework.configs;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import me.vitalframework.RequiresAnnotation;
import org.jetbrains.annotations.NotNull;
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

@Slf4j
public abstract class VitalConfig implements RequiresAnnotation<VitalConfig.Info> {
    private FileProcessor vitalConfigFileProcessor;

    public VitalConfig() {
        final var info = getRequiredAnnotation();

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

    @Override
    public final @NotNull Class<Info> requiredAnnotationType() {
        return Info.class;
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
     * Defines meta-information for the annotated config.
     */
    @Component
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Info {
        /**
         * Defines the file name (including extension) for the annotated config.
         */
        String name();

        /**
         * Defines the file processor used by this config.
         */
        Class<? extends FileProcessor> processor();
    }

    /**
     * Defines a field within a config extending class to be a key.
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Property {
        /**
         * Defines the class types this annotated field manages.
         * When annotating a list or map, specify their generic types.
         */
        Class<?>[] value();
    }

    /**
     * Describes an object which is capable of processing the contents of a given config file.
     */
    public interface FileProcessor {
        /**
         * Loads the file processed by this processor.
         */
        Map<String, ?> load(@NonNull Class<?> type) throws Exception;

        /**
         * Reads a config value by the specified key.
         */
        Object read(@NonNull String key);

        /**
         * Reads a config value by the specified key, if that value was not found, returns a default value.
         */
        Object read(@NonNull String key, @NonNull Object def);

        /**
         * Writes the given serialized content to this config.
         */
        void write(@NonNull Map<String, ?> serializedContentMap);

        /**
         * Writes the given object to this config.
         */
        void write(@NonNull Object object);

        /**
         * Writes the given value to this config.
         */
        void write(@NonNull String key, @NonNull Object value) throws Exception;

        /**
         * Saves the given serialized content to this config.
         */
        void save(@NonNull Map<String, ?> serializedContentMap) throws Exception;

        /**
         * Serializes the given object for config usage.
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