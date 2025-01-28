package me.vitalframework.configs.processor

import me.vitalframework.configs.VitalConfig.Companion.injectField
import me.vitalframework.configs.VitalConfig.FileProcessor
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*

class VitalPropertiesConfigFileProcessor(
    override val file: File,
) : FileProcessor<String> {
    val properties = Properties()

    @Throws(Exception::class)
    override fun load(type: Class<*>): Map<String, String> {
        properties.load(FileReader(file))

        return mapOf(*properties.entries.map { (key, value) -> key.toString() to value.toString() }.toTypedArray())
    }

    override fun read(key: String): String? {
        return properties.getProperty(key)
    }

    override fun read(key: String, def: String): String? {
        return properties.getProperty(key, def.toString())
    }

    override fun write(serializedContent: Map<String, String>) {
        serializedContent.forEach { (key, value) ->
            this.write(key, value)
        }
    }

    override fun write(instance: Any) {
        write(serialize(instance))
    }

    override fun write(key: String, value: String) {
        properties.setProperty(key, value)
    }

    @Throws(Exception::class)
    override fun save(serializedContent: Map<String, String>) {
        serializedContent.entries.forEach { (key, value) ->
            properties.setProperty(key, value)
        }

        properties.store(FileWriter(file), null)
    }

    override fun serialize(instance: Any): Map<String, String> {
        val stringObject = mutableMapOf<String, String>()

        getPropertyFieldsFromType(instance.javaClass)
            .filter { String::class.java.isAssignableFrom(it.type) }
            .map {
                try {
                    // else use default snakeyaml mapping.
                    return@map it.name to it[instance]
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                    throw RuntimeException("error while serializing properties config field '${it.name}'")
                }
            }
            .forEach { (key, value) ->
                stringObject[key] = value as String
            }

        return stringObject
    }

    @Throws(Exception::class)
    override fun deserialize(serializedContent: Map<String, String>, type: Class<*>): Any? {
        try {
            val defaultConstructor = type.getConstructor()
            val instance = defaultConstructor.newInstance()

            // default constructor was found, inject field properties...
            serializedContent
                .forEach { (key, value) ->
                    getFieldByProperty(type, key)?.let {
                        injectField(
                            instance,
                            it,
                            value
                        )
                    }
                }

            return instance
        } catch (e: NoSuchMethodException) {
            // default constructor not found, attempt to get constructor matching properties...
            val constructor = type.getConstructor(*getPropertyFieldsFromType(type).map { it.javaClass }.toTypedArray())

            // constructor found, create new instance with this constructor...
            return constructor.newInstance(serializedContent.values)
        }
    }
}