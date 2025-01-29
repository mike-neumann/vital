package me.vitalframework.configs

import me.vitalframework.RequiresAnnotation
import me.vitalframework.logger
import org.springframework.stereotype.Component
import java.io.IOException
import java.io.InputStream
import kotlin.io.path.*
import kotlin.reflect.KClass

abstract class VitalConfig : RequiresAnnotation<VitalConfig.Info> {
    val log = logger()
    val fileName: String
    val processor: Processor<Any>

    init {
        val info = getRequiredAnnotation()

        fileName = info.name
        processor =
            try {
                info.processor.java.getDeclaredConstructor().newInstance()
            } catch (e: Exception) {
                throw VitalConfigException.CreateFileProcessor(info.name, info.processor.java, e)
            }

        val file = Path(fileName)
        val inputStream =
            when (file.exists()) {
                true -> file.inputStream()
                else -> "".byteInputStream()
            }

        try {
            // after everything has worked without problem, inject field of our config with the values now retrievable...
            load(inputStream)
        } catch (e: Exception) {
            throw VitalConfigException.InjectFields(info.name, info.processor.java, e)
        }
    }

    override fun requiredAnnotationType() = Info::class.java

    fun save(writeToFile: Boolean = true) {
        try {
            if (writeToFile) {
                val file = Path(fileName)

                if (!file.exists()) {
                    if (file.parent != null) {
                        file.parent.createDirectories()
                    }

                    try {
                        file.createFile()
                        log.info("{} config file created", file.name)
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

    fun load(inputStream: InputStream) {
        injectFields(processor.load(inputStream, javaClass))
    }

    private fun injectFields(serializedContentMap: Map<String, *>) {
        serializedContentMap
            .forEach { (key, value) ->
                VitalConfigUtils.getFieldByProperty(javaClass, key)?.let {
                    VitalConfigUtils.injectField(this@VitalConfig, it, value)
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
        val processor: KClass<out Processor<Any>>,
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
    interface Processor<out T> {
        fun load(inputStream: InputStream, clazz: Class<*>): Map<String, T>
        fun read(key: String): T?
        fun read(key: String, def: @UnsafeVariance T): T?
        fun write(serializedContent: Map<String, @UnsafeVariance T>)
        fun write(instance: Any)
        fun write(key: String, value: @UnsafeVariance T)
        fun save(serializedContent: Map<String, @UnsafeVariance T>): String
        fun serialize(instance: Any): Map<String, T>
        fun deserialize(serializedContent: Map<String, @UnsafeVariance T>, type: Class<*>): Any?
    }
}