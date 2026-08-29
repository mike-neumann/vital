package dev.vitalframework.inventories

import dev.vitalframework.RequiresSpigot
import dev.vitalframework.VitalCoreModule.Companion.logger
import dev.vitalframework.VitalModule
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration

@VitalModule.Info(value = "vital-inventories")
class VitalInventoriesModule(
    val vitalInventories: List<VitalInventory>,
) : VitalModule() {
    val logger = logger()

    override fun onEnable() {
        try {
            Class.forName("org.bukkit.Bukkit")
        } catch (_: Exception) {
            logger.error(
                "'vital-inventories' has been enabled, but the Bukkit runtime was not found on the server classpath, calling Bukkit APIs might fail.",
            )
            logger.error("Please make sure you are running 'vital-inventories' in the correct server environment, e.g. Spigot, Paper.")
        }

        for (vitalInventory in vitalInventories) {
            logger.info("Inventory '${vitalInventory::class.java.name}' successfully registered")
        }
    }

    @Conditional(RequiresSpigot::class)
    @Configuration(proxyBeanMethods = false)
    class Spigot {
        @ConditionalOnMissingBean
        @Bean
        fun vitalInventoryListener(vitalInventories: List<VitalInventory>): VitalInventoryListener =
            VitalInventoryListener(vitalInventories)
    }
}
