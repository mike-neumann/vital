package me.vitalframework.utils

import me.vitalframework.SubModule
import me.vitalframework.VitalSubModule
import org.springframework.stereotype.Component

/**
 * Defines the official vital-utils submodule, which is displayed when Vital starts.
 * This submodule can also be used outside a Vital plugin, as it contains no dependencies on Vital itself.
 * It contains utilities, which reduce code, boilerplate and duplication.
 */
@SubModule("vital-utils")
class VitalUtilsSubModule : VitalSubModule()
