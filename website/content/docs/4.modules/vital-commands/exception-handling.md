---
title: Exception handling
description: This page explains how you can handle exceptions during your command executions.
navigation:
  title: Exception handling
---

# Exception handling

The commands module also includes a full solution for exception handling during your command execution.  
When a command is executed, the respective arg handlers are called as explained in [commands](/docs/modules/vital-commands/commands).  

When an exception occurs during that arg handler execution, Vital will search for appropriate arg exception handlers that can handle the exception that occurred for this arg handler.  

E.g., You have the following command.  

```java [MyCommand.java]
@VitalCommand.Info(name = "mycommand")
public class MyCommand extends VitalCommand.Spigot {
  @ArgHandler(arg = @Arg(name = "myarg0 myarg1"))
  public ReturnState onMyArg0MyArg1BlaBla(Player player) {
    throw new Exception();
  }
}
```

For demonstrational purposes, the arg handler above throws an `Exception`.  
How are we supposed to handle this exception?  

We **could** just wrap the statement that might throw the exception in a try-catch-block, but what if we have these kinds of statements across many commands?  
TLDR; it won't work, we'd have to manually handle all exceptions everywhere... garbage...  

The solution? Arg exception handlers.  

When this `Exception` is now thrown while you execute `/mycommand myarg0 myarg1`, Vital will automatically look for an arg exception handler in your command class that is configured to handle an `Exception` for `myarg0 myarg1`.  

An arg exception handler may look like this.  
```java [MyCommand.java]
@VitalCommand.Info(name = "mycommand")
public class MyCommand extends VitalCommand.Spigot {
  @ArgHandler(arg = @Arg(name = "myarg0 myarg1"))
  public ReturnState onMyArg0MyArg1BlaBla(Player player) {
    throw new Exception();
  }
  
  @ArgExceptionHandler(arg = "myarg0 myarg1", type = Exception.class)
  public void onMyArg0MyArg1Exception(Player player, Exception e) {
    // ...
  }
}
```

> Note that the exception handler shown above will currently handle **ALL exception**, because it's type is set to the super class of all exception (`Exception`).  
> If you only want to handle specific exceptions, you can narrow down your exceptions by simply using a different type, like `@ArgExceptionHandler(arg = "myarg0 myarg1", type = IllegalArgumentException.class)`, etc.

---

Just like in arg handlers, arg exception handlers also use the same method parameter binding technique.  
So you can freely choose whatever parameters you want to use for your method.  
The following parameters are supported and can freely be added to your method in whatever order you like.  

- `CommandSender` or `Player` - The command sender or player that executed the command.
- `String` - The actual argument that your player executed.
- `Arg` - The Vital arg annotation that was found for the command that the player executed.
- `Exception` - The actual exception that occurred, you can freely just use **your** exception type for this parameter.

## Global exceptions

But even with all of this exception handling, there are still some loopholes...  
What happens if we want to display a default message for all kinds of exceptions across our entire commands of your plugin?  
We'd have to create arg exception handlers for each command and that isn't really what we want to do...  

On top of everything that Vital already does, Vital also gives you an option to create a so-called **global command exception handler**.  
As the name implies, this exception handler will be called across **all of your commands in your plugin**.  

E.g., your command `mycommand` has an arg handler that now experiences an exception.  
Vital will try to find an arg exception handler for that exception.  
If it fails, Vital will try to find a global exception handler that can handle the exception that was thrown.  

Also, global exception handlers are **not bound to a specific argument**, they only handle a specific exception and are called for **all of your arguments**.  

Global command exceptions handlers are defined in their own class that must be annotated with `@VitalCommand.Advice(CommandSender)`.  

```java [MyGlobalCommandExceptionHandler.java]
@VitalCommand.Advice(CommandSender.class)
public class MyGlobalCommandExceptionHandler {
  @VitalCommand.GlobalExceptionHandler(type = Exception.class)
  public void onException(CommandSender sender, Exception e) {
    // ...
  }
}
```

As shown above, this global command exception handler will now automatically receive all exceptions that are otherwise not handled by any underlying arg exception handlers for any command.  
This global exception handler **will only be called if no arg exception handler exists that can handle the exception that was thrown, OR if an exception was thrown while your arg exception handler was called**.  

> Note that the exception handler shown above will currently handle **ALL exception**, because it's type is set to the super class of all exception (`Exception`).  
> If you only want to handle specific exceptions, you can narrow down your exceptions by simply using a different type, like `@VitalCommand.GlobalExceptionHandler(type = IllegalArgumentException.class)`, etc. 

But still, this isn't the final handling point for exceptions.  
If all of these handlers fail (for whatever reason), the `onCommandException` lifecycle of your command class will be the ultimate fallback for any kind of exception that was thrown during your arg exception handler and global exception handlers.  
Normally, this would never happen, but to make sure that you always have control over what happens in your command lifecyle, this is the absolute fallback.

Just like arg exception handlers, global exception handlers also use the same method parameter binding technique.  
So you can freely choose whatever parameters you want to use for your method.  
The following parameters are supported and can freely be added to your method in whatever order you like.  

- `CommandSender` or `Player` - The command sender or player that executed the command.
- `String` - The actual argument that your player executed.
- `Arg` - The Vital arg annotation that was found for the command that the player executed.
- `String[]` - The values (any player input for any varibale) for your command arg.
- `Exception` - The actual exception that occurred, you can freely just use **your** exception type for this parameter.
