# vital-all

This module can be used to add all Vital modules as a dependency to your project.  
It contains all Vital modules as a transitive dependency, even ones that might not work for your specific platform.  

To exclude specific modules from your plugin, you can add this to your plugin's build file:  

```kotlin
configurations.all {
    exclude(group = "dev.vitalframework", module = "vital-.")
}
```
