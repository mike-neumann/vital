package me.vitalframework

abstract class VitalGradlePluginException(message: String, cause: Throwable? = null) : RuntimeException(message, cause) {
    class PluginNotFound(id: String, exampleVersion: String) :
        VitalGradlePluginException("could not find plugin '$id' please declare it in 'plugins {}' block, e.g: 'id(\"$id\") version \"$exampleVersion\"'")
}