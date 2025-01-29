package me.vitalframework.configs.processor

import me.vitalframework.configs.VitalConfig
import me.vitalframework.configs.VitalConfig.Companion.injectField
import me.vitalframework.configs.VitalConfig.FileProcessor
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.TypeDescription
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.inspector.TagInspector
import org.yaml.snakeyaml.representer.Representer
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.lang.reflect.Constructor

class VitalYAMLConfigFileProcessor(
    override val file: File,
) : FileProcessor<Any> {
    private val yaml: Yaml
    private val data: MutableMap<String, Any> = HashMap()

    init {
        val loaderOptions = LoaderOptions().apply {
            tagInspector = TagInspector { true }
        }
        val constructor = org.yaml.snakeyaml.constructor.Constructor(loaderOptions)
        val dumperOptions = DumperOptions().apply {
            defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
        }
        val representer = Representer(dumperOptions)

        yaml = Yaml(constructor, representer, dumperOptions, loaderOptions)
    }

    private fun addTypeDescriptors(type: Class<*>) {
        val rootTypeDescription = TypeDescription(type, "!${type.simpleName}")
        val rootExcludes = getNonPropertyFieldsFromType(type)
            .map { it.name }
            .toTypedArray()

        rootTypeDescription.setExcludes(*rootExcludes)
        yaml.addTypeDescription(rootTypeDescription)

        for (field in getPropertyFieldsFromType(type)) {
            val vitalConfigProperty = field.getAnnotation(VitalConfig.Property::class.java)
            val typeDescription = TypeDescription(field.type, "!${field.type.simpleName}")
            val excludes = getNonPropertyFieldsFromType(field.type)
                .map { it.name }
                .toTypedArray()

            typeDescription.setExcludes(*excludes)
            yaml.addTypeDescription(typeDescription)

            // add type descriptors for annotated property types...
            vitalConfigProperty.value.forEach {
                addTypeDescriptors(it.java)
            }

            addTypeDescriptors(field.type)
        }
    }

    @Throws(Exception::class)
    override fun load(clazz: Class<*>): Map<String, Any> {
        data.clear()

        // add type descriptors for complex types...
        addTypeDescriptors(clazz)

        val data = yaml.load<Map<String, Any>>(FileReader(file))

        if (data != null) {
            this.data.putAll(data)
        }

        return this.data
    }

    override fun read(key: String): Any? {
        return data[key]
    }

    override fun read(key: String, def: Any): Any? {
        return data.getOrDefault(key, def)
    }

    override fun write(serializedContent: Map<String, Any>) {
        data.putAll(serializedContent)
    }

    override fun write(instance: Any) {
    }

    @Throws(Exception::class)
    override fun write(key: String, value: Any) {
        data[key] = serialize(value)
    }

    @Throws(Exception::class)
    override fun save(serializedContent: Map<String, Any>) {
        data.putAll(serializedContent)
        yaml.dump(data, FileWriter(file))
    }

    @Throws(Exception::class)
    override fun serialize(instance: Any): Map<String, Any> {
        val stringObjectMap = mutableMapOf<String, Any>()

        getPropertyFieldsFromType(instance.javaClass).stream()
            .map {
                try {
                    // else use default snakeyaml mapping.
                    // force field to be accessible even if private
                    it.isAccessible = true

                    return@map it.name to it[instance]
                } catch (e: Exception) {
                    e.printStackTrace()
                    throw RuntimeException("error while serializing yml config field '${it.name}'")
                }
            }
            .forEach { (key, value) ->
                stringObjectMap[key] = value
            }

        return stringObjectMap
    }

    @Throws(Exception::class)
    override fun deserialize(serializedContent: Map<String, Any>, type: Class<*>): Any {
        try {
            val defaultConstructor: Constructor<*> = type.getConstructor()
            val instance = defaultConstructor.newInstance()

            // default constructor was found, inject field properties...
            serializedContent
                .forEach { (key, value) ->
                    getFieldByProperty(type, key)?.let {
                        injectField(instance, it, value)
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