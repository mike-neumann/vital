# vital-all

This submodule can be used to include all Vital submodules as a dependency in the consuming project.  
It contains all Vital submodules as a transitive dependency, even ones that might only work for a singular platform.  

To exclude specific submodules from your plugin, you can include this in your consuming plugin's build file:  

```kotlin
configurations.all {
    exclude(group = "me.vitalframework", module = "vital-...")
}
```
