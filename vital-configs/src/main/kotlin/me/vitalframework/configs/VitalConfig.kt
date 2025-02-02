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
    val processor: Processor<*, Any>

    init {
        val info = getRequiredAnnotation()

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
                // create file is not already exists
                if (!file.exists()) {
                    if (file.parent != null) {
                        file.parent.createDirectories()
                    }

                    try {
                        file.createFile()
                        log.debug("{} config file created", file.name)
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
            VitalConfigUtils.getFieldByProperty(javaClass, key)?.let {
                VitalConfigUtils.injectField(this, it, value)
            }
        }
    }

    @Component
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Info(val name: String, val processor: KClass<out Processor<*, Any>>)

    @Target(AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Property(vararg val value: KClass<*>)

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