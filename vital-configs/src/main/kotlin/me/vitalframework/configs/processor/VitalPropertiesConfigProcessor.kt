package me.vitalframework.configs.processor

import me.vitalframework.configs.VitalConfig.Processor
import me.vitalframework.configs.VitalConfigException
import me.vitalframework.configs.VitalConfigUtils
import java.io.InputStream
import java.io.StringWriter
import java.util.*

class VitalPropertiesConfigProcessor() : Processor<String> {
    val properties = Properties()

    override fun load(inputStream: InputStream, clazz: Class<*>): Map<String, String> {
        properties.load(inputStream)

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

    override fun save(serializedContent: Map<String, String>): String {
        serializedContent.entries.forEach { (key, value) ->
            properties.setProperty(key, value)
        }

        val stringWriter = StringWriter()

        properties.store(stringWriter, null)

        return stringWriter.toString()
    }

    override fun serialize(instance: Any): Map<String, String> {
        val stringObject = mutableMapOf<String, String>()

        VitalConfigUtils.getPropertyFieldsFromType(instance.javaClass)
            .filter { String::class.java.isAssignableFrom(it.type) }
            .map {
                try {
                    // else use default snakeyaml mapping.
                    return@map it.name to it[instance]
                } catch (e: IllegalAccessException) {
                    throw VitalConfigException.SerializeField(it, e)
                }
            }
            .forEach { (key, value) ->
                stringObject[key] = value as String
            }

        return stringObject
    }

    override fun deserialize(serializedContent: Map<String, String>, type: Class<*>): Any? {
        try {
            val defaultConstructor = type.getConstructor()
            val instance = defaultConstructor.newInstance()

            // default constructor was found, inject field properties...
            serializedContent
                .forEach { (key, value) ->
                    VitalConfigUtils.getFieldByProperty(type, key)?.let {
                        VitalConfigUtils.injectField(
                            instance,
                            it,
                            value
                        )
                    }
                }

            return instance
        } catch (e: NoSuchMethodException) {
            // default constructor not found, attempt to get constructor matching properties...
            val constructor = type.getConstructor(*VitalConfigUtils.getPropertyFieldsFromType(type).map { it.javaClass }
                .toTypedArray())

            // constructor found, create new instance with this constructor...
            return constructor.newInstance(serializedContent.values)
        }
    }
}