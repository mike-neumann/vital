package me.vitalframework.configs

import me.vitalframework.RequiresAnnotation
import me.vitalframework.Vital.logger
import org.springframework.stereotype.Component
import java.io.File
import java.io.IOException
import java.lang.reflect.Field
import kotlin.reflect.KClass

abstract class VitalConfig : RequiresAnnotation<VitalConfig.Info> {
    val log = logger()
    var vitalConfigFileProcessor: FileProcessor<Any>? = null
        private set

    init {
        val info = getRequiredAnnotation()

        load(info.name, info.processor.java)
    }

    override fun requiredAnnotationType() = Info::class.java

    fun save() {
        try {
            vitalConfigFileProcessor!!.save(vitalConfigFileProcessor!!.serialize(this))
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("error while saving config")
        }
    }

    private fun load(fileName: String, processor: Class<out FileProcessor<Any>>) {
        try {
            val file = createFile(fileName)

            // attempt to create default processor instance.
            val defaultConstructor = processor.getDeclaredConstructor(File::class.java)

            vitalConfigFileProcessor = defaultConstructor.newInstance(file)

            try {
                // after everything has worked without problem, inject field of our config with the values now retrievable...
                injectFields(vitalConfigFileProcessor!!.load(javaClass))
            } catch (e: Exception) {
                e.printStackTrace()
                throw RuntimeException("error while injecting fields for config '$fileName' with processor '${processor.simpleName}'")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("error while creating config file processor '${processor.simpleName}' for config '$fileName'")
        }
    }

    private fun createFile(fileName: String): File {
        val file = File(fileName)

        if (!file.exists()) {
            if (file.parentFile != null) {
                file.parentFile.mkdirs()
            }

            try {
                val fileCreated = file.createNewFile()

                if (fileCreated) {
                    log.info("{} config file created", fileName)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                throw RuntimeException("error while creating config file '$fileName'")
            }
        }

        return file
    }

    private fun injectFields(serializedContentMap: Map<String, *>) {
        serializedContentMap
            .forEach { (key: String, value: Any?) ->
                val field = vitalConfigFileProcessor!!.getFieldByProperty(javaClass, key)

                field?.let {
                    injectField(this@VitalConfig, it, value)
                }
            }
    }

    /**
     * Defines meta-information for the annotated config.
     */
    @Component
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Info(
        /**
         * Defines the file name (including extension) for the annotated config.
         */
        val name: String,
        /**
         * Defines the file processor used by this config.
         */
        val processor: KClass<out FileProcessor<Any>>,
    )

    /**
     * Defines a field within a config extending class to be a key.
     */
    @Target(AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Property(
        /**
         * Defines the class types this annotated field manages.
         * When annotating a list or map, specify their generic types.
         */
        vararg val value: KClass<*>,
    )

    /**
     * Describes an object which is capable of processing the contents of a given config file.
     */
    interface FileProcessor<T> {
        val file: File

        /**
         * Loads the file processed by this processor.
         */
        @Throws(Exception::class)
        fun load(clazz: Class<*>): Map<String, T>

        /**
         * Reads a config value by the specified key.
         */
        fun read(key: String): T?

        /**
         * Reads a config value by the specified key, if that value was not found, returns a default value.
         */
        fun read(key: String, def: T): T?

        /**
         * Writes the given serialized content to this config.
         */
        fun write(serializedContent: Map<String, T>)

        /**
         * Writes the given object to this config.
         */
        fun write(instance: Any)

        /**
         * Writes the given value to this config.
         */
        @Throws(Exception::class)
        fun write(key: String, value: T)

        /**
         * Saves the given serialized content to this config.
         */
        // must use star-projection here, default map impl does not support contravariance
        @Throws(Exception::class)
        fun save(serializedContent: Map<String, T>)

        /**
         * Serializes the given object for config usage.
         */
        @Throws(Exception::class)
        fun serialize(instance: Any): Map<String, T>

        /**
         * Deserializes the given serialized map to the specified object type.
         *
         * @param serializedContent The serialized content.
         * @param type                 The type to deserialize to.
         * @return The deserialized object.
         * @throws Exception If any error occurs while deserializing.
         */
        @Throws(Exception::class)
        fun deserialize(serializedContent: Map<String, T>, type: Class<*>): Any?

        /**
         * Gets all property fields of the given type.
         *
         * @param clazz The type to fetch all property fields from.
         * @return All property fields of the given type.
         */
        fun getPropertyFieldsFromType(clazz: Class<*>) = clazz.declaredFields
            .filter { it.isAnnotationPresent(Property::class.java) }

        /**
         * Gets all non property fields of the given type.
         *
         * @param clazz The type to fetch all non property fields from.
         * @return All non property fields of the given type.
         */
        fun getNonPropertyFieldsFromType(clazz: Class<*>) = clazz.declaredFields
            .filter { !it.isAnnotationPresent(Property::class.java) }

        /**
         * Fetches a field of the given type by its property string.
         *
         * @param clazz     The type to fetch the field from.
         * @param property The property key of the annotated field.
         * @return The fetched field; or null.
         */
        fun getFieldByProperty(clazz: Class<*>, property: String) = getPropertyFieldsFromType(clazz)
            .firstOrNull { it.name == property }
    }

    companion object {
        @JvmStatic
        fun injectField(accessor: Any, field: Field, value: Any?) {
            try {
                // force field to be accessible even if private
                // this is needed for injection...
                field.isAccessible = true
                field[accessor] = value
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
                throw RuntimeException("error while injecting field '${field.name}' with '$value'")
            }
        }
    }
}