package me.xra1ny.vital.configs.processor;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.java.Log;
import me.xra1ny.vital.configs.VitalConfig;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;


@Log
public class PropertiesFileProcessor implements FileProcessor {
    @NonNull
    private final File file;

    @Getter
    @NonNull
    private final Properties properties = new Properties();

    public PropertiesFileProcessor(@NonNull File file) {
        this.file = file;
    }

    @Override
    public Map<String, String> load(@NonNull Class<?> type) throws Exception {
        properties.load(new FileReader(file));

        return Map.ofEntries(properties.entrySet().toArray(Map.Entry[]::new));
    }

    @Override
    public String read(@NonNull String key) {
        return properties.getProperty(key);
    }

    @Override
    public Object read(@NonNull String key, @NonNull Object def) {
        return properties.getProperty(key, String.valueOf(def));
    }

    @Override
    public void write(@NonNull Map<String, ?> serializedContentMap) {
        serializedContentMap.forEach(this::write);
    }

    @Override
    public void write(@NonNull Object object) {
        write(serialize(object));
    }

    @Override
    public void write(@NonNull String key, @NonNull Object value) {
        properties.setProperty(key, String.valueOf(value));
    }

    @Override
    public void save(@NonNull Map<String, ?> serializedContentMap) throws Exception {
        serializedContentMap.entrySet().stream()
                .map((entry) -> Map.entry(entry.getKey(), String.valueOf(entry.getValue())))
                .forEach((entry) -> properties.setProperty(entry.getKey(), entry.getValue()));

        properties.store(new FileWriter(file), null);
    }

    @Override
    public Map<String, String> serialize(@NonNull Object object) {
        Map<String, String> stringObjectMap = new HashMap<>();

        getPropertyFieldsFromType(object.getClass()).stream()
                .filter(field -> String.class.isAssignableFrom(field.getType()))
                .map(field -> {
                    try {
                        // else use default snakeyaml mapping.
                        return new AbstractMap.SimpleEntry<>(field.getName(), field.get(object));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        throw new RuntimeException("error while serializing properties config field %s"
                                .formatted(field.getName()));
                    }
                })
                .forEach((entry) -> stringObjectMap.put(entry.getKey(), (String) entry.getValue()));

        return stringObjectMap;
    }

    @Override
    public Object deserialize(@NonNull Map<String, ?> serializedContentMap, @NonNull Class<Object> type) throws Exception {
        try {
            final Constructor<?> defaultConstructor = type.getConstructor();
            final Object object = defaultConstructor.newInstance();

            // default constructor was found, inject field properties...
            serializedContentMap
                    .forEach((key, value) -> {
                        final Optional<Field> optionalField = Optional.ofNullable(getFieldByProperty(type, key));

                        optionalField.ifPresent(field -> VitalConfig.injectField(object, field, value));
                    });

            return object;
        } catch (NoSuchMethodException e) {
            // default constructor not found, attempt to get constructor matching properties...
            final Constructor<?> constructor = type.getConstructor(getPropertyFieldsFromType(type).stream().map(Object::getClass).toArray(Class[]::new));

            // constructor found, create new instance with this constructor...

            return constructor.newInstance(serializedContentMap.values());
        }
    }
}