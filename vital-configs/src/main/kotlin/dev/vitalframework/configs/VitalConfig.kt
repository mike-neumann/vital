package dev.vitalframework.configs

import dev.vitalframework.VitalCoreModule.Companion.getRequiredAnnotation
import dev.vitalframework.VitalCoreModule.Companion.logger
import dev.vitalframework.VitalHasInfo
import dev.vitalframework.configs.processor.VitalPropertiesConfigProcessor
import org.springframework.stereotype.Component
import java.io.IOException
import java.io.InputStream
import java.lang.reflect.Field
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.name
import kotlin.io.path.writeText
import kotlin.reflect.KClass

/**
 * Defines a config within the Vital-Framework.
 * A config class defines the structure and datatypes of a singular configuration file stored on the filesystem.
 * It's data can be loaded from the filesystem into the class to expose a developer-friendly api to access it.
 *
 * Each field annotated with [VitalConfig.Property] will be defined as a valid property for serialization and deserialization.
 *
 * The consuming class must be annotated with [VitalConfig.Info].
 *
 * ```java
 * @VitalConfig.Info(
 *   name = "my-vital-config.yaml",
 *   processor = VitalYAMLConfigProcessor.class
 * )
 * public class MyVitalConfig extends VitalConfig {
 *   @Property(types = {String.class})
 *   private String myString;
 *
 *   @Property(types = {String.class})
 *   private List<String> myStringList;
 *
 *   @Property(types = {String.class, Long.class})
 *   private Map<String, Long> myStringLongMap;
 * }
 * ```
 */
abstract class VitalConfig : VitalHasInfo {
    override val info = mutableMapOf(Info::class.java to javaClass.getRequiredAnnotation<Info>())

    val logger = logger()

    val file: Path
    val processor: Processor<*, Any>

    init {
        val info = getInfo(Info::class.java)
        file = Path(info.name)
        processor =
            try {
                info.processor.java
                    .getDeclaredConstructor()
                    .newInstance()
            } catch (e: Exception) {
                throw VitalConfigException.CreateFileProcessor(file.name, info.processor.java, e)
            }

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
     * Saves this config by writing its field values to the defined file in path [Info.name].
     */
    fun save() {
        val info = getInfo(Info::class.java)
        val loggingContext = "Context: Config '${info.name}', processor '${info.processor.simpleName}', class '${javaClass.simpleName}'"
        try {
            logger.debug("Saving config. $loggingContext")
            // create the file if it does not exist
            if (!file.exists()) {
                logger.debug("Config file doesnt exist yet, creating it. $loggingContext")
                if (file.parent != null) {
                    logger.debug("Config file parent directory doesnt exist yet, creating it. $loggingContext")
                    file.parent.createDirectories()
                }

                try {
                    file.createFile()
                    logger.debug("Config file created. $loggingContext")
                } catch (e: IOException) {
                    throw VitalConfigException.CreateFile(file.name, e)
                }
            } else {
                logger.debug("Config file already exists, writing directly to it. $loggingContext")
            }

            file.writeText(processor.save(processor.serialize(this)))
        } catch (e: Exception) {
            throw VitalConfigException.Save(file.name, e)
        }
    }

    /**
     * Loads this config from the given [inputStream].
     * This function will load the serialized content
     * via the defined [Info.processor] and initialize this class's fields.
     */
    fun load(inputStream: InputStream) {
        val info = getInfo(Info::class.java)
        val loggingContext = "Context: Config '${info.name}', processor '${info.processor.simpleName}', class '${javaClass.simpleName}'"
        logger.debug("Loading config from input stream. $loggingContext")
        val serializedContent = processor.load(inputStream, javaClass)
        logger.debug("Injecting serialized content '$serializedContent'. $loggingContext")

        for ((key, value) in serializedContent) {
            val field = javaClass.getFieldByProperty(key)
            if (field != null) {
                logger.debug("Key '$key' matches field '$field', attempting to inject it. $loggingContext")
                injectField(this, field, value)
            } else {
                logger.debug("Key '$key' does not match any field, cannot inject it. $loggingContext")
            }
        }
    }

    companion object {
        /**
         * Reads the given [field] using the given [accessor].
         * This function bypasses any modifier to directly access the field's value.
         * If this fails for any reason, [VitalConfigException.ReadField] is thrown.
         */
        @JvmStatic
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
         * Injects the given [value] into the [field] for the given [accessor].
         * This function bypasses any modifier to directly access and write to the given [field].
         * If the injection fails for any reason, [VitalConfigException.InjectField] is thrown.
         */
        @JvmStatic
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
         * Gets all property fields from the given class that are annotated via [Property].
         */
        @JvmStatic
        fun Class<*>.getPropertyFieldsFromType() = declaredFields.filter { it.isAnnotationPresent(Property::class.java) }

        /**
         * Gets all property fields from the given class that are annotated via [Property].
         */
        @JvmStatic
        fun KClass<*>.getPropertyFieldsFromType() = java.getPropertyFieldsFromType()

        /**
         * Gets all non-property fields from the given class that are not annotated via [Property].
         */
        @JvmStatic
        fun Class<*>.getNonPropertyFieldsFromType() = declaredFields.filter { !it.isAnnotationPresent(Property::class.java) }

        /**
         * Gets all non-property fields from the given class that are not annotated via [Property].
         */
        @JvmStatic
        fun KClass<*>.getNonPropertyFieldsFromType() = java.getNonPropertyFieldsFromType()

        /**
         * Gets the [Field] for the given [property] and class.
         */
        @JvmStatic
        fun Class<*>.getFieldByProperty(property: String) = getPropertyFieldsFromType().firstOrNull { it.name == property }

        /**
         * Gets the [Field] for the given [property] and class.
         */
        @JvmStatic
        fun KClass<*>.getFieldByProperty(property: String) = java.getFieldByProperty(property)
    }

    /**
     * Defines the info for a [VitalConfig].
     */
    @Component
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Info(
        val name: String,
        val processor: KClass<out Processor<*, Any>>,
    )

    /**
     * This annotation marks a [Field] as a property that can automatically be injected by a [VitalConfig].
     * Use this annotation inside a [VitalConfig] implementer or any other class than is used as a value in a [VitalConfig] implementer.
     */
    @Target(AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Property(
        vararg val types: KClass<*>,
    )

    /**
     * Defines a processor that can serialize and deserialize content for a [VitalConfig].
     * Implementation classes should be used in [Info.processor].
     */
    interface Processor<S, out T> {
        /**
         * The data object this serialize will use to serialize the content.
         * E.g., [java.util.Properties] for a processor that can write and load `.properties` configs.
         */
        val data: S

        /**
         * Loads the given [inputStream] into the [data] of this config and returns a [Map] of all properties to value mappings.
         */
        fun load(
            inputStream: InputStream,
            clazz: Class<*>,
        ): Map<String, T>

        /**
         * Reads a single value for the given [key] from the [data] of this config.
         */
        fun read(key: String): T?

        /**
         * Reads a single value for the given [key] from the [data] of this config.
         * If no value was found, this function will return the given [def].
         */
        fun read(
            key: String,
            def: @UnsafeVariance T,
        ): T?

        /**
         * Writes the given [serializedContent] to the file of this config.
         */
        fun write(serializedContent: Map<String, @UnsafeVariance T>)

        /**
         * Writes the given [instance] to the file of this config.
         * E.g., the [VitalPropertiesConfigProcessor] first serialized its content via [serialize] and then calls [write] with it.
         */
        fun write(instance: Any)

        /**
         * Writes the given [value] to the property with the given [key] to the [data] of this config.
         */
        fun write(
            key: String,
            value: @UnsafeVariance T,
        )

        /**
         * Saves the given [serializedContent] to the [data] of this config.
         */
        fun save(serializedContent: Map<String, @UnsafeVariance T>): String

        /**
         * Serializes the content of the given [instance] to a [Map] where each entry is the name and value for each [Field] of the given [instance].
         */
        fun serialize(instance: Any): Map<String, T>

        /**
         * Deserializes the given [serializedContent] to the given [type] returns an initialized instance of it.
         */
        fun deserialize(
            serializedContent: Map<String, @UnsafeVariance T>,
            type: Class<*>,
        ): Any?
    }
}
