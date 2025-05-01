package me.vitalframework.configs

import me.vitalframework.VitalClassUtils.getRequiredAnnotation
import me.vitalframework.logger
import org.springframework.stereotype.Component
import java.io.IOException
import java.io.InputStream
import kotlin.io.path.*
import kotlin.reflect.KClass

abstract class VitalConfig {
    val log = logger()
    val fileName: String
    val processor: Processor<*, Any>

    init {
        val info = getRequiredAnnotation<Info>()

        fileName = info.name
        processor = try {
            info.processor.java.getDeclaredConstructor().newInstance()
        } catch (e: Exception) {
            throw VitalConfigException.CreateFileProcessor(info.name, info.processor.java, e)
        }
        val file = Path(fileName)
        val inputStream = when {
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

    fun save(writeToFile: Boolean = true) {
        try {
            if (writeToFile) {
                val file = Path(fileName)
                // create the file if it does not exist
                if (!file.exists()) {
                    if (file.parent != null) file.parent.createDirectories()

                    try {
                        file.createFile()
                        log.debug("${file.name} config file created")
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
        val serializedContent = processor.load(inputStream, javaClass)

        for ((key, value) in serializedContent) {
            VitalConfigUtils.getFieldByProperty(javaClass, key)?.let { VitalConfigUtils.injectField(this, it, value) }
        }
    }

    @Component
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Info(val name: String, val processor: KClass<out Processor<*, Any>>)

    @Target(AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Property(vararg val types: KClass<*>)

    interface Processor<S, out T> {
        val data: S
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