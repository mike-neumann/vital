# vital-commands

This submodule can be used to include Vital's advanced command system into your plugin.  
It provides the means of defining advanced command using annotation-driven configurations right in your codebase.  

Commands will be defined as a class, which can have `n` argument handlers.  
Each handler can be mapped to a specific argument, e.g. "add <player>".  
When this argument is executed by a player, the annotated function will be called.  

This submodule also provides the means of handling exceptions, that may occur during the execution of one or more mapped argument handlers.  
There are argument-specific exception-handlers, as well as global exception handlers, that are executed, when no argument exception handler can be found.

This submodule must be used in combination with `vital-commands-processor`.  
Make sure you add the `vital-commands-processor` as an annotation processor, otherwise your commands will not be registered.  
