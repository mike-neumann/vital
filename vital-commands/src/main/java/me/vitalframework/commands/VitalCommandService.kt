package me.vitalframework.commands

import me.vitalframework.Vital.context
import org.springframework.stereotype.Service

@Service
class VitalCommandService {
    fun getCommands(vitalInventoryClass: Class<out VitalCommand<*, *>?>): MutableCollection<out VitalCommand<*, *>?> {
        return try {
            context.getBeansOfType(vitalInventoryClass).values
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    val commands: MutableCollection<out VitalCommand<*, *>?>
        // must be suppressed otherwise compiler cant convert types safely
        @Suppress("UNCHECKED_CAST")
        get() = getCommands(((VitalCommand::class.java) as Class<*>) as Class<out VitalCommand<*, *>?>)

    fun <T : VitalCommand<*, *>?> getCommand(vitalInventoryClass: Class<T?>): T? {
        return try {
            context.getBean<T?>(vitalInventoryClass)
        } catch (e: Exception) {
            null
        }
    }
}