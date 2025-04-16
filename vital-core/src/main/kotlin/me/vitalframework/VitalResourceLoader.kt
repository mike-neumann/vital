package me.vitalframework

import org.springframework.core.io.*

class VitalResourceLoader : DefaultResourceLoader() {
    override fun getResource(location: String): Resource {
        // strip the protocol from the location before attempting to fetch
        val filteredLocation = location.replace("^[a-zA-Z]+:".toRegex(), "")
        // try to get the resource from classloader
        // if the resource is not found on the classpath of our plugin, delegate to super
        return classLoader?.getResource(filteredLocation)?.let {
            var finalLocation = it.path
            if (finalLocation.contains(".jar!/")) {
                finalLocation = "jar:$finalLocation"
            }
            UrlResource(finalLocation)
        } ?: super.getResource(location)
    }
}