package me.vitalframework.configs.processor

import me.vitalframework.configs.VitalConfig
import me.vitalframework.configs.VitalConfig.Processor
import me.vitalframework.configs.VitalConfigException
import me.vitalframework.configs.VitalConfigUtils
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.TypeDescription
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.inspector.TagInspector
import org.yaml.snakeyaml.representer.Representer
import java.io.InputStream
import java.io.StringWriter
import java.lang.reflect.Constructor

class VitalYAMLConfigProcessor : Processor<Any> {
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
        val rootExcludes = VitalConfigUtils.getNonPropertyFieldsFromType(type)
            .map { it.name }
            .toTypedArray()

        rootTypeDescription.setExcludes(*rootExcludes)
        yaml.addTypeDescription(rootTypeDescription)

        for (field in VitalConfigUtils.getPropertyFieldsFromType(type)) {
            val vitalConfigProperty = field.getAnnotation(VitalConfig.Property::class.java)
            val typeDescription = TypeDescription(field.type, "!${field.type.simpleName}")
            val excludes = VitalConfigUtils.getNonPropertyFieldsFromType(field.type)
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

    override fun load(inputStream: InputStream, clazz: Class<*>): Map<String, Any> {
        data.clear()

        // add type descriptors for complex types...
        addTypeDescriptors(clazz)

        val data = yaml.load<Map<String, Any>>(inputStream)

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

    override fun write(key: String, value: Any) {
        data[key] = serialize(value)
    }

    override fun save(serializedContent: Map<String, Any>): String {
        data.putAll(serializedContent)

        val stringWriter = StringWriter()

        yaml.dump(data, stringWriter)

        return stringWriter.toString()
    }

    override fun serialize(instance: Any): Map<String, Any> {
        val stringObjectMap = mutableMapOf<String, Any>()

        VitalConfigUtils.getPropertyFieldsFromType(instance.javaClass).stream()
            .map {
                try {
                    // else use default snakeyaml mapping.
                    // force field to be accessible even if private
                    it.isAccessible = true

                    return@map it.name to it[instance]
                } catch (e: Exception) {
                    throw VitalConfigException.SerializeField(it, e)
                }
            }
            .forEach { (key, value) ->
                stringObjectMap[key] = value
            }

        return stringObjectMap
    }

    override fun deserialize(serializedContent: Map<String, Any>, type: Class<*>): Any {
        try {
            val defaultConstructor: Constructor<*> = type.getConstructor()
            val instance = defaultConstructor.newInstance()

            // default constructor was found, inject field properties...
            serializedContent
                .forEach { (key, value) ->
                    VitalConfigUtils.getFieldByProperty(type, key)?.let {
                        VitalConfigUtils.injectField(instance, it, value)
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