package me.vitalframework.configs

import me.vitalframework.configs.VitalConfig.Property
import java.lang.reflect.Field

object VitalConfigUtils {
    fun injectField(accessor: Any, field: Field, value: Any?) {
        try {
            // force field to be accessible even if private
            // this is needed for injection...
            field.isAccessible = true
            field[accessor] = value
        } catch (e: IllegalAccessException) {
            throw VitalConfigException.InjectField(field, value, e)
        }
    }

    fun getPropertyFieldsFromType(clazz: Class<*>) = clazz.declaredFields
        .filter { it.isAnnotationPresent(Property::class.java) }

    fun getNonPropertyFieldsFromType(clazz: Class<*>) = clazz.declaredFields
        .filter { !it.isAnnotationPresent(Property::class.java) }

    fun getFieldByProperty(clazz: Class<*>, property: String) = getPropertyFieldsFromType(clazz)
        .firstOrNull { it.name == property }
}