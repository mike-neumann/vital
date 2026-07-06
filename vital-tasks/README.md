# vital-tasks

This module can be used to add Vital's tasks system to your plugin.  
It provides the means of defining repeating tasks and global tasks using the server's schedular.  

Tasks will be defined as a class, which can react to specific events published by its superclass.  
Each task handler will be using the server's schedular at its core, meaning it is closely tied to the native implementation.  

You can use tasks to create countdowns, timers, etc. 