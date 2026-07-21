@file:JvmName("VitalLocalizationModule")

package dev.vitalframework.localization

import dev.vitalframework.BungeePlayer
import dev.vitalframework.SpigotPlayer
import dev.vitalframework.Vital
import dev.vitalframework.VitalCoreModule.Companion.logger
import dev.vitalframework.VitalModule
import dev.vitalframework.localization.VitalLocalizationModule.Spigot.vitalLocale
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataType
import org.springframework.beans.factory.getBean
import org.springframework.context.MessageSource
import java.util.Locale

@VitalModule.Info(value = "vital-localization")
class VitalLocalizationModule : VitalModule() {
    companion object {
        private val logger = logger()

        /**
         * Internal storage; all players and their currently selected locale are stored here.
         * This [Map] controls what message bundle the [t] function uses to fetch any localized messages.
         */
        @JvmStatic
        internal val playerLocales = mutableMapOf<Any, Locale?>()

        /**
         * Gets the localized version of the given [key] based on the given [locale].
         * If the given [key] does not resolve to any known localized text in a message bundle, this function returns the [key].
         * This function will never return `null`.
         *
         * Any passes [args] will allow you to format your message with dynamic data.
         * E.g.,
         * ```java
         * // myserver.item.name=My item {0}
         *
         * VitalLocalizationModule.t("myserver.item.name", "VALUE") -> My item VALUE
         * ```
         */
        @JvmStatic
        fun t(
            locale: Locale?,
            key: String,
            vararg args: Any?,
        ): String =
            if (locale == null) {
                key
            } else {
                try {
                    Vital.context.getBean<MessageSource>().getMessage(key, arrayOf(*args), locale)
                } catch (_: Exception) {
                    key
                }
            }
    }

    object Spigot {
        @JvmStatic
        var SpigotPlayer.vitalLocale: Locale?
            get() = playerLocales[this]
            set(value) {
                playerLocales[this] = value

                val loggingContext = "Context: player '$this', new locale '$value'."

                logger.debug("Player vital localization was changed. Will update inventory items viable for localization. $loggingContext")
                if ("vital-items" in Vital.vitalModules) {
                    logger.debug(
                        "The 'vital-items' module was not found, cannot update item locales for player and new locale. $loggingContext",
                    )
                    // update any now non-localized items
                    for (item in inventory.filterNotNull()) {
                        val itemLocalized =
                            item.itemMeta.persistentDataContainer[
                                NamespacedKey(
                                    "vital",
                                    "item-localized",
                                ), PersistentDataType.BOOLEAN,
                            ]
                        if (itemLocalized == null || !itemLocalized) {
                            logger.debug(
                                "Cannot update locale of item '$item' for player. Items must have a namespaced key 'vital:item-localized' to be viable for localization. $loggingContext",
                            )
                            continue
                        }

                        logger.debug("Item '$item' is viable for localization, extracting required values. $loggingContext")
                        val localizationKey =
                            item.itemMeta.persistentDataContainer[
                                NamespacedKey(
                                    "vital",
                                    "item-localization-key",
                                ),
                                PersistentDataType.STRING,
                            ]
                        logger.debug("Name localization key '$localizationKey'. $loggingContext")
                        if (localizationKey == null) {
                            logger.warn("Item '$item' has been marked for localization but no localization key is present")
                            continue
                        }

                        val loreLocalizationKeys =
                            item.itemMeta.persistentDataContainer[
                                NamespacedKey(
                                    "vital",
                                    "item-lore-localization-keys",
                                ),
                                PersistentDataType.LIST.strings(),
                            ]
                        logger.debug("Lore localization keys '$loreLocalizationKeys'. $loggingContext")
                        if (loreLocalizationKeys == null) {
                            logger.warn("Item '$item' has been marked for localization but no lore localization keys are present")
                            continue
                        }

                        val translatedName = t(localizationKey)
                        val translatedLore = loreLocalizationKeys.map { t(it) }
                        logger.debug(
                            "Applying item localization for item '$item' with name localization key '$localizationKey' to '$translatedName' and lore localization keys '$loreLocalizationKeys' to '$translatedLore'. $loggingContext",
                        )

                        // we have an item that is set to be localized
                        // update only its name and lore now.
                        // we want to set the same item state, only its name and lore should be touched.
                        item.itemMeta =
                            item.itemMeta.apply {
                                val displayName = displayName()
                                if (displayName != null) {
                                    displayName(
                                        MiniMessage
                                            .miniMessage()
                                            .deserialize(translatedName)
                                            .decoration(TextDecoration.ITALIC, false),
                                    )
                                }

                                val lore = lore()
                                if (lore != null) {
                                    lore(
                                        translatedLore.map {
                                            MiniMessage
                                                .miniMessage()
                                                .deserialize(it)
                                                .decoration(TextDecoration.ITALIC, false)
                                        },
                                    )
                                }
                            }
                    }
                }
            }

        /**
         * Gets the localized version of the given [key] based on the given player's currently selected locale in [vitalLocale].
         * If the given [key] does not resolve to any known localized text in a message bundle, this function returns the [key].
         * This function will never return `null`.
         *
         * Any passes [args] will allow you to format your message with dynamic data.
         * E.g.,
         * ```java
         * // "myserver.item.name=My item {0}"
         *
         * VitalLocalizationModule.Spigot.INSTANCE.t(myPlayer, "myserver.item.name", "VALUE") -> My item VALUE
         * ```
         */
        @JvmStatic
        fun SpigotPlayer.t(
            key: String,
            vararg args: Any?,
        ): String = t(vitalLocale, key, *args)
    }

    object Bungee {
        @JvmStatic
        var BungeePlayer.vitalLocale: Locale?
            get() = playerLocales[this]
            set(value) {
                playerLocales[this] = value
            }

        /**
         * Gets the localized version of the given [key] based on the given player's currently selected locale in [vitalLocale].
         * If the given [key] does not resolve to any known localized text in a message bundle, this function returns the [key].
         * This function will never return `null`.
         *
         * Any passes [args] will allow you to format your message with dynamic data.
         * E.g.,
         * ```java
         * // "myserver.item.name=My item {0}"
         *
         * VitalLocalizationModule.Bungee.INSTANCE.t(myPlayer, "myserver.item.name", "VALUE"); -> My item VALUE
         * ```
         */
        @JvmStatic
        fun BungeePlayer.t(
            key: String,
            vararg args: Any?,
        ): String = t(vitalLocale, key, *args)
    }
}
