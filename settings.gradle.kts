plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "vital"

include("vital-core")
include("vital-players")
include("vital-commands")
include("vital-inventories")
include("vital-tasks")
include("vital-items")
include("vital-holograms")
include("vital-scoreboards")
include("vital-minigames")
include("vital-core-processor")
include("vital-commands-processor")
include("vital-utils")
include("vital-statistics")
include("vital-configs")
include("vital-cloudnet4-driver")
include("vital-cloudnet4-bridge")
include("vital-logs")
