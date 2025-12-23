package me.vitalframework.statistics

import me.vitalframework.SubModule
import me.vitalframework.VitalSubModule

/**
 * Defines the official vital-statistics submodule, which is displayed when Vital starts.
 * It contains the Vital statistics system, which registers health checkers to your server that warn you when performance drops.
 */
@SubModule("vital-statistics")
class VitalStatisticsSubModule : VitalSubModule()
