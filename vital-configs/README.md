# vital-configs

This module can be used to add Vital's advanced config system to your plugin.  
It provides the means of defining advanced configs which can be stored on the filesystem.  

Configs will be defined as a class, which can have `n` fields.  
Each field will represent a piece of data, which can be stored on the filesystem.  

`vital-configs` currently supports 2 types of configs out-of-the-box.

1. Properties-Files
2. YAML-Files
