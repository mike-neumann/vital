package me.vitalframework.configs.processor;

import lombok.Getter;
import lombok.NonNull;
import me.vitalframework.configs.VitalConfig;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
public class VitalYMLConfigFileProcessor implements VitalConfig.FileProcessor {
    @NonNull
    private final File file;

    @NonNull
    private final Yaml yaml;

    @NonNull
    private final Map<String, Object> data = new HashMap<>();

    public VitalYMLConfigFileProcessor(@NonNull File file) {
        this.file = file;

        final var loaderOptions = new LoaderOptions();

        loaderOptions.setTagInspector(tag -> true);

        final var constructor = new org.yaml.snakeyaml.constructor.Constructor(loaderOptions);
        final var dumperOptions = new DumperOptions();

        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        final var representer = new Representer(dumperOptions);

        yaml = new Yaml(constructor, representer, dumperOptions, loaderOptions);
    }

    private void addTypeDescriptors(@NonNull Class<?> type) {
        final var rootTypeDescription = new TypeDescription(type, "!%s"
                .formatted(type.getSimpleName()));
        final var rootExcludes = getNonPropertyFieldsFromType(type).stream()
                .map(Field::getName)
                .toArray(String[]::new);

        rootTypeDescription.setExcludes(rootExcludes);
        yaml.addTypeDescription(rootTypeDescription);

        for (var field : getPropertyFieldsFromType(type)) {
            final var vitalConfigProperty = field.getAnnotation(VitalConfig.Property.class);
            final var typeDescription = new TypeDescription(field.getType(), "!%s"
                    .formatted(field.getType().getSimpleName()));
            final var excludes = getNonPropertyFieldsFromType(field.getType()).stream()
                    .map(Field::getName)
                    .toArray(String[]::new);

            typeDescription.setExcludes(excludes);
            yaml.addTypeDescription(typeDescription);

            // add type descriptors for annotated property types...
            for (var propertyType : vitalConfigProperty.value()) {
                addTypeDescriptors(propertyType);
            }

            addTypeDescriptors(field.getType());
        }
    }

    @Override
    public Map<String, Object> load(@NonNull Class<?> type) throws Exception {
        data.clear();

        // add type descriptors for complex types...
        addTypeDescriptors(type);

        final Map<String, Object> data = yaml.load(new FileReader(file));

        if (data != null) {
            this.data.putAll(data);
        }

        return this.data;
    }

    @Override
    public Object read(@NonNull String key) {
        return data.get(key);
    }

    @Override
    public Object read(@NonNull String key, @NonNull Object def) {
        return data.getOrDefault(key, def);
    }

    @Override
    public void write(@NonNull Map<String, ?> serializedContentMap) {
        data.putAll(serializedContentMap);
    }

    @Override
    public void write(@NonNull Object object) {

    }

    @Override
    public void write(@NonNull String key, @NonNull Object value) throws Exception {
        data.put(key, serialize(value));
    }

    @Override
    public void save(@NonNull Map<String, ?> serializedContentMap) throws Exception {
        data.putAll(serializedContentMap);
        yaml.dump(data, new FileWriter(file));
    }

    @Override
    public Map<String, Object> serialize(@NonNull Object object) throws Exception {
        final Map<String, Object> stringObjectMap = new HashMap<>();

        getPropertyFieldsFromType(object.getClass()).stream()
                .map(field -> {
                    try {
                        // else use default snakeyaml mapping.
                        // force field to be accessible even if private
                        field.setAccessible(true);

                        return new AbstractMap.SimpleEntry<>(field.getName(), field.get(object));
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException("error while serializing yml config field %s"
                                .formatted(field.getName()));
                    }
                })
                .forEach((entry) -> stringObjectMap.put(entry.getKey(), entry.getValue()));

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