package me.vitalframework.configs.processor

import me.vitalframework.configs.VitalConfig.Companion.injectField
import me.vitalframework.configs.VitalConfig.FileProcessor
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*

class VitalPropertiesConfigFileProcessor(
    override val file: File
) : FileProcessor {
    val properties = Properties()

    @Throws(Exception::class)
    override fun load(type: Class<*>): Map<String, String> {
        properties.load(FileReader(file))

        return mapOf(*properties.entries.map { (key, value) -> key.toString() to value.toString() }.toTypedArray())
    }

    override fun read(key: String): String? {
        return properties.getProperty(key)
    }

    override fun read(key: String, def: Any): Any? {
        return properties.getProperty(key, def.toString())
    }

    override fun write(serializedContentMap: Map<String, *>) {
        serializedContentMap.forEach { (key, value) ->
            this.write(key, value!!)
        }
    }

    override fun write(`object`: Any) {
        write(serialize(`object`))
    }

    override fun write(key: String, value: Any) {
        properties.setProperty(key, value.toString())
    }

    @Throws(Exception::class)
    override fun save(serializedContentMap: Map<String, *>) {
        serializedContentMap.entries
            .map { (key, value) -> key to value.toString() }
            .forEach { (key, value) ->
                properties.setProperty(key, value)
            }

        properties.store(FileWriter(file), null)
    }

    override fun serialize(`object`: Any): Map<String, String> {
        val stringObjectMap = mutableMapOf<String, String>()

        getPropertyFieldsFromType(`object`.javaClass)
            .filter { String::class.java.isAssignableFrom(it.type) }
            .map {
                try {
                    // else use default snakeyaml mapping.
                    return@map it.name to it[`object`]
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                    throw RuntimeException("error while serializing properties config field '${it.name}'")
                }
            }
            .forEach { (key, value) ->
                stringObjectMap[key] = value as String
            }

        return stringObjectMap
    }

    @Throws(Exception::class)
    override fun deserialize(serializedContentMap: Map<String, *>, type: Class<*>): Any? {
        try {
            val defaultConstructor = type.getConstructor()
            val `object` = defaultConstructor.newInstance()

            // default constructor was found, inject field properties...
            serializedContentMap
                .forEach { (key, value) ->
                    getFieldByProperty(type, key)?.let {
                        injectField(
                            `object`,
                            it,
                            value
                        )
                    }
                }

            return `object`
        } catch (e: NoSuchMethodException) {
            // default constructor not found, attempt to get constructor matching properties...
            val constructor = type.getConstructor(*getPropertyFieldsFromType(type).map { it.javaClass }.toTypedArray())

            // constructor found, create new instance with this constructor...
            return constructor.newInstance(serializedContentMap.values)
        }
    }
}