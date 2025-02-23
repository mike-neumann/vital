gradle.beforeProject {
    extra["kotlinSpringPluginVersion"] = findProperty("kotlinSpringPluginVersion")
    extra["dependencyManagementPluginVersion"] = findProperty("dependencyManagementPluginVersion")
    extra["springBootPluginVersion"] = findProperty("springBootPluginVersion")
    extra["shadowVersion"] = findProperty("shadowVersion")
}