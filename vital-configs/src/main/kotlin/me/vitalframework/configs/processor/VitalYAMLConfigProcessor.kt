package me.vitalframework.configs.processor

import me.vitalframework.configs.*
import me.vitalframework.configs.VitalConfig.Processor
import org.yaml.snakeyaml.*
import org.yaml.snakeyaml.inspector.TagInspector
import org.yaml.snakeyaml.representer.Representer
import java.io.InputStream
import java.io.StringWriter

class VitalYAMLConfigProcessor : Processor<MutableMap<String, Any>, Any> {
    private val yaml: Yaml
    override val data = mutableMapOf<String, Any>()

    init {
        val loaderOptions = LoaderOptions().apply {
            tagInspector = TagInspector { true }
        }
        val constructor = SnakeYamlConstructor(loaderOptions)
        val dumperOptions = DumperOptions().apply {
            defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
        }
        val representer = Representer(dumperOptions)

        yaml = Yaml(constructor, representer, dumperOptions, loaderOptions)
    }

    private fun addTypeDescriptors(type: Class<*>) {
        val rootTypeDescription = TypeDescription(type, "!${type.simpleName}")
        val rootExcludes = VitalConfigUtils.getNonPropertyFieldsFromType(type).map { it.name }.toTypedArray()

        rootTypeDescription.setExcludes(*rootExcludes)
        yaml.addTypeDescription(rootTypeDescription)

        for (field in VitalConfigUtils.getPropertyFieldsFromType(type)) {
            val configProperty = field.getAnnotation(VitalConfig.Property::class.java)
            val typeDescription = TypeDescription(field.type, "!${field.type.simpleName}")
            val excludes = VitalConfigUtils.getNonPropertyFieldsFromType(field.type).map { it.name }.toTypedArray()

            typeDescription.setExcludes(*excludes)
            yaml.addTypeDescription(typeDescription)
            // add type descriptors for annotated property types...
            for (clazz in configProperty.value) {
                addTypeDescriptors(clazz.java)
            }

            addTypeDescriptors(field.type)
        }
    }

    override fun load(inputStream: InputStream, clazz: Class<*>): Map<String, Any> {
        data.clear()
        // add type descriptors for complex types...
        addTypeDescriptors(clazz)
        val data = yaml.load<Map<String, Any>>(inputStream)

        if (data != null) this.data.putAll(data)

        return this.data
    }

    override fun read(key: String) = data[key]
    override fun read(key: String, def: Any) = data.getOrDefault(key, def)
    override fun write(serializedContent: Map<String, Any>) = data.putAll(serializedContent)
    override fun write(instance: Any) = run {}
    override fun write(key: String, value: Any) = run { data[key] = serialize(value) }

    override fun save(serializedContent: Map<String, Any>): String {
        data.putAll(serializedContent)
        val stringWriter = StringWriter()

        yaml.dump(data, stringWriter)

        return stringWriter.toString()
    }

    override fun serialize(instance: Any) = VitalConfigUtils.getPropertyFieldsFromType(instance.javaClass)
        .associate { it.name to VitalConfigUtils.readField(instance, it)!! }

    override fun deserialize(serializedContent: Map<String, Any>, type: Class<*>): Any? = try {
        val defaultConstructor = type.getConstructor()
        val instance = defaultConstructor.newInstance()
        // default constructor was found, inject field properties...
        for ((key, value) in serializedContent) {
            VitalConfigUtils.getFieldByProperty(type, key)?.let { VitalConfigUtils.injectField(instance, it, value) }
        }

        instance
    } catch (e: NoSuchMethodException) {
        // default constructor not found, attempt to get constructor matching properties...
        val constructor = type.getConstructor(*VitalConfigUtils.getPropertyFieldsFromType(type).map { it.javaClass }.toTypedArray())
        // constructor found, create new instance with this constructor...
        constructor.newInstance(serializedContent.values)
    }
}