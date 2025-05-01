package me.vitalframework.configs.processor

import me.vitalframework.configs.VitalConfig.Processor
import me.vitalframework.configs.VitalConfigUtils
import java.io.InputStream
import java.io.StringWriter
import java.util.*

class VitalPropertiesConfigProcessor : Processor<Properties, String> {
    override val data = Properties()

    override fun load(inputStream: InputStream, clazz: Class<*>): Map<String, String> {
        data.load(inputStream)
        return mapOf(*data.entries.map { (key, value) -> key.toString() to value.toString() }.toTypedArray())
    }

    override fun read(key: String): String? = data.getProperty(key)
    override fun read(key: String, def: String): String? = data.getProperty(key, def)

    override fun write(instance: Any) = write(serialize(instance))

    override fun write(key: String, value: String) {
        data.setProperty(key, value)
    }

    override fun write(serializedContent: Map<String, String>) {
        for ((key, value) in serializedContent) {
            write(key, value)
        }
    }

    override fun save(serializedContent: Map<String, String>): String {
        for ((key, value) in serializedContent.entries) {
            data.setProperty(key, value)
        }
        val stringWriter = StringWriter()

        data.store(stringWriter, null)

        return stringWriter.toString()
    }

    override fun serialize(instance: Any) = VitalConfigUtils.getPropertyFieldsFromType(instance.javaClass)
        .filter { String::class.java.isAssignableFrom(it.type) }
        .associate { it.name to VitalConfigUtils.readField(instance, it) as String }

    override fun deserialize(serializedContent: Map<String, String>, type: Class<*>): Any? = try {
        val defaultConstructor = type.getConstructor()
        val instance = defaultConstructor.newInstance()
        // default constructor was found, inject field properties...
        for ((key, value) in serializedContent) {
            VitalConfigUtils.getFieldByProperty(type, key)?.let { VitalConfigUtils.injectField(instance, it, value) }
        }

        instance
    } catch (_: NoSuchMethodException) {
        // the default constructor was not found, attempt to get constructor matching properties...
        val constructor = type.getConstructor(*VitalConfigUtils.getPropertyFieldsFromType(type).map { it.javaClass }.toTypedArray())
        // constructor found, create a new instance with this constructor...
        constructor.newInstance(serializedContent.values)
    }
}