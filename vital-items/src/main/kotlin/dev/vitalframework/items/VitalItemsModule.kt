package dev.vitalframework.items

import dev.vitalframework.RequiresSpigot
import dev.vitalframework.VitalCoreModule.Companion.logger
import dev.vitalframework.VitalModule
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration

@VitalModule.Info(value = "vital-items")
class VitalItemsModule(
    val vitalItems: List<VitalItem>,
) : VitalModule() {
    val logger = logger()

    override fun onEnable() {
        try {
            Class.forName("org.bukkit.Bukkit")
        } catch (_: Exception) {
            logger.error(
                "'vital-items' has been enabled, but the Bukkit runtime was not found on the server classpath, calling Bukkit APIs might fail.",
            )
            logger.error("Please make sure you are running 'vital-items' in the correct server environment, e.g. Spigot, Paper.")
        }

        for (vitalItem in vitalItems) {
            logger.info("Item '${vitalItem::class.java.name}' successfully registered")
        }
    }

    @Conditional(RequiresSpigot::class)
    @Configuration(proxyBeanMethods = false)
    class Spigot {
        @ConditionalOnMissingBean
        @Bean
        fun vitalItemService(vitalItems: List<VitalItem>): VitalItemService = VitalItemService(vitalItems)

        @ConditionalOnMissingBean
        @Bean
        fun vitalItemListener(vitalItemService: VitalItemService): VitalItemListener = VitalItemListener(vitalItemService)
    }
}
