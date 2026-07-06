@file:JvmName("VitalLocalizationModule")

package me.vitalframework.localization

import me.vitalframework.BungeePlayer
import me.vitalframework.SpigotPlayer
import me.vitalframework.Vital
import me.vitalframework.VitalCoreModule.Companion.logger
import me.vitalframework.VitalModule
import me.vitalframework.localization.VitalLocalizationModule.Spigot.vitalLocale
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

                if ("vital-items" in Vital.vitalModules) {
                    // update any now non-localized items
                    for (item in inventory.filterNotNull()) {
                        val itemLocalized =
                            item.itemMeta.persistentDataContainer[
                                NamespacedKey(
                                    "vital",
                                    "item-localized",
                                ), PersistentDataType.BOOLEAN,
                            ]
                                ?: continue

                        if (!itemLocalized) continue

                        val localizationKey =
                            item.itemMeta.persistentDataContainer[
                                NamespacedKey(
                                    "vital",
                                    "item-localization-key",
                                ),
                                PersistentDataType.STRING,
                            ]
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
                        if (loreLocalizationKeys == null) {
                            logger.warn("Item '$item' has been marked for localization but no lore localization keys are present")
                            continue
                        }

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
                                            .deserialize(t(localizationKey))
                                            .decoration(TextDecoration.ITALIC, false),
                                    )
                                }

                                val lore = lore()
                                if (lore != null) {
                                    lore(
                                        loreLocalizationKeys.map {
                                            MiniMessage
                                                .miniMessage()
                                                .deserialize(t(it))
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
