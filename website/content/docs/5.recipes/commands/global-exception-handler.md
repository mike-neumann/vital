---
title: Recipe for a global command exception handler
description: This page defines a recipe (pre-written code snippet) for a global command exception handler, that can be copied by a developer instead of manually writing it.
navigation:
  title: Global command exception handler
---

# Global command exception handler

Below is an example of a global command exception handler that can automatically handle exceptions for all of your commands so you don’t have to define arg exception handlers for each command.  
Use this to display more generic error messages, specific error messages for your commands should still be arg exception handlers.

Global exception handlers are also used to handle exceptions that might have been thrown inside your arg exception handlers.  
If you want to test this, simply throw any kind of exception in any arg exception handler.

```java [MyGlobalCommandExceptionHandler.java]
@VitalCommand.Advice(commandSenderClass = CommandSender.class)
public class MyGlobalCommandExceptionHandler {
	@VitalCommand.GlobalExceptionHandler(type = Exception.class)
	public void onException(CommandSender sender, String executedArg, Exception e) {
		player.sendMessage("An exception has occurred while you executed command '" + executedArg + "':  '" + e.getMessage() + "'");
	}
}
```
