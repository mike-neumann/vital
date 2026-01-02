# vital-gradle-plugin

This submodule contains the Vital Gradle Plugin, which is used to simplify project setup when developing a plugin with Vital.  
It takes care of dependency management, as well as dependency-version management.  

To create a plugin with Vital, you may only include this as a plugin in your Gradle build file.  
The `vital-core` and `vital-core-processor` dependencies will automatically be added by default when using this plugin.  

When using `vital-commands`, the `vital-commands-processor` will also be automatically added as an annotation processor when using this plugin.
