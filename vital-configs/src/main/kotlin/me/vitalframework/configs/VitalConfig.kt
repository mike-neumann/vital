package me.vitalframework.configs

import me.vitalframework.logger
import org.springframework.stereotype.Component
import java.io.IOException
import java.io.InputStream
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.name
import kotlin.io.path.writeText
import kotlin.reflect.KClass

/**
 * Represents an abstract configuration class that provides functionality for
 * managing configuration files. Implementations of this class are expected to
 * define configurations handled with a specific processor.
 *
 * The class features automatic initialization of configuration fields based
 * on annotations and processes configuration file loading, saving, and injecting
 * field values seamlessly.
 *
 * It uses a processor specified via the `Info` annotation to handle
 * serialization and deserialization of configuration data. The handling of
 * configuration fields is achieved through the `Property` annotation.
 *
 * Main responsibilities:
 * - Loading configuration values from a file or input stream.
 * - Saving current configuration values to a file.
 * - Dynamically injecting field values into the configuration object.
 *
 * Subclasses should annotate their implementation with the `Info` annotation
 * to provide details about the configuration such as its name and the processor
 * used. Fields within the configuration can be annotated with the `Property`
 * annotation to indicate they should be handled as part of the configuration.
 *
 * @constructor Initializes the configuration by loading its content from the
 *     specified file or an empty default if the file doesn't exist. Uses the
 *     processor defined in the `Info` annotation to manage the configuration
 *     file's content. If initialization fails at any point, specific exceptions
 *     are thrown with detailed error descriptions.
 */
abstract class VitalConfig {
    val logger = logger<VitalConfig>()
    val fileName: String
    val processor: Processor<*, Any>

    init {
        val info = getInfo()

        fileName = info.name
        processor =
            try {
                info.processor.java
                    .getDeclaredConstructor()
                    .newInstance()
            } catch (e: Exception) {
                throw VitalConfigException.CreateFileProcessor(info.name, info.processor.java, e)
            }
        val file = Path(fileName)
        val inputStream =
            when {
                file.exists() -> file.inputStream()
                else -> "".byteInputStream()
            }

        try {
            // after everything has worked without any problem, inject fields of our config with the values now retrievable...
            load(inputStream)
        } catch (e: Exception) {
            throw VitalConfigException.InjectFields(info.name, info.processor.java, e)
        }
    }

    /**
     * Saves the configuration to a file if specified.
     *
     * @param writeToFile Indicates whether the configuration should be written to a file. Defaults to `true`.
     * When this parameter is `true`, the method attempts to save the configuration data to a file specified
     * by the `fileName` property. If the file does not exist, it is created along with any necessary
     * parent directories. The operation may throw exceptions if issues occur during file creation
     * or data writing.
     */
    @JvmOverloads
    fun save(writeToFile: Boolean = true) {
        try {
            if (writeToFile) {
                val file = Path(fileName)
                // create the file if it does not exist
                if (!file.exists()) {
                    if (file.parent != null) file.parent.createDirectories()

                    try {
                        file.createFile()
                        logger.debug("${file.name} config file created")
                    } catch (e: IOException) {
                        throw VitalConfigException.CreateFile(file.name, e)
                    }
                }

                file.writeText(processor.save(processor.serialize(this)))
            }
        } catch (e: Exception) {
            throw VitalConfigException.Save(fileName, e)
        }
    }

    /**
     * Loads configuration data from the provided input stream and injects the content into the fields of the current object.
     *
     * The method processes the provided input stream, using the class of the current object as a reference for mapping
     * configuration properties. Fields annotated with the appropriate property annotations are injected with their corresponding
     * values from the configuration content.
     *
     * @param inputStream The input stream containing configuration data to be loaded. Must not be null.
     */
    fun load(inputStream: InputStream) {
        val serializedContent = processor.load(inputStream, javaClass)

        for ((key, value) in serializedContent) {
            VitalConfigUtils.getFieldByProperty(javaClass, key)?.let { VitalConfigUtils.injectField(this, it, value) }
        }
    }

    /**
     * Annotation used to provide metadata for configuration classes.
     *
     * @property name Specifies the name or path of the configuration file associated with the annotated class.
     * This name is typically used when saving or loading the configuration.
     *
     * @property processor Indicates the processor class responsible for managing the configuration format
     * (e.g., YAML, JSON, Properties). The processor class must implement the `Processor` interface
     * and handle the serialization and deserialization of the associated data.
     */
    @Component
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Info(
        val name: String,
        val processor: KClass<out Processor<*, Any>>,
    )

    /**
     * Annotation used to mark fields within a configuration class as properties.
     *
     * The `types` parameter allows specifying one or more `KClass` types that are
     * supported for the annotated field. These types may be used to define
     * compatible data formats, enforce type safety, or enable type-based processing
     * of the annotated fields.
     *
     * This annotation is retained at runtime and is targetable at fields. It can
     * be used in frameworks or libraries that need to introspect configuration
     * data and perform field injections or validations dynamically.
     *
     * @property types The array of supported Kotlin class types for the annotated field.
     */
    @Target(AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Property(
        vararg val types: KClass<*>,
    )

    /**
     * Represents a generic processor interface for managing serialized and deserialized data.
     * It provides methods for loading, reading, writing, saving, serializing, and deserializing objects and data.
     *
     * The interface is parameterized with a source type `S` and a target type `T`, enabling flexible implementations
     * that can work with various data formats and types.
     */
    interface Processor<S, out T> {
        /**
         * Represents the core data object used within the `Processor` class. This variable
         * is used to store and process generic serialized or deserialized content.
         *
         * The type `S` is determined by the `Processor` implementation and can be customized
         * as per the needs of specific use cases. The data stored might represent configuration
         * settings, serialized objects, or intermediate processing state.
         */
        val data: S

        /**
         * Loads and deserializes data from the provided input stream into a map representation,
         * where the keys are property names and the values represent their corresponding serialized objects.
         *
         * @param inputStream The input stream from which the data will be loaded. Must not be null.
         * @param clazz The class type to which the data should be mapped. This specifies the schema or structure
         *              expected for the deserialized elements.
         * @return A map containing key-value pairs, where keys are strings representing property or field names,
         *         and values are the deserialized objects of the specified type.
         */
        fun load(
            inputStream: InputStream,
            clazz: Class<*>,
        ): Map<String, T>

        /**
         * Retrieves the value associated with the provided key from the processor's internal storage.
         *
         * @param key The unique identifier used to retrieve the associated value. Must not be null.
         * @return The value of type `T` associated with the key, or null if the key is not found.
         */
        fun read(key: String): T?

        /**
         * Reads a value associated with a given key from the processor's internal storage.
         * If the key does not exist, the specified default value will be returned.
         *
         * @param key The unique identifier for the value to be retrieved.
         * @param def The default value to return if the key is not found in the storage.
         * @return The value associated with the specified key, or the default value if the key is not found.
         * Returns null if no value exists and no default is provided.
         */
        fun read(
            key: String,
            def: @UnsafeVariance T,
        ): T?

        /**
         * Writes the provided serialized content.
         *
         * @param serializedContent a map containing the serialized content to be written,
         * where the keys are strings and the values are potentially variant of type T
         */
        fun write(serializedContent: Map<String, @UnsafeVariance T>)

        /**
         * Writes the provided instance into the processor's underlying configuration or serialized storage.
         *
         * @param instance The instance to be serialized and written. This object holds the data
         *                 that should be saved or persisted by the processor.
         */
        fun write(instance: Any)

        /**
         * Writes a value associated with a given key to the processor's internal storage mechanism.
         *
         * @param key The unique identifier used to associate the value in the storage.
         * @param value The value to be written, where the type may vary depending on the processor implementation.
         */
        fun write(
            key: String,
            value: @UnsafeVariance T,
        )

        /**
         * Saves the provided serialized content and returns the file name where the data was saved.
         *
         * @param serializedContent The map containing serialized key-value pairs to be saved. Each key is a string,
         * and each value is of type `T`, which may be subject to unsafe variance. Must not be null.
         * @return The name of the file where the serialized content has been saved. This is typically derived
         * from the configuration's predefined file name or path.
         */
        fun save(serializedContent: Map<String, @UnsafeVariance T>): String

        /**
         * Serializes the given instance into a map representation where the keys are property names
         * and the values are their corresponding serialized values.
         *
         * @param instance The object instance to serialize. Must not be null.
         * @return A map containing the serialized key-value pairs representing the object's properties.
         */
        fun serialize(instance: Any): Map<String, T>

        /**
         * Deserializes a given map of serialized data into an instance of the specified type.
         *
         * @param serializedContent The map containing serialized data where keys represent property names
         *                          and values represent their respective serialized values.
         * @param type The target class type to which the serialized content will be deserialized.
         * @return An instance of the specified type populated with data from the serialized content,
         *         or null if deserialization fails.
         */
        fun deserialize(
            serializedContent: Map<String, @UnsafeVariance T>,
            type: Class<*>,
        ): Any?
    }
}
