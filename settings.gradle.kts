rootProject.name = "vital"

include("vital-cloudnet4-bridge")
include("vital-cloudnet4-driver")
include("vital-commands")
include("vital-commands-processor")
include("vital-configs")
include("vital-core")
include("vital-core-processor")
include("vital-gradle-plugin")
include("vital-holograms")
include("vital-inventories")
include("vital-items")
include("vital-minigames")
include("vital-players")
include("vital-scoreboards")
include("vital-statistics")
include("vital-tasks")
include("vital-utils")

pluginManagement {
    plugins {
        val kotlinVersion: String by settings

        kotlin("jvm") version kotlinVersion
    }
}