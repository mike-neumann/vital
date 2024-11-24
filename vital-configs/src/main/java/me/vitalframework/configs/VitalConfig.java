package me.vitalframework.configs;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import me.vitalframework.configs.processor.VitalConfigFileProcessor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;

/**
 * @apiNote Must be annotated with {@link Info}.
 */
@Slf4j
public abstract class VitalConfig {
    private VitalConfigFileProcessor vitalConfigFileProcessor;

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

    private void load(@NonNull String fileName, @NonNull Class<? extends VitalConfigFileProcessor> processor) {
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
                    log.info("%s config file created"
                            .formatted(fileName));
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
        Class<? extends VitalConfigFileProcessor> processor();
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
}