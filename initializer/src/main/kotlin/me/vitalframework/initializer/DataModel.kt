package me.vitalframework.initializer

data class DataModel(
    val name: String,
    val description: String,
    val version: String,
    val apiVersion: String?,
    val authors: List<String>,
    val pluginEnvironment: PluginEnvironment,
    val programmingLanguage: ProgrammingLanguage,
    val gradleDslProgrammingLanguage: GradleDslProgrammingLanguage
)
