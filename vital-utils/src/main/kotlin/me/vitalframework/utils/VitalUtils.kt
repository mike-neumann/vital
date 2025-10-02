package me.vitalframework.utils

import me.vitalframework.BungeeCommandSender
import me.vitalframework.BungeePlayer
import me.vitalframework.SpigotCommandSender
import me.vitalframework.SpigotPlayer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Difficulty
import org.bukkit.GameRule
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.inventory.CreativeCategory
import org.bukkit.inventory.MenuType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.scoreboard.Team
import org.jetbrains.annotations.Range
import java.util.UUID
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

interface VitalUtils<CS, P : CS> {
    companion object {
        /**
         * Converts the current string, assumed to be in MiniMessage format, into a component.
         *
         * This method uses the MiniMessage library to deserialize the string input,
         * transforming it into a Component instance that can be used in applications requiring
         * formatted or styled text rendering.
         *
         * @receiver The string input in MiniMessage format to be converted into a component.
         * @return The deserialized Component representation of the MiniMessage string.
         */
        fun String.toMiniMessageComponent() = MiniMessage.miniMessage().deserialize(this)

        /**
         * Converts the current Component object to a legacy Minecraft chat string representation using section symbol (§).
         *
         * The legacy string format is commonly used for in-game messages and supports color codes
         * with the section symbol (§) as the prefix. This method allows for compatibility with older
         * message and title systems that rely on the legacy formatting style.
         *
         * @receiver Component instance to be serialized into the legacy section string format.
         * @return A string representation of the component in legacy format.
         */
        fun Component.toLegacySectionString() = LegacyComponentSerializer.legacySection().serialize(this)

        /**
         * Serializes the current component into a legacy-format string using the ampersand (&) as the color code prefix.
         *
         * This method converts the `Component` into its legacy text representation, commonly used in older
         * Minecraft versions or contexts which do not support modern text styling formats like MiniMessage.
         * The ampersand character is used to indicate color and formatting codes in the resulting string.
         *
         * @receiver The `Component` instance to be serialized.
         * @return A string representing the serialized component in the legacy ampersand format.
         */
        fun Component.toLegacyAmpersandString() = LegacyComponentSerializer.legacyAmpersand().serialize(this)

        /**
         * Converts a `Component` instance to BungeeCord-compatible components.
         *
         * This function uses the BungeeComponentSerializer to serialize the
         * provided `Component` into a format that can be used with BungeeCord's chat
         * API, allowing compatibility with Spigot or Paper implementations that use
         * BungeeCord-based components for messaging.
         *
         * @receiver The `Component` to be serialized into BungeeCord-compatible components.
         * @return An array of `BaseComponent` representing the BungeeCord-compatible serialized format of the `Component`.
         */
        fun Component.toBungeeComponent() = BungeeComponentSerializer.get().serialize(this)

        /**
         * Converts the current `Component` instance into a plain text string representation.
         *
         * This method uses the `PlainTextComponentSerializer` to serialize the component,
         * effectively stripping away any formatting or additional metadata, leaving only the
         * raw text content.
         *
         * @return The plain text string representation of the component.
         */
        fun Component.toPlainTextString() = PlainTextComponentSerializer.plainText().serialize(this)

        /**
         * Converts the `Component` instance into a MiniMessage-formatted string.
         *
         * This method uses the MiniMessage serialization functionality to produce
         * a string representation of the component, preserving its formatting and style
         * as defined within the `Component`.
         *
         * @receiver The `Component` to be serialized into a MiniMessage-formatted string.
         * @return A MiniMessage-compatible string representation of the `Component`.
         */
        fun Component.toMiniMessageString() = MiniMessage.miniMessage().serialize(this)

        /**
         * Creates a chat button with hover text, clickable text, and a specific click action.
         *
         * @param hover the text to be displayed when the user hovers over the button
         * @param text the text displayed on the button
         * @param click the action or command triggered when the button is clicked
         * @param action the type of click action to be performed
         */
        fun chatButton(
            hover: String,
            text: String,
            click: String,
            action: ClickEvent.Action,
        ) = "<hover:show_text:'$hover'><click:${action.name.lowercase()}:'$click'>$text</click></hover>"

        /**
         * Creates a chat button that executes a specified command when clicked.
         *
         * @param text The display text for the button in the chat.
         * @param command The command to be executed when the button is clicked.
         */
        fun chatRunCommandButton(
            text: String,
            command: String,
        ) = chatButton(command, text, command, ClickEvent.Action.RUN_COMMAND)

        /**
         * Creates a chat button that suggests a command to the user upon clicking.
         *
         * The button is styled with the provided text and command, and clicking it suggests the command
         * in the user's chat input field. This functionality allows for streamlined interaction where
         * commands can be pre-filled for execution.
         *
         * @param text the text displayed on the button
         * @param command the command to suggest when the button is clicked
         */
        fun chatSuggestCommandButton(
            text: String,
            command: String,
        ) = chatButton(command, text, command, ClickEvent.Action.SUGGEST_COMMAND)

        /**
         * Creates a formatted "YES" button in green and bold text that, when clicked, executes a specified chat command.
         *
         * @param command The chat command to be executed when the button is clicked.
         */
        fun chatRunCommandYesButton(command: String) = chatRunCommandButton("<green><bold>YES</bold></green>", command)

        /**
         * Creates a formatted chat button with the text "NO" styled in red and bold, which executes the specified command when clicked.
         *
         * @param command The command to execute when the button is clicked.
         */
        fun chatRunCommandNoButton(command: String) = chatRunCommandButton("<red><bold>NO</bold></red>", command)

        /**
         * Creates a chat button labeled "OK" that executes the specified command when clicked.
         * The button is styled with green bold text.
         *
         * @param command The command to be executed when the "OK" button is clicked.
         */
        fun chatRunCommandOkButton(command: String) = chatRunCommandButton("<green><bold>OK</bold></green>", command)

        /**
         * Creates a red "✕" button in chat that, when clicked, executes the specified command.
         *
         * @param command The command to execute when the button is clicked.
         */
        fun chatRunCommandXButton(command: String) = chatRunCommandButton("<red><bold>✕</bold></red>", command)

        /**
         * Creates a checkmark button in chat that executes a given command when clicked.
         *
         * @param command The command to be executed when the button is clicked.
         */
        fun chatRunCommandCheckmarkButton(command: String) = chatRunCommandButton("<green><bold>✓</bold></green>", command)

        /**
         * Creates a chat button with the label "ACCEPT" styled in green and bold, which executes the specified command
         * when clicked by the player.
         *
         * @param command The command to be executed when the "ACCEPT" button is clicked.
         */
        fun chatRunCommandAcceptButton(command: String) = chatRunCommandButton("<green><bold>ACCEPT</bold></green>", command)

        /**
         * Creates a formatted decline button with a red, bold "DECLINE" label that triggers the specified command when clicked.
         *
         * @param command the command to be executed when the decline button is clicked
         */
        fun chatRunCommandDeclineButton(command: String) = chatRunCommandButton("<red><bold>DECLINE</bold></red>", command)

        /**
         * Converts the current string into a regular expression that matches the string
         * as a blacklisted word. The resulting regex is case-insensitive and allows for
         * non-alphanumeric characters between the characters of the word.
         *
         * This method is useful for detecting variations of a word that might include
         * special characters or different casing while maintaining its integrity as a match.
         *
         * @receiver The word to be converted into a regular expression.
         * @return A regular expression instance that matches the blacklisted word in
         *         case-insensitive formats, including potential variations with symbols
         *         or spaces between the characters.
         */
        fun String.toBlacklistedWordRegex() = buildBlacklistedWordRegex(this)

        /**
         * Builds a regular expression pattern to detect a given word in a case-insensitive manner,
         * ignoring special characters and non-alphanumeric symbols between characters.
         *
         * @param word The word to create a regex pattern for. It is expected to be a string
         *             representing the blacklisted word.
         * @return A compiled regular expression that matches the word case-insensitively,
         *         including possible variations that may include non-alphanumeric characters
         *         between the word's letters.
         */
        fun buildBlacklistedWordRegex(word: String): Regex {
            val pattern = word.lowercase().map { Regex.escape(it.toString()) }.joinToString("[\\W_]*")
            return Regex("\\b$pattern\\b", RegexOption.IGNORE_CASE)
        }

        /**
         * Converts the current string to a censored version by replacing all occurrences of
         * the provided blacklisted words with their censored forms.
         *
         * @param blacklistedWords A list of words that should be censored within the string.
         */
        fun String.toCensoredText(blacklistedWords: List<String>) = getCensoredText(this, blacklistedWords)

        /**
         * Replaces all occurrences of blacklisted words in the given text with a censored version.
         * Each blacklisted word is replaced with its first character followed by asterisks, e.g., "test" becomes "t***".
         * Single-character blacklisted words are replaced with a single asterisk.
         *
         * @param text The input text to be censored.
         * @param blacklistedWords A list of words that should be censored in the input text.
         * @return The censored text where all blacklisted words are replaced with their censored forms.
         */
        fun getCensoredText(
            text: String,
            blacklistedWords: List<String>,
        ): String {
            var result = text

            // cumulatively update the message for each blacklisted word.
            for (word in blacklistedWords) {
                val regex = buildBlacklistedWordRegex(word)
                result =
                    regex.replace(result) {
                        if (it.value.length > 1) {
                            // replace "test" with "t***" and "t" with "*"
                            it.value.first() + "*".repeat(it.value.length - 1)
                        } else {
                            "*".repeat(it.value.length)
                        }
                    }
            }

            return result
        }
    }

    /**
     * Broadcasts an action to a collection of players based on a specified condition.
     *
     * This method iterates through players meeting the given predicate and applies
     * the provided action to each player. It allows selective execution of actions
     * based on the predicate's evaluation.
     *
     * @param predicate A function that evaluates each player to determine if the action
     *                  should be applied. Defaults to a predicate that always returns true.
     * @param action A function representing the action to be executed for each player
     *               meeting the predicate's condition.
     */
    fun broadcastAction(
        predicate: (P) -> Boolean = { true },
        action: (P) -> Unit,
    )

    /**
     * Sends a formatted message to the current player or client system (`CS`).
     *
     * @param message The message to be sent, which supports format styling via MiniMessage.
     */
    fun CS.sendFormattedMessage(message: String)

    /**
     * Broadcasts a formatted message to all players satisfying the given predicate.
     *
     * @param message The formatted message to be broadcasted.
     * @param predicate A filter function that checks whether a player should receive the message. Defaults to a predicate that always returns true for all players.
     */
    fun broadcastFormattedMessage(
        message: String,
        predicate: (P) -> Boolean = { true },
    ) {
        broadcastAction(predicate) { it.sendFormattedMessage(message) }
    }

    /**
     * Sends a formatted title and subtitle to the current player or client system (`P`).
     *
     * This method provides a customizable title display with configurable transition timings for fading in, staying, and fading out.
     *
     * @param title The title text to be displayed. Defaults to an empty string if not provided.
     * @param subtitle The subtitle text to be displayed. Defaults to an empty string if not provided.
     * @param fadeIn The duration (in ticks) for the fade-in animation. Must be between 0 and 72,000.
     * @param stay The duration (in ticks) that the title will remain visible. Must be between 0 and 72,000.
     * @param fadeOut The duration (in ticks) for the fade-out animation. Must be between 0 and 72,000.
     */
    fun P.sendFormattedTitle(
        title: String = "",
        subtitle: String = "",
        fadeIn:
            @Range(from = 0, to = 72_000)
            Int,
        stay:
            @Range(from = 0, to = 72_000)
            Int,
        fadeOut:
            @Range(from = 0, to = 72_000)
            Int,
    )

    /**
     * Sends a formatted title and subtitle to the current player.
     * The title and subtitle are displayed in the player's title bar.
     *
     * @param title The main title text to be displayed. Defaults to an empty string.
     * @param subtitle The subtitle text to be displayed below the main title. Defaults to an empty string.
     */
    fun P.sendFormattedTitle(
        title: String = "",
        subtitle: String = "",
    )

    /**
     * Broadcasts a formatted title and subtitle to all players satisfying the given predicate.
     *
     * This method sends a formatted title and subtitle to players that meet the condition specified
     * by the provided predicate.
     *
     * @param title The main title to be displayed to the players. Defaults to an empty string.
     * @param subtitle The subtitle to be displayed under the main title. Defaults to an empty string.
     * @param predicate A filter function determining the players who should receive the formatted title.
     */
    fun broadcastFormattedTitle(
        title: String = "",
        subtitle: String = "",
        predicate: (P) -> Boolean,
    ) = broadcastAction(predicate) { it.sendFormattedTitle(title, subtitle) }

    /**
     * Broadcasts a formatted title to all eligible recipients.
     *
     * @param title The main title text to display. Defaults to an empty string if not provided.
     * @param subtitle The subtitle text to display below the title. Defaults to an empty string if not provided.
     * @param fadeIn The duration (in ticks) for the fade-in animation of the title. Must be between 0 and 72,000 inclusive.
     * @param stay The duration (in ticks) for which the title should remain visible*/
    fun broadcastFormattedTitle(
        title: String = "",
        subtitle: String = "",
        fadeIn:
            @Range(from = 0, to = 72_000)
            Int,
        stay:
            @Range(from = 0, to = 72_000)
            Int,
        fadeOut:
            @Range(from = 0, to = 72_000)
            Int,
        predicate: (P) -> Boolean = { true },
    ) = broadcastAction(predicate) { it.sendFormattedTitle(title, subtitle, fadeIn, stay, fadeOut) }

    /**
     * Sends a formatted, persistent title and subtitle to the current player or client system (`P`).
     *
     * This method is used to display a title with a subtitle that persists on the screen,
     * with a configurable fade-in duration.
     *
     * @param title The main title text to be displayed. Defaults to an empty string.
     * @param subtitle The subtitle text to be displayed below the main title. Defaults to an empty string.
     * @param fadeIn The duration (in ticks) for the fade-in animation. Must be between 0 and 72,000.
     */
    fun P.sendFormattedPersistentTitle(
        title: String = "",
        subtitle: String = "",
        fadeIn:
            @Range(from = 0, to = 72_000)
            Int,
    )

    /**
     * Sends a persistent formatted title and subtitle to all players that satisfy the specified predicate.
     * The title remains displayed with the specified fade-in duration.
     *
     * @param title The main title text to be displayed. Defaults to an empty string.
     * @param subtitle The subtitle text to be displayed below the main title. Defaults to an empty string.
     * @param fadeIn The duration (in ticks) for the fade-in animation. Must be between 0 and 72,000.
     * @param predicate A function that evaluates whether a player should receive the title.
     *                  Defaults to a predicate that always returns true for all players.
     */
    fun broadcastFormattedPersistentTitle(
        title: String = "",
        subtitle: String = "",
        fadeIn:
            @Range(from = 0, to = 72_000)
            Int,
        predicate: (P) -> Boolean = { true },
    ) = broadcastAction(predicate) { it.sendFormattedPersistentTitle(title, subtitle, fadeIn) }

    /**
     * Sends a formatted action bar message to the current player.
     *
     * The message supports formatting and is displayed as an action bar,
     * which appears above the player's hotbar for a brief duration.
     *
     * @param message The formatted message to be displayed in the action bar.
     */
    fun P.sendFormattedActionBar(message: String)

    /**
     * Broadcasts a formatted action bar message to all players satisfying the given predicate.
     *
     * This method utilizes `broadcastAction` to send a formatted action bar message
     * to players that meet the specified condition.
     *
     * @param message The formatted action bar message to be displayed.
     * @param predicate A filter function that determines which players should receive the message.
     *                  Defaults to a predicate that includes all players.
     */
    fun broadcastFormattedActionBar(
        message: String,
        predicate: (P) -> Boolean = { true },
    ) = broadcastAction(predicate) { it.sendFormattedActionBar(message) }

    /**
     * Utility object providing Spigot-specific implementations of the VitalUtils abstraction.
     *
     * This object offers a collection of methods tailored for the Spigot server API, enabling
     * formatted messaging, broadcasting actions, handling titles and action bars, and applying
     * or clearing effects on players. It defines actions for efficiently working with Spigot's
     * CommandSender and Player entities.
     */
    object Spigot : VitalUtils<SpigotCommandSender, SpigotPlayer> {
        private val customNametagIdentifier = UUID.randomUUID().toString()

        override fun broadcastAction(
            predicate: (SpigotPlayer) -> Boolean,
            action: (SpigotPlayer) -> Unit,
        ) = Bukkit
            .getOnlinePlayers()
            .filter(predicate)
            .forEach(action)

        override fun SpigotCommandSender.sendFormattedMessage(message: String) =
            spigot().sendMessage(
                // must be used since, both version (paper and spigot) support the bungeeapi implementations...
                *message.toMiniMessageComponent().toBungeeComponent(),
            )

        override fun broadcastFormattedMessage(
            message: String,
            predicate: (SpigotPlayer) -> Boolean,
        ) = broadcastAction(predicate) {
            // must be used since, both version (paper and spigot) support the bungeeapi implementations...
            it.spigot().sendMessage(*message.toMiniMessageComponent().toBungeeComponent())
        }

        /**
         * Broadcasts a sound to all players satisfying the given predicate.
         *
         * This method sends a sound effect to all players that meet the condition specified
         * by the provided predicate. The sound is played at a specified volume and pitch.
         *
         * @param sound The sound to be broadcasted to players.
         * @param volume The volume at which the sound is played. Typically ranges from 0.0 to 1.0.
         * @param pitch The pitch of the sound. Higher values produce higher-pitched sounds.
         * @param predicate A function that evaluates whether a player should receive the sound.
         *                  Defaults to a predicate that includes all players.
         */
        @JvmOverloads
        fun broadcastSound(
            sound: Sound,
            volume: Float,
            pitch: Float,
            predicate: (SpigotPlayer) -> Boolean = { true },
        ) = broadcastAction(predicate) { it.playSound(it, sound, volume, pitch) }

        /**
         * Broadcasts a sound to all players satisfying the given predicate.
         *
         * This method allows a sound to be played for a group of players based on a specified condition.
         * By default, the sound is broadcasted to all players.
         *
         * @param sound The sound to be played.
         * @param predicate A filter function to determine which players should receive the sound.
         *                  Defaults to a predicate that always returns true.
         */
        @JvmOverloads
        fun broadcastSound(
            sound: Sound,
            predicate: (SpigotPlayer) -> Boolean = { true },
        ) = broadcastSound(sound, 1f, 1f, predicate)

        override fun SpigotPlayer.sendFormattedTitle(
            title: String,
            subtitle: String,
            fadeIn:
                @Range(from = 0, to = 72_000)
                Int,
            stay:
                @Range(from = 0, to = 72_000)
                Int,
            fadeOut:
                @Range(from = 0, to = 72_000)
                Int,
        ) = sendTitle(
            title.toMiniMessageComponent().toLegacySectionString(),
            subtitle.toMiniMessageComponent().toLegacySectionString(),
            fadeIn,
            stay,
            fadeOut,
        )

        override fun SpigotPlayer.sendFormattedTitle(
            title: String,
            subtitle: String,
        ) = sendTitle(
            title.toMiniMessageComponent().toLegacySectionString(),
            subtitle.toMiniMessageComponent().toLegacySectionString(),
        )

        override fun broadcastFormattedTitle(
            title: String,
            subtitle: String,
            predicate: (SpigotPlayer) -> Boolean,
        ) = broadcastAction { it.sendFormattedTitle(title, subtitle) }

        override fun SpigotPlayer.sendFormattedPersistentTitle(
            title: String,
            subtitle: String,
            fadeIn:
                @Range(from = 0, to = 72_000)
                Int,
        ) = sendTitle(
            title.toMiniMessageComponent().toLegacySectionString(),
            subtitle.toMiniMessageComponent().toLegacySectionString(),
            fadeIn,
            72_000, // 1h
            0,
        )

        override fun broadcastFormattedPersistentTitle(
            title: String,
            subtitle: String,
            fadeIn:
                @Range(from = 0, to = 72_000)
                Int,
            predicate: (SpigotPlayer) -> Boolean,
        ) = broadcastAction { it.sendFormattedPersistentTitle(title, subtitle, fadeIn) }

        /**
         * Broadcasts a potion effect to all players who meet the specified condition.
         *
         * This method applies the specified potion effect to all players that satisfy
         * the provided predicate. The effect includes type, duration, and amplifier.
         *
         * @param potionEffectType The type of the potion effect to be applied.
         * @param duration The duration of the potion effect, in ticks.
         * @param amplifier The amplifier (or strength) of the potion effect.
         * @param playerPredicate A predicate function to filter players. Only players
         *                        satisfying this condition will receive the potion effect.
         *                        Defaults to a predicate that includes all players.
         */
        @JvmOverloads
        fun broadcastPotionEffect(
            potionEffectType: PotionEffectType,
            duration: Int,
            amplifier: Int,
            playerPredicate: (SpigotPlayer) -> Boolean = { true },
        ) = broadcastAction(playerPredicate) { it.addPotionEffect(PotionEffect(potionEffectType, duration, amplifier)) }

        /**
         * Broadcasts the removal of a specific potion effect to all players satisfying the given predicate.
         *
         * This method iterates through all online players and removes the specified potion
         * effect type from players that meet the condition defined by the provided predicate.
         *
         * @param potionEffectType The type of potion effect to be removed from the players.
         * @param playerPredicate A filter function that determines which players should have the potion effect removed.
         *                        Defaults to a predicate that includes all players.
         */
        @JvmOverloads
        fun broadcastClearPotionEffect(
            potionEffectType: PotionEffectType,
            playerPredicate: (SpigotPlayer) -> Boolean = { true },
        ) = broadcastAction(playerPredicate) { it.removePotionEffect(potionEffectType) }

        /**
         * Removes all active potion effects from players that satisfy the specified predicate.
         *
         * This method iterates through all online players and clears their active potion effects
         * if they meet the condition defined by the given predicate.
         *
         * @param playerPredicate A function that evaluates whether a player should have their potion
         *                        effects removed. Defaults to a predicate that accepts all players.
         */
        @JvmOverloads
        fun broadcastClearPotionEffects(playerPredicate: (SpigotPlayer) -> Boolean = { true }) =
            broadcastAction(playerPredicate) {
                it.activePotionEffects.map { it.type }.forEach { type: PotionEffectType -> it.removePotionEffect(type) }
            }

        override fun SpigotPlayer.sendFormattedActionBar(message: String) =
            spigot().sendMessage(
                ChatMessageType.ACTION_BAR,
                *message.toMiniMessageComponent().toBungeeComponent(),
            )

        override fun broadcastFormattedActionBar(
            message: String,
            predicate: (SpigotPlayer) -> Boolean,
        ) = broadcastAction(predicate) { it.sendFormattedActionBar(message) }

        /**
         * Teleports a player to a specified location with a temporary visual potion effect.
         *
         * This method applies a potion effect to the player, teleports them to the given location,
         * and then removes the effect immediately after teleportation.
         *
         * @param location The target location to which the player will be teleported.
         * @param potionEffectType The type of potion effect to be applied temporarily. Defaults to `PotionEffectType.SLOWNESS`.
         */
        @JvmOverloads
        fun SpigotPlayer.teleportWithEffect(
            location: Location,
            potionEffectType: PotionEffectType = PotionEffectType.SLOWNESS,
        ) {
            removePotionEffect(potionEffectType)
            addPotionEffect(PotionEffect(potionEffectType, 2, Int.Companion.MAX_VALUE))
            teleport(location)
            removePotionEffect(potionEffectType)
        }

        /**
         * Teleports the player to the specified entity's location while applying a visual effect.
         *
         * @param to The target entity whose location the player will be teleported to.
         */
        fun SpigotPlayer.teleportWithEffect(to: Entity) = teleportWithEffect(to.location, PotionEffectType.SLOWNESS)

        /**
         * Determines if the current material can be placed in mid-air without support.
         *
         * This method checks specific conditions to decide whether a material is capable of being placed
         * in mid-air. A material can be placed in mid-air if it:
         * - Does not have gravity.
         * - Is not classified as vegetation (checked via the `isVegetation` method).
         * - Is not one of the following disallowed materials:
         *   - REDSTONE
         *   - REDSTONE_TORCH
         *   - REPEATER
         *   - COMPARATOR
         *   - LEVER
         *   - TRIPWIRE
         * - Does not have a name containing "BUTTON", "PRESSURE_PLATE", or "RAIL".
         *
         * @return `true` if the material can be placed in mid-air, otherwise `false`.
         */
        fun Material.canBePlacedInMidAir() =
            !hasGravity() &&
                !isVegetation() &&
                (
                    this != Material.REDSTONE &&
                        this != Material.REDSTONE_TORCH &&
                        this != Material.REPEATER &&
                        this != Material.COMPARATOR &&
                        this != Material.LEVER &&
                        this != Material.TRIPWIRE &&
                        !name.contains("BUTTON") &&
                        !name.contains("PRESSURE_PLATE") &&
                        !name.contains("RAIL")
                )

        /**
         * Checks if the material is related to vegetation.
         *
         * This function evaluates whether the material name contains specific keywords
         * like "SAPLING", "FLOWER", "CROP", or others commonly associated with vegetation,
         * or if it matches one of several predefined material constants representing vegetation.
         *
         * @receiver The material to be evaluated.
         * @return True if the material is considered vegetation, otherwise false.
         */
        fun Material.isVegetation() =
            name.contains("SAPLING") ||
                name.contains("FLOWER") ||
                name.contains("WHEAT") ||
                name.contains("SEEDS") ||
                name.contains("CROP") ||
                name.contains("KELP") ||
                name.contains("BUSH") ||
                name.contains("MUSHROOM") ||
                name.contains("CHORUS") ||
                name.contains("FERN") ||
                name.contains("POTTED") ||
                name.contains("ROSE") ||
                name.contains("POPPY") ||
                this == Material.MELON_STEM ||
                this == Material.PUMPKIN_STEM ||
                this == Material.BAMBOO ||
                this == Material.SUGAR_CANE ||
                this == Material.SEA_PICKLE ||
                this == Material.NETHER_WART ||
                this == Material.LILY_PAD ||
                this == Material.VINE ||
                this == Material.GLOW_LICHEN ||
                this == Material.SCULK_VEIN ||
                this == Material.CACTUS ||
                this == Material.LILAC ||
                this == Material.PEONY ||
                this == Material.TALL_GRASS ||
                this == Material.TALL_SEAGRASS ||
                this == Material.MANGROVE_PROPAGULE

        /**
         * Determines if the current Material instance represents a redstone-related machine or component.
         *
         * This method evaluates whether the material belongs to the redstone creative category,
         * and matches specific materials or naming conventions associated with redstone machines.
         *
         * A material is considered a redstone machine if:
         * - It belongs to the `CreativeCategory.REDSTONE`.
         * - It explicitly matches specific redstone-related materials (e.g., `REDSTONE_TORCH`, `COMPARATOR`, `REPEATER`, etc.).
         * - Its name contains certain keywords such as "PISTON", "BUTTON", "PRESSURE_PLATE", "DETECTOR", or "LAMP".
         *
         * @return `true` if the material is a redstone machine, otherwise `false`.
         */
        fun Material.isRedstoneMachine() =
            creativeCategory == CreativeCategory.REDSTONE &&
                (
                    this == Material.REDSTONE_TORCH ||
                        name.contains("PISTON") ||
                        name.contains("BUTTON") ||
                        name.contains("PRESSURE_PLATE") ||
                        name.contains("DETECTOR") ||
                        name.contains("LAMP") ||
                        this == Material.COMPARATOR ||
                        this == Material.REPEATER ||
                        this == Material.REDSTONE ||
                        this == Material.REDSTONE_WIRE ||
                        this == Material.OBSERVER ||
                        this == Material.DROPPER ||
                        this == Material.DISPENSER ||
                        this == Material.HOPPER ||
                        this == Material.HOPPER_MINECART
                )

        /**
         * Determines if the current location is inside a defined 3D rectangular area.
         *
         * @param location1 One corner of the 3D rectangular area.
         * @param location2 The opposite corner of the 3D rectangular area.
         * @return True if the current location is within or on the boundaries of the defined area, false otherwise.
         */
        fun Location.isInsideLocationArea(
            location1: Location,
            location2: Location,
        ): Boolean {
            val ourMinX = min(location1.x, location2.x)
            val ourMaxX = max(location1.x, location2.x)
            val ourMinY = min(location1.y, location2.y)
            val ourMaxY = max(location1.y, location2.y)
            val ourMinZ = min(location1.z, location2.z)
            val ourMaxZ = max(location1.z, location2.z)
            val theirX = x
            val theirY = y
            val theirZ = z

            return theirX >= ourMinX &&
                theirX <= ourMaxX &&
                theirY >= ourMinY &&
                theirY <= ourMaxY &&
                theirZ >= ourMinZ &&
                theirZ <= ourMaxZ
        }

        /**
         * Generates a random location within a rectangular area defined by two corner locations.
         *
         * The area is determined by the two given locations (`location1` and `location2`),
         * which represent opposite corners of a cuboid in 3D space. The resulting random
         * location is inside the bounds defined by the minimum and maximum x, y, and z coordinates.
         *
         * @param location1 One corner of the location area.
         * @param location2 The opposite corner of the location area.
         * @return A random `Location` object within the specified area, retaining the `world`
         *         of the first provided location (`location1`).
         */
        fun getRandomLocationInLocationArea(
            location1: Location,
            location2: Location,
        ): Location {
            val ourMinX = min(location1.x, location2.x)
            val ourMaxX = max(location1.x, location2.x)
            val ourMinY = min(location1.y, location2.y)
            val ourMaxY = max(location1.y, location2.y)
            val ourMinZ = min(location1.z, location2.z)
            val ourMaxZ = max(location1.z, location2.z)
            val randomX = Random.nextDouble(ourMinX, ourMaxX)
            val randomY = Random.nextDouble(ourMinY, ourMaxY)
            val randomZ = Random.nextDouble(ourMinZ, ourMaxZ)

            return Location(location1.world, randomX, randomY, randomZ)
        }

        /**
         * Calculates the center of the block for the given location, with optional offsets.
         *
         * This method returns a new `Location` object centered within the current block,
         * applying the given offsets on the x, y, and z axes. The pitch, yaw, and direction
         * of the current location are retained in the resulting location.
         *
         * @param xOffset The offset to apply along the x-axis relative to the block's center.
         * @param yOffset The offset to apply along the y-axis relative to the block's center.
         * @param zOffset The offset to apply along the z-axis relative to the block's center.
         * @return A `Location` object representing the center of the block with the applied offsets.
         */
        fun Location.getCenterBlockLocation(
            xOffset: Double,
            yOffset: Double,
            zOffset: Double,
        ): Location {
            val finalLocation =
                block.location
                    .clone()
                    .add(.5, .5, .5)
                    .add(xOffset, yOffset, zOffset)

            finalLocation.pitch = pitch
            finalLocation.yaw = yaw
            finalLocation.direction = direction

            return finalLocation
        }

        /**
         * Returns a Location object that represents the center of the block
         * of the current location. The method adjusts the current location's
         * coordinates to align it to the exact center of the block it belongs to.
         *
         * This method includes optional offsets in the x, y, and z axes, which
         * can be used to shift the center position accordingly.
         *
         * @receiver The current Location object.
         * @return A new Location object representing the adjusted center block location.
         */
        fun Location.getCenterBlockLocation() = getCenterBlockLocation(0.0, 0.0, 0.0)

        /**
         * Calculates the top center location of the block the current location belongs to.
         *
         * This function returns a `Location` object that represents the center point
         * of the block at the top surface by internally leveraging the `getCenterBlockLocation` method.
         * It applies an offset to position the resulting location slightly above the center of the block.
         *
         * @receiver The current location for which the center block's top location is to be calculated.
         * @return A `Location` object representing the center of the top of the block.
         */
        fun Location.getCenterBlockTopLocation() = getCenterBlockLocation(0.0, .5, 0.0)

        /**
         * Retrieves the center block side location of a `Location` with specific offsets.
         *
         * This method calculates a new `Location` by centering it within the current block
         * and applying predefined x, y, and z offsets. It preserves the original pitch, yaw,
         * and direction of the `Location`.
         *
         * @return A `Location` object positioned at the center block side with the applied offsets.
         */
        fun Location.getCenterBlockSideLocation() = getCenterBlockLocation(0.0, -.5, 0.0)

        /**
         * Calculates the volume of a rectangular cuboid defined by two locations.
         *
         * @param pos1 The first corner of the cuboid.
         * @param pos2 The opposite corner of the cuboid.
         * @return The volume of the cuboid in block units.
         */
        fun calculateVolume(
            pos1: Location,
            pos2: Location,
        ): Int {
            val dx = abs(pos1.blockX - pos2.blockX) + 1
            val dy = abs(pos1.blockY - pos2.blockY) + 1
            val dz = abs(pos1.blockZ - pos2.blockZ) + 1
            return dx * dy * dz
        }

        /**
         * Generates a sequence of all block locations within the cuboid defined by two corner locations.
         * The locations must belong to the same world, and the sequence is generated
         * from the minimum coordinates to the maximum coordinates of the defined cuboid.
         *
         * @param pos1 The first corner location of the cuboid.
         * @param pos2 The second corner location of the cuboid.
         * @return A sequence of block locations within the cuboid.
         * @throws IllegalArgumentException if the locations do not belong to the same world, or if one of the locations has no world.
         */
        fun getVolumeLocations(
            pos1: Location,
            pos2: Location,
        ): Sequence<Location> {
            val world = pos1.world ?: throw IllegalArgumentException("Location 1 has no world")
            require(world == pos2.world) { "Locations must be in the same world" }

            val minX = minOf(pos1.blockX, pos2.blockX)
            val maxX = maxOf(pos1.blockX, pos2.blockX)
            val minY = minOf(pos1.blockY, pos2.blockY)
            val maxY = maxOf(pos1.blockY, pos2.blockY)
            val minZ = minOf(pos1.blockZ, pos2.blockZ)
            val maxZ = maxOf(pos1.blockZ, pos2.blockZ)

            return (minX..maxX).asSequence().flatMap { x ->
                (minY..maxY).asSequence().flatMap { y ->
                    (minZ..maxZ).asSequence().map { z ->
                        Location(world, x.toDouble(), y.toDouble(), z.toDouble())
                    }
                }
            }
        }

        /**
         * Calculates the total number of edges for a cuboid defined by two 3D locations.
         *
         * This method determines the distances between the two positions in each dimension,
         * computes the edges along each dimension, and adds the number of corners.
         *
         * @param pos1 The first location defining one corner of the cuboid.
         * @param pos2 The second location defining the opposite corner of the cuboid.
         * @return The total number of edges, including all dimensions and corners.
         */
        fun calculateEdges(
            pos1: Location,
            pos2: Location,
        ): Int {
            val dx = abs(pos1.blockX - pos2.blockX) + 1
            val dy = abs(pos1.blockY - pos2.blockY) + 1
            val dz = abs(pos1.blockZ - pos2.blockZ) + 1

            // If any dimension is 1, treat it specially to avoid negative or zero edges
            val edgeX = if (dx > 1) 4 * (dx - 1) else 0
            val edgeY = if (dy > 1) 4 * (dy - 1) else 0
            val edgeZ = if (dz > 1) 4 * (dz - 1) else 0

            val corners = 8
            return edgeX + edgeY + edgeZ + corners
        }

        /**
         * Calculates and returns the edge locations of a cuboid defined by two diagonal corners.
         *
         * The method identifies all the points along the edges of the cuboid between the two given
         * locations. Only the unique points are included in the resulting sequence.
         *
         * Both input locations must be in the same world, otherwise an exception is thrown.
         *
         * @param pos1 The first corner location of the cuboid.
         * @param pos2 The second corner location of the cuboid.
         * @return A sequence of unique locations representing the edges of the cuboid.
         * @throws IllegalArgumentException if the locations are in different worlds, or if the first location has no world.
         */
        fun getEdgeLocations(
            pos1: Location,
            pos2: Location,
        ): Sequence<Location> {
            val world = pos1.world ?: throw IllegalArgumentException("Location 1 has no world")
            require(world == pos2.world) { "Locations must be in the same world" }

            val minX = minOf(pos1.blockX, pos2.blockX)
            val maxX = maxOf(pos1.blockX, pos2.blockX)
            val minY = minOf(pos1.blockY, pos2.blockY)
            val maxY = maxOf(pos1.blockY, pos2.blockY)
            val minZ = minOf(pos1.blockZ, pos2.blockZ)
            val maxZ = maxOf(pos1.blockZ, pos2.blockZ)

            fun loc(
                x: Int,
                y: Int,
                z: Int,
            ) = Location(world, x.toDouble(), y.toDouble(), z.toDouble())

            val edges =
                sequence {
                    // Edges along X axis (4 edges)
                    for (x in minX..maxX) {
                        yield(loc(x, minY, minZ))
                        yield(loc(x, minY, maxZ))
                        yield(loc(x, maxY, minZ))
                        yield(loc(x, maxY, maxZ))
                    }

                    // Edges along Y axis (4 edges)
                    for (y in minY..maxY) {
                        yield(loc(minX, y, minZ))
                        yield(loc(minX, y, maxZ))
                        yield(loc(maxX, y, minZ))
                        yield(loc(maxX, y, maxZ))
                    }

                    // Edges along Z axis (4 edges)
                    for (z in minZ..maxZ) {
                        yield(loc(minX, minY, z))
                        yield(loc(minX, maxY, z))
                        yield(loc(maxX, minY, z))
                        yield(loc(maxX, maxY, z))
                    }
                }

            // Remove duplicates (some corners can appear multiple times)
            return edges.distinct()
        }

        /**
         * Calculates the surface area of a cuboid region defined by two locations.
         *
         * The calculation considers the total exposed surface area of the cuboid
         * formed by the two locations, including all six faces.
         *
         * @param pos1 One corner of the cuboid region.
         * @param pos2 The opposite corner of the cuboid region.
         * @return The total surface area of the cuboid region.
         */
        fun calculateSurfaceArea(
            pos1: Location,
            pos2: Location,
        ): Int {
            val dx = abs(pos1.blockX - pos2.blockX) + 1
            val dy = abs(pos1.blockY - pos2.blockY) + 1
            val dz = abs(pos1.blockZ - pos2.blockZ) + 1

            val areaXY = dx * dy
            val areaXZ = dx * dz
            val areaYZ = dy * dz

            return 2 * (areaXY + areaXZ + areaYZ)
        }

        /**
         * Calculates the locations on the surface of the cuboid defined by two positions.
         *
         * The method returns a sequence of all `Location` objects that lie on the surface
         * of the cuboid. The surface includes all blocks on any of the six faces.
         *
         * @param pos1 The first corner of the cuboid. Must belong to the same world as `pos2`.
         * @param pos2 The second corner of the cuboid. Must belong to the same world as `pos1`.
         * @return A sequence of `Location` objects that represent all positions on the surface of the cuboid.
         * @throws IllegalArgumentException if either `pos1` or `pos2` does not have an associated world,
         * or if the two locations belong to different worlds.
         */
        fun getSurfaceAreaLocations(
            pos1: Location,
            pos2: Location,
        ): Sequence<Location> {
            val world = pos1.world ?: throw IllegalArgumentException("Location 1 has no world")
            require(world == pos2.world) { "Locations must be in the same world" }

            val minX = minOf(pos1.blockX, pos2.blockX)
            val maxX = maxOf(pos1.blockX, pos2.blockX)
            val minY = minOf(pos1.blockY, pos2.blockY)
            val maxY = maxOf(pos1.blockY, pos2.blockY)
            val minZ = minOf(pos1.blockZ, pos2.blockZ)
            val maxZ = maxOf(pos1.blockZ, pos2.blockZ)

            fun loc(
                x: Int,
                y: Int,
                z: Int,
            ) = Location(world, x.toDouble(), y.toDouble(), z.toDouble())

            val xRange = minX..maxX
            val yRange = minY..maxY
            val zRange = minZ..maxZ

            // Surface is blocks on any face:
            // X == minX or maxX OR
            // Y == minY or maxY OR
            // Z == minZ or maxZ

            return sequence {
                for (x in xRange) {
                    for (y in yRange) {
                        for (z in zRange) {
                            if (x == minX ||
                                x == maxX ||
                                y == minY ||
                                y == maxY ||
                                z == minZ ||
                                z == maxZ
                            ) {
                                yield(loc(x, y, z))
                            }
                        }
                    }
                }
            }
        }

        /**
         * Configures the game rules and environment settings for a `World` to align with a controlled or static gameplay setup.
         *
         * This method adjusts several game rules to disable features such as day-night cycling, fire spread, mob behavior,
         * and weather changes. It also enables `KEEP_INVENTORY`, sets the time to 0, enforces peaceful difficulty, and clears
         * weather effects to create a more predictable or controlled world state.
         *
         * The following properties are managed:
         * - Game rules such as mob spawning, fire spread, daylight cycle, and more are turned off.
         * - Inventory preservation is enabled (`KEEP_INVENTORY` set to `true`).
         * - The world's time is reset to 0, ensuring consistent lighting.
         * - Difficulty is set to `PEACEFUL`, preventing hostile mobs from spawning naturally.
         * - Weather effects are disabled by clearing weather duration.
         */
        fun World.cleanGameRules() {
            setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
            setGameRule(GameRule.DO_FIRE_TICK, false)
            setGameRule(GameRule.DO_MOB_SPAWNING, false)
            setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)
            setGameRule(GameRule.DO_MOB_LOOT, false)
            setGameRule(GameRule.DO_MOB_SPAWNING, false)
            setGameRule(GameRule.DO_TRADER_SPAWNING, false)
            setGameRule(GameRule.DO_VINES_SPREAD, false)
            setGameRule(GameRule.MOB_GRIEFING, false)
            setGameRule(GameRule.SHOW_DEATH_MESSAGES, false)
            setGameRule(GameRule.KEEP_INVENTORY, true)
            setGameRule(GameRule.DISABLE_RAIDS, true)
            setGameRule(GameRule.DO_WEATHER_CYCLE, false)

            time = 0
            difficulty = Difficulty.PEACEFUL
            weatherDuration = 0
        }

        /**
         * Generates a random location within the valid world boundary and returns it.
         *
         * This method selects random coordinates within the permissible range of a Minecraft
         * world (-29,999,984 to 29,999,984 for x and z, and -256 to 256 for y) and retrieves
         * the corresponding location after ensuring that the block exists within the world.
         *
         * @receiver The world in which the random location is to be generated.
         * @return A random location within the world boundary.
         */
        fun World.getRandomLocation() =
            getBlockAt(
                (-29_999_984..29_999_984).random(),
                (-256..256).random(),
                (-29_999_984..29_999_984).random(),
            ).location

        /**
         * Determines the highest non-air block location from the specified start height and returns a safe location above it.
         *
         * @param startFrom The y-coordinate to start the search from, defaulting to 256. The method iterates downward from this height.
         * @return The location of the highest safe block, which is two blocks above the first non-air block encountered.
         */
        @JvmOverloads
        fun Location.getHighestSafeLocationFromTop(startFrom: Int = 256): Location {
            var y = startFrom
            do {
                y--
            } while (world!!.getBlockAt(x.toInt(), y, z.toInt()).type == Material.AIR)

            return world!!.getBlockAt(x.toInt(), y + 2, z.toInt()).location
        }

        /**
         * Finds the highest safe location starting from a specified point and moving upward.
         * A location is considered safe if the block at the level and the block above it are both air.
         *
         * @param startFrom The y-coordinate from which to begin the search. Defaults to 50.
         * @return The highest safe `Location` instance found above or at the starting position.
         */
        @JvmOverloads
        fun Location.getHighestSafeLocationFromBottom(startFrom: Int = 50): Location {
            var safeY = startFrom
            do {
                safeY++
            } while (world!!.getBlockAt(x.toInt(), safeY, z.toInt()).type != Material.AIR &&
                world!!.getBlockAt(x.toInt(), safeY + 1, z.toInt()).type != Material.AIR
            )

            return Location(world, x, safeY.toDouble(), z)
        }

        /**
         * Cleans and configures game rules for a specified world to ensure a controlled gameplay setup.
         *
         * This method ensures the specified world aligns with predefined game rules and environment settings by invoking
         * `cleanGameRules` on the world. If the world does not exist, an exception is thrown.
         *
         * @param worldName The name of the world whose game rules are being cleaned and configured.
         *                  If the world doesn't exist, a runtime exception is thrown.
         */
        fun cleanGameRules(worldName: String) =
            Bukkit.getWorld(worldName)?.cleanGameRules()
                ?: throw RuntimeException("World '$worldName' does not exist")

        /**
         * Opens a custom inventory menu for the player based on the provided menu type.
         *
         * @param menuType The type of custom menu to be displayed, represented as a MenuType.Typed instance.
         */
        @Suppress("UnstableApiUsage")
        fun SpigotPlayer.openInventory(menuType: MenuType.Typed<*, *>) = openInventory(menuType.create(this))

        /**
         * Retrieves or creates a custom nametag team for the player in the specified scoreboard.
         *
         * This function constructs a unique team name using the player's unique ID, a custom identifier,
         * and a sorting key. If a team with the constructed name does not already exist in the scoreboard,
         * it registers a new team and applies the provided prefix and suffix to it.
         *
         * @param scoreboard The scoreboard in which the team will be retrieved or created.
         * @param sort A sorting key used as part of the team's unique name, will determine the sorting from a - z in tab.
         * @param prefix An optional component to set as the team's prefix. Defaults to null.
         * @param suffix An optional component to set as the team's suffix. Defaults to null.
         * @return The custom `Team` associated with the player, either newly created or existing.
         */
        fun SpigotPlayer.getCustomNametagTeam(
            scoreboard: Scoreboard,
            sort: String,
            prefix: Component? = null,
            suffix: Component? = null,
        ): Team {
            val teamName = "${sort}_${uniqueId}__$customNametagIdentifier"
            return (scoreboard.getTeam(teamName) ?: scoreboard.registerNewTeam(teamName)).apply {
                prefix(prefix)
                suffix(suffix)
            }
        }

        /**
         * Retrieves or creates a custom nametag team for the player in the specified scoreboard.
         *
         * This function constructs a unique team name using the player's unique ID, a custom identifier,
         * and a sorting key. If a team with the constructed name does not already exist in the scoreboard,
         * it registers a new team and applies the provided prefix and suffix to it.
         *
         * @param scoreboard The scoreboard in which the team will be retrieved or created.
         * @param sort A sorting key used as part of the team's unique name, will determine the sorting from a - z in tab.
         * @param prefix An optional minimessage string to set as the team's prefix. Defaults to null.
         * @param suffix An optional minimessage string to set as the team's suffix. Defaults to null.
         * @return The custom `Team` associated with the player, either newly created or existing.
         */
        fun SpigotPlayer.getCustomNametagTeam(
            scoreboard: Scoreboard,
            sort: String,
            prefix: String? = null,
            suffix: String? = null,
        ) = getCustomNametagTeam(scoreboard, sort, prefix?.toMiniMessageComponent(), suffix?.toMiniMessageComponent())

        /**
         * Sets a custom nametag for a `SpigotPlayer` instance, along with managing the scoreboard teams
         * required to properly display the nametag information.
         *
         * This method creates a team on the player's scoreboard with the specified prefix, suffix,
         * and sorting value. It also ensures visibility of other players' nametags by synchronizing
         * all custom nametags across players on the server.
         *
         * @param sort A string value representing the sort order for the custom nametag, used to organize teams.
         * @param prefix An optional minimessage string to be displayed as the prefix of the player's nametag. Defaults to `null`.
         * @param suffix An optional minimessage string to be displayed as the suffix of the player's nametag. Defaults to `null`.
         */
        fun SpigotPlayer.setCustomNametag(
            sort: String,
            prefix: String? = null,
            suffix: String? = null,
        ) {
            // first, we need to create a team on our own scoreboard.
            // this will contain the data we set while calling this function.
            val ourTeam = getCustomNametagTeam(scoreboard, sort, prefix, suffix)
            ourTeam.addPlayer(this)

            // now we have at least ourselves on our scoreboard.
            // to also include everyone else, we need to grab the custom nametags of everyone else...
            val otherPlayers = Bukkit.getOnlinePlayers().filter { it.uniqueId != uniqueId }
            for (otherPlayer in otherPlayers) {
                // try to grab the custom nametag team of the other player in this iteration...
                // if the other does NOT have a custom nametag team, this iteration is skipped
                val otherTeam =
                    otherPlayer.scoreboard.teams.firstOrNull {
                        it.name.endsWith(customNametagIdentifier) && it.hasPlayer(otherPlayer)
                    } ?: continue

                // at this point we have the team of the other player's custom nametag.
                // we can now extract the values we need to construct a new team on OUR own scoreboard.
                // this will then reflect the data of the other player for us to see...
                // By standard, the first value before the initial "_" will ALWAYS be the sorting value,
                // so we can safely extract this by splitting the string...
                val otherTeamSort = otherTeam.name.substringBefore("_")

                // we can now construct the other player's custom nametag on our own scoreboard...
                val ourOtherTeam = otherPlayer.getCustomNametagTeam(scoreboard, otherTeamSort, otherTeam.prefix(), otherTeam.suffix())
                ourOtherTeam.addPlayer(otherPlayer)
            }
        }
    }

    /**
     * Object representing essential utility functions for managing BungeeCord players and commands.
     *
     * This object provides methods for broadcasting messages, sending formatted titles, messages, and action bar notifications
     * specific to the BungeeCord environment. It operates on `BungeeCommandSender` and `BungeePlayer` types,
     * enabling server-wide or player-specific interactions.
     */
    object Bungee : VitalUtils<BungeeCommandSender, BungeePlayer> {
        override fun broadcastAction(
            predicate: (BungeePlayer) -> Boolean,
            action: (BungeePlayer) -> Unit,
        ) = ProxyServer
            .getInstance()
            .players
            .filter(predicate)
            .forEach(action)

        override fun BungeeCommandSender.sendFormattedMessage(message: String) =
            sendMessage(*message.toMiniMessageComponent().toBungeeComponent())

        override fun BungeePlayer.sendFormattedTitle(
            title: String,
            subtitle: String,
            fadeIn:
                @Range(from = 0, to = 72_000)
                Int,
            stay:
                @Range(from = 0, to = 72_000)
                Int,
            fadeOut:
                @Range(from = 0, to = 72_000)
                Int,
        ) = sendTitle(
            ProxyServer
                .getInstance()
                .createTitle()
                .title(TextComponent.fromLegacy(title.toMiniMessageComponent().toLegacySectionString()))
                .subTitle(TextComponent.fromLegacy(subtitle.toMiniMessageComponent().toLegacySectionString()))
                .fadeIn(fadeIn)
                .stay(stay)
                .fadeOut(fadeOut),
        )

        override fun BungeePlayer.sendFormattedTitle(
            title: String,
            subtitle: String,
        ) = sendTitle(
            ProxyServer
                .getInstance()
                .createTitle()
                .title(TextComponent.fromLegacy(title.toMiniMessageComponent().toLegacySectionString()))
                .subTitle(TextComponent.fromLegacy(subtitle.toMiniMessageComponent().toLegacySectionString())),
        )

        override fun BungeePlayer.sendFormattedPersistentTitle(
            title: String,
            subtitle: String,
            fadeIn:
                @Range(from = 0, to = 72_000)
                Int,
        ) = sendFormattedTitle(title, subtitle, fadeIn, 72_000, 0)

        override fun BungeePlayer.sendFormattedActionBar(message: String) =
            sendMessage(
                ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacy(message.toMiniMessageComponent().toLegacySectionString()),
            )

        override fun broadcastFormattedTitle(
            title: String,
            subtitle: String,
            predicate: (BungeePlayer) -> Boolean,
        ) = broadcastAction(predicate) { it.sendFormattedTitle(title, subtitle) }
    }
}
