# vital-gradle-plugin

This module contains the Vital Gradle Plugin, which is used to simplify project setup when developing a plugin with Vital.  
It takes care of dependency management, as well as dependency-version management.  

To create a plugin with Vital, you may only add this as a Gradle plugin in your plugin build file.  
The `vital-core` and `vital-core-processor` dependencies will automatically be added by default when using this plugin.  

When using `vital-commands`, the `vital-commands-processor` will also be automatically added as an annotation processor when using this plugin.
