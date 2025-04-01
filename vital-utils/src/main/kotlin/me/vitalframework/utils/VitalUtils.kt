package me.vitalframework.utils

import me.vitalframework.*
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.*
import org.bukkit.entity.Entity
import org.bukkit.inventory.CreativeCategory
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.jetbrains.annotations.Range
import java.util.*
import kotlin.math.max
import kotlin.math.min

interface VitalUtils<CS, P : CS> {
    companion object {
        fun chatButton(hover: String, text: String, click: String, action: ClickEvent.Action) =
            "<hover:show_text:'$hover'><click:${action.name.lowercase()}:'$click'>$text</click></hover>"

        fun chatRunCommandButton(text: String, command: String) = chatButton(command, text, command, ClickEvent.Action.RUN_COMMAND)
        fun chatSuggestCommandButton(text: String, command: String) = chatButton(command, text, command, ClickEvent.Action.SUGGEST_COMMAND)
        fun chatRunCommandYesButton(command: String) = chatRunCommandButton("<green><bold>YES</bold></green>", command)
        fun chatRunCommandNoButton(command: String) = chatRunCommandButton("<red><bold>NO</bold></red>", command)
        fun chatRunCommandOkButton(command: String) = chatRunCommandButton("<green><bold>OK</bold></green>", command)
        fun chatRunCommandXButton(command: String) = chatRunCommandButton("<red><bold>✕</bold></red>", command)
        fun chatRunCommandCheckmarkButton(command: String) = chatRunCommandButton("<green><bold>✓</bold></green>", command)
    }

    fun broadcastAction(predicate: (P) -> Boolean = { true }, action: (P) -> Unit)
    fun CS.sendFormattedMessage(message: String)
    fun broadcastFormattedMessage(message: String, predicate: (P) -> Boolean = { true }) {
        broadcastAction(predicate) { it.sendFormattedMessage(message) }
    }

    fun P.sendFormattedTitle(
        title: String = "",
        subtitle: String = "",
        fadeIn: @Range(from = 0, to = 72000) Int,
        stay: @Range(from = 0, to = 72000) Int,
        fadeOut: @Range(from = 0, to = 72000) Int,
    )

    fun P.sendFormattedTitle(title: String = "", subtitle: String = "")
    fun broadcastFormattedTitle(title: String = "", subtitle: String = "", predicate: (P) -> Boolean) =
        broadcastAction(predicate) { it.sendFormattedTitle(title, subtitle) }

    fun broadcastFormattedTitle(
        title: String = "",
        subtitle: String = "",
        fadeIn: @Range(from = 0, to = 72000) Int,
        stay: @Range(from = 0, to = 72000) Int,
        fadeOut: @Range(from = 0, to = 72000) Int,
        predicate: (P) -> Boolean = { true },
    ) = broadcastAction(predicate) { it.sendFormattedTitle(title, subtitle, fadeIn, stay, fadeOut) }

    fun P.sendFormattedPersistentTitle(title: String = "", subtitle: String = "", fadeIn: @Range(from = 0, to = 72000) Int)

    fun broadcastFormattedPersistentTitle(
        title: String = "",
        subtitle: String = "",
        fadeIn: @Range(from = 0, to = 72000) Int,
        predicate: (P) -> Boolean = { true },
    ) = broadcastAction(predicate) { it.sendFormattedPersistentTitle(title, subtitle, fadeIn) }

    fun P.sendFormattedActionBar(message: String)

    fun broadcastFormattedActionBar(message: String, predicate: (P) -> Boolean = { true }) =
        broadcastAction(predicate) { it.sendFormattedActionBar(message) }

    object Spigot : VitalUtils<SpigotCommandSender, SpigotPlayer> {
        override fun broadcastAction(predicate: (SpigotPlayer) -> Boolean, action: (SpigotPlayer) -> Unit) = Bukkit.getOnlinePlayers()
            .filter(predicate)
            .forEach(action)

        override fun SpigotCommandSender.sendFormattedMessage(message: String) = spigot().sendMessage(
            // must be used since, both version (paper and spigot) support the bungeeapi implementations...
            *BungeeComponentSerializer.get().serialize(MiniMessage.miniMessage().deserialize(message))
        )

        override fun broadcastFormattedMessage(message: String, predicate: (SpigotPlayer) -> Boolean) =
            broadcastAction(predicate) {
                // must be used since, both version (paper and spigot) support the bungeeapi implementations...
                it.spigot().sendMessage(*BungeeComponentSerializer.get().serialize(MiniMessage.miniMessage().deserialize(message)))
            }

        @JvmOverloads
        fun broadcastSound(sound: Sound, volume: Float, pitch: Float, predicate: (SpigotPlayer) -> Boolean = { true }) =
            broadcastAction(predicate) { it.playSound(it, sound, volume, pitch) }

        @JvmOverloads
        fun broadcastSound(sound: Sound, predicate: (SpigotPlayer) -> Boolean = { true }) =
            broadcastSound(sound, 1f, 1f, predicate)

        override fun SpigotPlayer.sendFormattedTitle(
            title: String,
            subtitle: String,
            fadeIn: @Range(from = 0, to = 72000) Int,
            stay: @Range(from = 0, to = 72000) Int,
            fadeOut: @Range(from = 0, to = 72000) Int,
        ) = sendTitle(
            LegacyComponentSerializer.legacySection().serialize(MiniMessage.miniMessage().deserialize(title)),
            LegacyComponentSerializer.legacySection().serialize(MiniMessage.miniMessage().deserialize(subtitle)),
            fadeIn,
            stay,
            fadeOut
        )

        override fun SpigotPlayer.sendFormattedTitle(title: String, subtitle: String) = sendTitle(
            LegacyComponentSerializer.legacySection().serialize(MiniMessage.miniMessage().deserialize(title)),
            LegacyComponentSerializer.legacySection().serialize(MiniMessage.miniMessage().deserialize(subtitle))
        )

        override fun broadcastFormattedTitle(title: String, subtitle: String, predicate: (SpigotPlayer) -> Boolean) =
            broadcastAction { it.sendFormattedTitle(title, subtitle) }

        override fun SpigotPlayer.sendFormattedPersistentTitle(
            title: String,
            subtitle: String,
            fadeIn: @Range(from = 0, to = 72000) Int,
        ) = sendTitle(title, subtitle, fadeIn, 72000,  /* 1h */0)

        override fun broadcastFormattedPersistentTitle(
            title: String,
            subtitle: String,
            fadeIn: @Range(from = 0, to = 72000) Int,
            predicate: (SpigotPlayer) -> Boolean,
        ) = broadcastAction { it.sendFormattedPersistentTitle(title, subtitle, fadeIn) }

        @JvmOverloads
        fun broadcastPotionEffect(
            potionEffectType: PotionEffectType,
            duration: Int,
            amplifier: Int,
            playerPredicate: (SpigotPlayer) -> Boolean = { true },
        ) = broadcastAction(playerPredicate) { it.addPotionEffect(PotionEffect(potionEffectType, duration, amplifier)) }

        @JvmOverloads
        fun broadcastClearPotionEffect(potionEffectType: PotionEffectType, playerPredicate: (SpigotPlayer) -> Boolean = { true }) =
            broadcastAction(playerPredicate) { it.removePotionEffect(potionEffectType) }

        @JvmOverloads
        fun broadcastClearPotionEffects(playerPredicate: (SpigotPlayer) -> Boolean = { true }) = broadcastAction(playerPredicate) {
            it.activePotionEffects.map { it.type }.forEach { type: PotionEffectType -> it.removePotionEffect(type) }
        }

        override fun SpigotPlayer.sendFormattedActionBar(message: String) = spigot().sendMessage(
            ChatMessageType.ACTION_BAR, *BungeeComponentSerializer.get().serialize(MiniMessage.miniMessage().deserialize(message))
        )

        override fun broadcastFormattedActionBar(message: String, predicate: (SpigotPlayer) -> Boolean) =
            broadcastAction(predicate) { it.sendFormattedActionBar(message) }

        @JvmOverloads
        fun SpigotPlayer.teleportWithEffect(location: Location, potionEffectType: PotionEffectType = PotionEffectType.SLOWNESS) {
            removePotionEffect(potionEffectType)
            addPotionEffect(PotionEffect(potionEffectType, 2, Int.Companion.MAX_VALUE))
            teleport(location)
            removePotionEffect(potionEffectType)
        }

        fun SpigotPlayer.teleportWithEffect(to: Entity) = teleportWithEffect(to.location, PotionEffectType.SLOWNESS)

        fun canBePlacedInMidAir(material: Material) = !material.hasGravity() &&
                !isVegetation(material) &&
                (material != Material.REDSTONE &&
                        material != Material.REDSTONE_TORCH &&
                        material != Material.REPEATER &&
                        material != Material.COMPARATOR &&
                        material != Material.LEVER &&
                        material != Material.TRIPWIRE &&
                        !material.name.contains("BUTTON") &&
                        !material.name.contains("PRESSURE_PLATE") &&
                        !material.name.contains("RAIL"))

        fun isVegetation(material: Material) = material.name.contains("SAPLING") ||
                material.name.contains("FLOWER") ||
                material.name.contains("WHEAT") ||
                material.name.contains("SEEDS") ||
                material.name.contains("CROP") ||
                material.name.contains("KELP") ||
                material.name.contains("BUSH") ||
                material.name.contains("MUSHROOM") ||
                material.name.contains("CHORUS") ||
                material.name.contains("FERN") ||
                material.name.contains("POTTED") ||
                material.name.contains("ROSE") ||
                material.name.contains("POPPY") ||
                material == Material.MELON_STEM ||
                material == Material.PUMPKIN_STEM ||
                material == Material.BAMBOO ||
                material == Material.SUGAR_CANE ||
                material == Material.SEA_PICKLE ||
                material == Material.NETHER_WART ||
                material == Material.LILY_PAD ||
                material == Material.VINE ||
                material == Material.GLOW_LICHEN ||
                material == Material.SCULK_VEIN ||
                material == Material.CACTUS ||
                material == Material.LILAC ||
                material == Material.PEONY ||
                material == Material.TALL_GRASS ||
                material == Material.TALL_SEAGRASS ||
                material == Material.MANGROVE_PROPAGULE

        fun isRedstoneMachine(material: Material) = material.creativeCategory == CreativeCategory.REDSTONE &&
                (material == Material.REDSTONE_TORCH ||
                        material.name.contains("PISTON") ||
                        material.name.contains("BUTTON") ||
                        material.name.contains("PRESSURE_PLATE") ||
                        material.name.contains("DETECTOR") ||
                        material.name.contains("LAMP") ||
                        material == Material.COMPARATOR ||
                        material == Material.REPEATER ||
                        material == Material.REDSTONE ||
                        material == Material.REDSTONE_WIRE ||
                        material == Material.OBSERVER ||
                        material == Material.DROPPER ||
                        material == Material.DISPENSER ||
                        material == Material.HOPPER ||
                        material == Material.HOPPER_MINECART)

        fun isInsideLocationArea(location1: Location, location2: Location, location: Location): Boolean {
            val ourMinX = min(location1.x, location2.x)
            val ourMaxX = max(location1.x, location2.x)
            val ourMinY = min(location1.y, location2.y)
            val ourMaxY = max(location1.y, location2.y)
            val ourMinZ = min(location1.z, location2.z)
            val ourMaxZ = max(location1.z, location2.z)
            val theirX = location.x
            val theirY = location.y
            val theirZ = location.z

            return theirX >= ourMinX && theirX <= ourMaxX && theirY >= ourMinY && theirY <= ourMaxY && theirZ >= ourMinZ && theirZ <= ourMaxZ
        }

        fun getRandomLocationInLocationArea(location1: Location, location2: Location): Location {
            val ourMinX = min(location1.x, location2.x)
            val ourMaxX = max(location1.x, location2.x)
            val ourMinY = min(location1.y, location2.y)
            val ourMaxY = max(location1.y, location2.y)
            val ourMinZ = min(location1.z, location2.z)
            val ourMaxZ = max(location1.z, location2.z)
            val randomX = Random().nextDouble(ourMinX, ourMaxX)
            val randomY = Random().nextDouble(ourMinY, ourMaxY)
            val randomZ = Random().nextDouble(ourMinZ, ourMaxZ)

            return Location(location1.world, randomX, randomY, randomZ)
        }

        fun getCenterBlockLocation(location: Location, xOffset: Double, yOffset: Double, zOffset: Double): Location {
            val finalLocation = location.block.location.clone()
                .add(.5, .5, .5)
                .add(xOffset, yOffset, zOffset)

            finalLocation.pitch = location.pitch
            finalLocation.yaw = location.yaw
            finalLocation.direction = location.direction

            return finalLocation
        }

        fun getCenterBlockLocation(location: Location) = getCenterBlockLocation(location, 0.0, 0.0, 0.0)
        fun getCenterBlockTopLocation(location: Location) = getCenterBlockLocation(location, 0.0, .5, 0.0)
        fun getCenterBlockSideLocation(location: Location) = getCenterBlockLocation(location, 0.0, -.5, 0.0)

        fun getCircumferenceOfLocationArea(location1: Location, location2: Location): List<Location> {
            val minX = min(location1.x, location2.x)
            val maxX = max(location1.x, location2.x)
            val minY = min(location1.y, location2.y)
            val maxY = max(location1.y, location2.y)
            val minZ = min(location1.z, location2.z)
            val maxZ = max(location1.z, location2.z)
            val circumference = ArrayList<Location>()

            run {
                var y = minY
                while (y <= maxY) {
                    var z = minZ
                    while (z <= maxZ) {
                        circumference.add(Location(location1.world, minX, y, z))
                        z++
                    }
                    y++
                }
            }

            run {
                var y = minY
                while (y <= maxY) {
                    var z = minZ
                    while (z <= maxZ) {
                        circumference.add(Location(location1.world, maxX, y, z))
                        z++
                    }
                    y++
                }
            }

            run {
                var y = minY
                while (y <= maxY) {
                    var x = minX
                    while (x <= maxX) {
                        circumference.add(Location(location1.world, x, y, minZ))
                        x++
                    }
                    y++
                }
            }
            var y = minY
            while (y <= maxY) {
                var x = minX
                while (x <= maxX) {
                    circumference.add(Location(location1.world, x, y, maxZ))
                    x++
                }
                y++
            }

            return circumference
        }

        fun getVolumeOfLocationArea(location1: Location, location2: Location): List<Location> {
            val minX = min(location1.x, location2.x)
            val maxX = max(location1.x, location2.x)
            val minY = min(location1.y, location2.y)
            val maxY = max(location1.y, location2.y)
            val minZ = min(location1.z, location2.z)
            val maxZ = max(location1.z, location2.z)
            val volume = ArrayList<Location>()
            var x = minX
            while (x <= maxX) {
                var y = minY
                while (y <= maxY) {
                    var z = minZ
                    while (z <= maxZ) {
                        volume.add(Location(location1.world, x, y, z))
                        z++
                    }
                    y++
                }
                x++
            }

            return volume
        }

        fun getVolumeSizeOfLocationArea(location1: Location, location2: Location) = getVolumeOfLocationArea(location1, location2).size

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

        fun World.getRandomLocation() = getBlockAt(
            (-29_999_984..29_999_984).random(),
            (-256..256).random(),
            (-29_999_984..29_999_984).random()
        ).location

        fun Location.getHighestSafeLocationFromTop(startFrom: Int = 256) = let {
            var y = startFrom
            do {
                y--
            } while (world!!.getBlockAt(x.toInt(), y, z.toInt()).type == Material.AIR)

            world!!.getBlockAt(x.toInt(), y + 2, z.toInt()).location
        }

        fun Location.getHighestSafeLocationFromBottom(startFrom: Int = 50) = let {
            var safeY = startFrom
            do {
                safeY++
            } while (world!!.getBlockAt(x.toInt(), safeY, z.toInt()).type != Material.AIR &&
                world!!.getBlockAt(x.toInt(), safeY + 1, z.toInt()).type != Material.AIR
            )

            Location(world, x, safeY.toDouble(), z)
        }

        fun cleanGameRules(worldName: String) = Bukkit.getWorld(worldName)?.cleanGameRules()
            ?: throw RuntimeException("World $worldName does not exist")
    }

    object Bungee : VitalUtils<BungeeCommandSender, BungeePlayer> {
        override fun broadcastAction(predicate: (BungeePlayer) -> Boolean, action: (BungeePlayer) -> Unit) =
            ProxyServer.getInstance().players.filter(predicate).forEach(action)

        override fun BungeeCommandSender.sendFormattedMessage(message: String) =
            sendMessage(*BungeeComponentSerializer.get().serialize(MiniMessage.miniMessage().deserialize(message)))

        override fun BungeePlayer.sendFormattedTitle(
            title: String,
            subtitle: String,
            fadeIn: @Range(from = 0, to = 72000) Int,
            stay: @Range(from = 0, to = 72000) Int,
            fadeOut: @Range(from = 0, to = 72000) Int,
        ) = sendTitle(
            ProxyServer.getInstance().createTitle()
                .title(
                    TextComponent.fromLegacy(
                        LegacyComponentSerializer.legacySection().serialize(MiniMessage.miniMessage().deserialize(title))
                    )
                )
                .subTitle(
                    TextComponent.fromLegacy(
                        LegacyComponentSerializer.legacySection().serialize(MiniMessage.miniMessage().deserialize(subtitle))
                    )
                )
                .fadeIn(fadeIn)
                .stay(stay)
                .fadeOut(fadeOut)
        )

        override fun BungeePlayer.sendFormattedTitle(title: String, subtitle: String) = sendTitle(
            ProxyServer.getInstance().createTitle()
                .title(
                    TextComponent.fromLegacy(
                        LegacyComponentSerializer.legacySection().serialize(MiniMessage.miniMessage().deserialize(title))
                    )
                )
                .subTitle(
                    TextComponent.fromLegacy(
                        LegacyComponentSerializer.legacySection().serialize(MiniMessage.miniMessage().deserialize(subtitle))
                    )
                )
        )

        override fun BungeePlayer.sendFormattedPersistentTitle(title: String, subtitle: String, fadeIn: @Range(from = 0, to = 72000) Int) =
            sendFormattedTitle(title, subtitle, fadeIn, 72000,  /* 1h */0)

        override fun BungeePlayer.sendFormattedActionBar(message: String) = sendMessage(
            ChatMessageType.ACTION_BAR,
            TextComponent.fromLegacy(LegacyComponentSerializer.legacySection().serialize(MiniMessage.miniMessage().deserialize(message)))
        )

        override fun broadcastFormattedTitle(title: String, subtitle: String, predicate: (BungeePlayer) -> Boolean) {
            TODO("Not yet implemented")
        }
    }
}