---
title: Commands.
description: This page explains how you can create commands using the `vital-commands` module.
navigation:
  title: Commands
---

# Commands

Every command will be a class that extends `VitalCommand.Spigot` and is annotated with `VitalCommand.Info`.  
A command can have n-number of arg handlers, each arg handler is essentially a method that is executed, when the command is executed with the configured argument.  

Since Vital can determine the structure of your command by simply just looking at your class it will automatically build tab-completions without you having to do anything!  

```java [MyCommand.java]
@VitalCommand.Info(name = "mycommand")
public class MyCommand extends VitalCommand.Spigot {
}
```

## Arguments

The following example shows a command that has an arg handler for `/mycommand myarg0 myarg1`.  
When this command is executed, Vital automatically calls the method annotated with `@ArgHandler`.

```java [MyCommand.java]
@VitalCommand.Info(name = "mycommand")
public class MyCommand extends VitalCommand.Spigot {
  @ArgHandler(arg = @Arg(name = "myarg0 myarg1"))
  public ReturnState onMyArg0MyArg1SomethingBlaBla(Player player) {
    // ...
  }
}
```

Every arg handler **MUST** return a `ReturnState`.  
This type determines the status of your command arg execution.

By default, this will most likely be `ReturnState.SUCCESS` for almost any of your arg handlers.  
But sometimes you might need to return `ReturnState.NO_PERMISSION` or something else for special use cases.

> Note that we have direct access to the player (or command sender) that executed the command.  
> Using the `@VitalCommand.Info` annotation, you can also mark your command to only be executable by players, by default, this is turned off and your commands can also be executed by non-players, like your server console.  
> Important: Obviously, you cant set your parameter to a `Player` if you can also receive non-players as a sender, be careful of that when you build your commands!

We can also restrict specific arg handlers to players, or restrict their permissions.
```java [MyCommand.java]
@ArgHandler(arg = @Arg(name = "myarg0 myarg1", permission = "myserver.mycommand.permission", playerOnly = true))
```

## Parameters

Sometimes we need to get some input from a player.  
To do this, we can use the `<...>` syntax.

```java [MyCommand.java]
@VitalCommand.Info(name = "mycommand")
public class MyCommand extends VitalCommand.Spigot {
  @ArgHandler(arg = @Arg(name = "myarg0 <myarg1>"))
  public ReturnState onMyArg0MyArg1SomethingBlaBla(Player player, String[] values) {
    // ...
  }
}
```

The arg handler defined above will now be called when someone executes `/mycommand myarg0 somethinghere`.  
To get the value that was input in this variable, add a new parameter to your arg handler method: `String[] values`.  
This array holds all values for all variables in your command arg handler.

E.g., when someone executes `/mycommand myarg0 hello`, the `String[] values` array will be `["hello"]`.

Note that you cannot receive values that are delimited by spaces.  
So this `/mycommand myarg0 hello 123` will not work.  
For space delimited values, use varargs.

## Varargs

To also receive values delimited by spaces, you can use the vararg syntax `<...>*` in your arg handlers.

```java [MyCommand.java]
  @ArgHandler(arg = @Arg(name = "myarg0 <myarg1>*"))
  public ReturnState onMyArg0MyArg1SomethingBlaBla(Player player, String[] values) {
    // ...
  }
```

E.g., when someone executes `/mycommand myarg0 hello`, the `String[] values` array will be `["hello"]`.  
If someone executes `/mycommand myarg0 hello 123`, the `String[] values` array will be `["hello", "123"]`.

> Note that varargs behave the same as they do in Java.  
> Varargs **MUST** be the last variable in your arg handler and can only exist once.  
> You CANNOT have multiple varargs in a single arg handler, you can also NOT define another variable AFTER a vararg.
