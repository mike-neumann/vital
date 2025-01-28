package me.vitalframework.commands

import me.vitalframework.Vital.context
import org.springframework.stereotype.Service

@Service
class VitalCommandService {
    fun getCommands(vitalInventoryClass: Class<VitalCommand<*, *>>) =
        try {
            context.getBeansOfType(vitalInventoryClass).values
        } catch (e: Exception) {
            mutableListOf()
        }

    // must be suppressed otherwise compiler cant convert types safely
    @Suppress("UNCHECKED_CAST")
    fun getCommands() = getCommands(((VitalCommand::class.java) as Class<*>) as Class<VitalCommand<*, *>>)

    fun <T : VitalCommand<*, *>> getCommand(vitalInventoryClass: Class<T>) =
        try {
            context.getBean<T?>(vitalInventoryClass)
        } catch (e: Exception) {
            null
        }
}