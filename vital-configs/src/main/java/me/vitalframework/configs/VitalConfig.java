package me.vitalframework.configs;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import me.vitalframework.configs.annotation.VitalConfigInfo;
import me.vitalframework.configs.processor.VitalConfigFileProcessor;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;

/**
 * @apiNote Must be annotated with {@link VitalConfigInfo}.
 */
@Slf4j
public abstract class VitalConfig {
    private VitalConfigFileProcessor vitalConfigFileProcessor;

    public VitalConfig() {
        final var info = Optional.ofNullable(getClass().getAnnotation(VitalConfigInfo.class))
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
                    final Optional<Field> optionalField = Optional.ofNullable(vitalConfigFileProcessor.getFieldByProperty(getClass(), key));

                    optionalField.ifPresent(field -> injectField(VitalConfig.this, field, value));
                });
    }
}