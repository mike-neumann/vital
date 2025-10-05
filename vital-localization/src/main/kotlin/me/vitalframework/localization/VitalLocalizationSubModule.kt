@file:JvmName("VitalLocalizationSubModule")

package me.vitalframework.localization

import me.vitalframework.BungeePlayer
import me.vitalframework.SpigotPlayer
import me.vitalframework.Vital
import me.vitalframework.VitalCoreSubModule.Companion.logger
import me.vitalframework.VitalSubModule
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataType
import org.springframework.context.MessageSource
import org.springframework.stereotype.Component
import java.util.Locale

@Component("vital-localization")
class VitalLocalizationSubModule : VitalSubModule() {
    companion object {
        private val logger = logger()

        /**
         * Maintains a mapping of player instances to their associated locales within the Vital framework.
         *
         * This map is used to store and retrieve localization preferences for specific players. The key represents
         * the player instance, while the value represents the `Locale` assigned to that player. A value of `null`
         * indicates that no locale has been set for the corresponding player.
         *
         * This collection underpins player-specific localization features, enabling the system to adapt messages
         * and translations based on individual player locales. It supports various player implementations, such as
         * `SpigotPlayer` and `BungeePlayer`.
         *
         * Note:
         * - The map is mutable, allowing dynamic updates to player locales during runtime.
         * - Setting a player's locale to `null` effectively removes their localization preferences.
         */
        @JvmStatic
        private val playerLocales = mutableMapOf<Any, Locale?>()

        /**
         * Retrieves a localized message for the given key, arguments, and locale.
         * If the locale is null or the message cannot be resolved, the key itself is returned.
         *
         * @param locale The locale for which the message should be localized. If null, the key is returned.
         * @param key The message key used to identify the localized text.
         * @param args Arguments to format the localized text. Defaults to an empty array.
         * @return The localized message for the given key and locale, or the key itself if localization fails.
         */
        @JvmStatic
        fun getMessage(
            locale: Locale?,
            key: String,
            args: Array<Any?>,
        ): String =
            if (locale == null) {
                key
            } else {
                try {
                    Vital.context.getBean(MessageSource::class.java).getMessage(key, args, locale)
                } catch (_: Exception) {
                    key
                }
            }
    }

    object Spigot {
        /**
         * Represents the locale associated with a specific `SpigotPlayer` instance within the `Vital` framework.
         *
         * This property enables setting or retrieving the player's assigned `Locale` for localization purposes.
         * It supports features such as player-specific message translations based on their preferred or assigned language.
         *
         * Getter:
         * - Retrieves the `Locale` assigned to the `SpigotPlayer`. Returns `null` if no locale has been set.
         *
         * Setter:
         * - Assigns a new `Locale` to the `SpigotPlayer`. Setting to `null` removes the player's assigned locale.
         *
         * The underlying implementation uses an internal map to store and retrieve locale information
         * associated with `SpigotPlayer` instances.
         *
         * Note:
         * - This property facilitates the execution of localization methods, such as retrieving
         *   localized messages or formatted translations for the player.
         */
        @JvmStatic
        var SpigotPlayer.vitalLocale: Locale?
            get() = playerLocales[this]
            set(value) {
                playerLocales[this] = value

                if ("vital-items" in Vital.vitalSubModules) {
                    // update any now non-localized items
                    for (item in inventory.filter { it != null }) {
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
                                            .deserialize(getTranslatedText(localizationKey))
                                            .decoration(TextDecoration.ITALIC, false),
                                    )
                                }

                                val lore = lore()
                                if (lore != null) {
                                    lore(
                                        loreLocalizationKeys.map {
                                            MiniMessage
                                                .miniMessage()
                                                .deserialize(getTranslatedText(it))
                                                .decoration(TextDecoration.ITALIC, false)
                                        },
                                    )
                                }
                            }
                    }
                }
            }

        /**
         * Retrieves a localized and translated text for the given key based on the player's assigned locale.
         *
         * The method uses the `vitalLocale` associated with the `SpigotPlayer` instance to fetch
         * the translated message. If no arguments are provided, the method will simply return
         * the translated text associated with the key. If arguments are provided, they will be
         * used for formatting the message.
         *
         * @param key The message key used to retrieve the localized text.
         * @param args Optional arguments to format the localized text.
         * @return The translated and formatted text for the given key and arguments.
         */
        @JvmStatic
        fun SpigotPlayer.getTranslatedText(
            key: String,
            vararg args: Any?,
        ): String = getMessage(vitalLocale, key, arrayOf(*args))
    }

    object Bungee {
        /**
         * Represents the locale associated with a specific `BungeePlayer` instance within the `Vital` framework.
         *
         * This property allows for the retrieval and assignment of a player's locale, enabling features such as
         * player-specific localization and personalized message translation. By associating a `Locale` with
         * individual `BungeePlayer` instances, the localization system ensures that players receive content
         * in their preferred or assigned language.
         *
         * Getter:
         * - Retrieves the `Locale` assigned to the `BungeePlayer`. If no locale has been assigned, `null` is returned.
         *
         * Setter:
         * - Updates the player's associated locale. The value may be set to `null` to clear the assigned locale for
         *   the player.
         *
         * This property relies on an internal map to store and retrieve locale data for `BungeePlayer` instances.
         */
        @JvmStatic
        var BungeePlayer.vitalLocale: Locale?
            get() = playerLocales[this]
            set(value) {
                playerLocales[this] = value
            }

        /**
         * Retrieves the translated text for the given key based on the player's locale.
         * If the locale is not available or the translation key cannot be resolved, the key itself is returned.
         *
         * @param key The translation key used to fetch the desired localized message.
         * @param args Optional arguments to be inserted into the localized message.
         * @return A string containing the localized message if available; otherwise, the key itself.
         */
        @JvmStatic
        fun BungeePlayer.getTranslatedText(
            key: String,
            vararg args: Any?,
        ): String = getMessage(vitalLocale, key, arrayOf(*args))
    }
}
