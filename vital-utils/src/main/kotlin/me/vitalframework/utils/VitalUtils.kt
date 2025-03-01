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
        fun chatButton(color: String, hover: String, text: String, click: String, action: ClickEvent.Action) =
            """
                <hover:show_text:'$hover'>
                    <click:${action.name.lowercase(Locale.getDefault())}:'$click'>
                        <$color>[$text]
                    </click>
                </hover>
            """.trimIndent().lines().joinToString("")

        fun chatRunCommandButton(color: String, text: String, command: String) =
            chatButton(color, command, text, command, ClickEvent.Action.RUN_COMMAND)

        fun chatSuggestCommandButton(color: String, text: String, command: String) =
            chatButton(color, command, text, command, ClickEvent.Action.SUGGEST_COMMAND)

        fun chatRunCommandYesButton(command: String) = chatRunCommandButton("green", "YES", command)
        fun chatRunCommandNoButton(command: String) = chatRunCommandButton("red", "NO", command)
        fun chatRunCommandOkButton(command: String) = chatRunCommandButton("green", "OK", command)
        fun chatRunCommandXButton(command: String) = chatRunCommandButton("red", "X", command)
    }

    fun broadcastAction(playerPredicate: (P) -> Boolean = { true }, action: (P) -> Unit)
    fun sendMessage(sender: CS, message: String)
    fun broadcastMessage(message: String, playerPredicate: (P) -> Boolean = { true }) {
        broadcastAction(playerPredicate) { sendMessage(it, message) }
    }

    fun sendTitle(
        player: P,
        title: String?,
        subtitle: String?,
        fadeIn: @Range(from = 0, to = 72000) Int,
        stay: @Range(from = 0, to = 72000) Int,
        fadeOut: @Range(from = 0, to = 72000) Int,
    )

    fun sendTitle(player: P, title: String?, subtitle: String?)

    fun broadcastTitle(
        title: String?,
        subtitle: String?,
        fadeIn: @Range(from = 0, to = 72000) Int,
        stay: @Range(from = 0, to = 72000) Int,
        fadeOut: @Range(from = 0, to = 72000) Int,
        playerPredicate: (P) -> Boolean = { true },
    ) {
        broadcastAction(playerPredicate) { sendTitle(it, title, subtitle, fadeIn, stay, fadeOut) }
    }

    fun broadcastTitle(title: String?, subtitle: String?, playerPredicate: (P) -> Boolean = { true }) {
        broadcastAction(playerPredicate) { sendTitle(it, title, subtitle) }
    }

    fun sendPersistentTitle(
        player: P,
        title: String?,
        subtitle: String?,
        fadeIn: @Range(from = 0, to = 72000) Int,
    )

    fun broadcastPersistentTitle(
        title: String?,
        subtitle: String?,
        fadeIn: @Range(from = 0, to = 72000) Int,
        playerPredicate: (P) -> Boolean = { true },
    ) {
        broadcastAction(playerPredicate) { sendPersistentTitle(it, title, subtitle, fadeIn) }
    }

    fun sendActionBar(player: P, message: String)

    fun broadcastActionBar(message: String, playerPredicate: (P) -> Boolean = { true }) {
        broadcastAction(playerPredicate) { sendActionBar(it, message) }
    }

    object Spigot : VitalUtils<SpigotCommandSender, SpigotPlayer> {
        override fun broadcastAction(
            playerPredicate: (SpigotPlayer) -> Boolean,
            action: (SpigotPlayer) -> Unit,
        ) {
            Bukkit.getOnlinePlayers()
                .filter(playerPredicate)
                .forEach(action)
        }

        override fun sendMessage(sender: SpigotCommandSender, message: String) {
            // must be used since, both version (paper and spigot) support the bungeeapi implementations...
            sender.spigot().sendMessage(
                *BungeeComponentSerializer.get().serialize(
                    MiniMessage.miniMessage()
                        .deserialize(message)
                )
            )
        }

        override fun broadcastMessage(message: String, playerPredicate: (SpigotPlayer) -> Boolean) {
            // must be used since, both version (paper and spigot) support the bungeeapi implementations...
            broadcastAction(playerPredicate) {
                it.spigot().sendMessage(
                    *BungeeComponentSerializer.get()
                        .serialize(MiniMessage.miniMessage().deserialize(message))
                )
            }
        }

        @JvmOverloads
        fun broadcastSound(
            sound: Sound,
            volume: Float,
            pitch: Float,
            playerPredicate: (SpigotPlayer) -> Boolean = { true },
        ) {
            broadcastAction(playerPredicate) { it.playSound(it, sound, volume, pitch) }
        }

        @JvmOverloads
        fun broadcastSound(sound: Sound, playerPredicate: (SpigotPlayer) -> Boolean = { true }) {
            broadcastSound(sound, 1f, 1f, playerPredicate)
        }

        override fun sendTitle(
            player: SpigotPlayer,
            title: String?,
            subtitle: String?,
            fadeIn: @Range(from = 0, to = 72000) Int,
            stay: @Range(from = 0, to = 72000) Int,
            fadeOut: @Range(from = 0, to = 72000) Int,
        ) {
            player.sendTitle(
                if (title == null) "" else LegacyComponentSerializer.legacySection()
                    .serialize(MiniMessage.miniMessage().deserialize(title)),
                if (subtitle == null) "" else LegacyComponentSerializer.legacySection()
                    .serialize(MiniMessage.miniMessage().deserialize(subtitle)),
                fadeIn,
                stay,
                fadeOut
            )
        }

        override fun sendTitle(player: SpigotPlayer, title: String?, subtitle: String?) {
            player.sendTitle(
                if (title == null) "" else LegacyComponentSerializer.legacySection()
                    .serialize(MiniMessage.miniMessage().deserialize(title)),
                if (subtitle == null) "" else LegacyComponentSerializer.legacySection()
                    .serialize(MiniMessage.miniMessage().deserialize(subtitle))
            )
        }

        override fun broadcastTitle(
            title: String?,
            subtitle: String?,
            playerPredicate: (SpigotPlayer) -> Boolean,
        ) {
            broadcastAction { sendTitle(it, title, subtitle) }
        }

        override fun sendPersistentTitle(
            player: SpigotPlayer,
            title: String?,
            subtitle: String?,
            fadeIn: @Range(from = 0, to = 72000) Int,
        ) {
            sendTitle(player, title, subtitle, fadeIn, 72000,  /* 1h */0)
        }

        override fun broadcastPersistentTitle(
            title: String?,
            subtitle: String?,
            fadeIn: @Range(from = 0, to = 72000) Int,
            playerPredicate: (SpigotPlayer) -> Boolean,
        ) {
            broadcastAction { sendPersistentTitle(it, title, subtitle, fadeIn) }
        }

        @JvmOverloads
        fun broadcastPotionEffect(
            potionEffectType: PotionEffectType,
            duration: Int,
            amplifier: Int,
            playerPredicate: (SpigotPlayer) -> Boolean = { true },
        ) {
            broadcastAction(playerPredicate) {
                it.addPotionEffect(PotionEffect(potionEffectType, duration, amplifier))
            }
        }

        @JvmOverloads
        fun broadcastClearPotionEffect(
            potionEffectType: PotionEffectType,
            playerPredicate: (SpigotPlayer) -> Boolean = { true },
        ) {
            broadcastAction(playerPredicate) { it.removePotionEffect(potionEffectType) }
        }

        @JvmOverloads
        fun broadcastClearPotionEffects(playerPredicate: (SpigotPlayer) -> Boolean = { true }) {
            broadcastAction(playerPredicate) { player ->
                player.activePotionEffects
                    .map { it.type }
                    .forEach { type: PotionEffectType -> player.removePotionEffect(type) }
            }
        }

        override fun sendActionBar(player: SpigotPlayer, message: String) {
            player.spigot().sendMessage(
                ChatMessageType.ACTION_BAR, *BungeeComponentSerializer.get().serialize(
                    MiniMessage.miniMessage().deserialize(message)
                )
            )
        }

        override fun broadcastActionBar(message: String, playerPredicate: (SpigotPlayer) -> Boolean) {
            broadcastAction(playerPredicate) { sendActionBar(it, message) }
        }

        @JvmOverloads
        fun teleport(
            player: SpigotPlayer,
            location: Location,
            potionEffectType: PotionEffectType = PotionEffectType.SLOWNESS,
        ) {
            player.removePotionEffect(potionEffectType)
            player.addPotionEffect(PotionEffect(potionEffectType, 2, Int.Companion.MAX_VALUE))
            player.teleport(location)
            player.removePotionEffect(potionEffectType)
        }

        fun teleport(player: SpigotPlayer, to: Entity) {
            teleport(player, to.location, PotionEffectType.SLOWNESS)
        }

        fun canBePlacedInMidAir(material: Material) =
            !material.hasGravity() &&
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

        fun isVegetation(material: Material) =
            material.name.contains("SAPLING") ||
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

        fun isRedstoneMachine(material: Material) =
            material.creativeCategory == CreativeCategory.REDSTONE &&
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

        fun getCenterBlockLocation(
            location: Location,
            xOffset: Double,
            yOffset: Double,
            zOffset: Double,
        ): Location {
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

        fun getVolumeSizeOfLocationArea(location1: Location, location2: Location): Long {
            return getVolumeOfLocationArea(location1, location2).size.toLong()
        }

        fun cleanGameRules(world: World) {
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
            world.setGameRule(GameRule.DO_FIRE_TICK, false)
            world.setGameRule(GameRule.DO_MOB_SPAWNING, false)
            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)
            world.setGameRule(GameRule.DO_MOB_LOOT, false)
            world.setGameRule(GameRule.DO_MOB_SPAWNING, false)
            world.setGameRule(GameRule.DO_TRADER_SPAWNING, false)
            world.setGameRule(GameRule.DO_VINES_SPREAD, false)
            world.setGameRule(GameRule.MOB_GRIEFING, false)
            world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false)
            world.setGameRule(GameRule.KEEP_INVENTORY, true)
            world.setGameRule(GameRule.DISABLE_RAIDS, true)
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false)

            world.time = 0
            world.difficulty = Difficulty.PEACEFUL
            world.weatherDuration = 0
        }

        fun cleanGameRules(worldName: String) {
            val world = Bukkit.getWorld(worldName)
                ?: throw RuntimeException("World $worldName does not exist")

            cleanGameRules(world)
        }
    }

    object Bungee : VitalUtils<BungeeCommandSender, BungeePlayer> {
        override fun broadcastAction(
            playerPredicate: (BungeePlayer) -> Boolean,
            action: (BungeePlayer) -> Unit,
        ) {
            ProxyServer.getInstance().players
                .filter(playerPredicate)
                .forEach(action)
        }

        override fun sendMessage(sender: BungeeCommandSender, message: String) {
            sender.sendMessage(
                *BungeeComponentSerializer.get().serialize(
                    MiniMessage.miniMessage()
                        .deserialize(message)
                )
            )
        }

        override fun sendTitle(
            player: BungeePlayer,
            title: String?,
            subtitle: String?,
            fadeIn: @Range(from = 0, to = 72000) Int,
            stay: @Range(from = 0, to = 72000) Int,
            fadeOut: @Range(from = 0, to = 72000) Int,
        ) {
            player.sendTitle(
                ProxyServer.getInstance().createTitle()
                    .title(
                        TextComponent.fromLegacy(
                            if (title == null) "" else LegacyComponentSerializer.legacySection()
                                .serialize(MiniMessage.miniMessage().deserialize(title))
                        )
                    )
                    .subTitle(
                        TextComponent.fromLegacy(
                            if (subtitle == null) "" else LegacyComponentSerializer.legacySection()
                                .serialize(MiniMessage.miniMessage().deserialize(subtitle))
                        )
                    )
                    .fadeIn(fadeIn)
                    .stay(stay)
                    .fadeOut(fadeOut)
            )
        }

        override fun sendTitle(player: BungeePlayer, title: String?, subtitle: String?) {
            player.sendTitle(
                ProxyServer.getInstance().createTitle()
                    .title(
                        TextComponent.fromLegacy(
                            if (title == null) "" else LegacyComponentSerializer.legacySection()
                                .serialize(MiniMessage.miniMessage().deserialize(title))
                        )
                    )
                    .subTitle(
                        TextComponent.fromLegacy(
                            if (subtitle == null) "" else LegacyComponentSerializer.legacySection()
                                .serialize(MiniMessage.miniMessage().deserialize(subtitle))
                        )
                    )
            )
        }

        override fun sendPersistentTitle(
            player: BungeePlayer,
            title: String?,
            subtitle: String?,
            fadeIn: @Range(from = 0, to = 72000) Int,
        ) {
            sendTitle(player, title, subtitle, fadeIn, 72000,  /* 1h */0)
        }

        override fun sendActionBar(player: BungeePlayer, message: String) {
            player.sendMessage(
                ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacy(
                    LegacyComponentSerializer.legacySection()
                        .serialize(MiniMessage.miniMessage().deserialize(message))
                )
            )
        }
    }
}