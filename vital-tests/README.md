# vital-tests

This module can be used to add Vital's testsuite to your plugin.  
It will contain some dependencies, which are required to run tests when building a plugin using Vital.  

This module should not be present in your plugin output, so add it via `compileOnly` instead of `implementation`.  
