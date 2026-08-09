---
title: Recipe for a command with an arg exception handler
description: This page defines a recipe (pre-written code snippet) for a command and arg exception handler, that can be copied by a developer instead of manually writing it.
navigation:
  title: Command with arg exception handler
---

# Command with an arg exception handler

Below is an example of a Spigot command that handles the following subcommands:
- `/mycommand arg0`
- `/mycommand arg0 <arg1>`
- `/mycommand arg0 arg1 <arg2>*`

For each of these subcommands, an arg exception handler has been defined that will catch any error thrown while executing the command.

If you want to test the exceptions, simply uncomment `throw new Exception("Test exception")` (Remove the `//`).

```java [MyCommand.java]
@VitalCommand.Info(name = "mycommand")
public class MyCommand extends VitalCommand.Spigot {
	@ArgHandler(arg = @Arg(name = "arg0"))
	public ReturnState onArg0(CommandSender sender) {
		sender.sendMessage("You have executed '/mycommand arg0'");
		if (true) {
			// throw new Exception("Test exception");
		}

		return ReturnState.SUCCESS;
	}

	@ArgHandler(arg = @Arg(name = "arg0 <arg1>"))
	public ReturnState onArg0(CommandSender sender, String[] values) {
		sender.sendMessage("You have executed '/mycommand arg0 <arg1>' here is your value for '<arg1>': 'values[0]'");
		if (true) {
			// throw new Exception("Test exception");
		}

		return ReturnState.SUCCESS;
	}

	@ArgHandler(arg = @Arg(name = "arg0 arg1 <arg2>*"))
	public ReturnState onArg0(CommandSender sender) {
		sender.sendMessage("You have executed '/mycommand arg0 arg1 <arg2>*', here are your values for '<arg2>*’: 'Arrays.toString(values)'");
		if (true) {
			// throw new Exception("Test exception");
		}

		return ReturnState.SUCCESS;
	}

	@ArgExceptionHandler(arg = "arg0", type = Exception.class)
	public void onArg0Exception(CommandSender sender, Exception e) {
		sender.sendMessage("An exception occurred while executing '/mycommand arg0': '" + e.getMessage() + "'");
	}

	@ArgExceptionHandler(arg = "arg0 <arg1>", type = Exception.class)
	public void onArg0Exception(CommandSender sender, Exception e) {
		sender.sendMessage("An exception occurred while executing '/mycommand arg0 <arg1>': '" + e.getMessage() + "'");
	}

	@ArgExceptionHandler(arg = "arg0 arg1 <arg2>*", type = Exception.class)
	public void onArg0Exception(CommandSender sender, Exception e) {
		sender.sendMessage("An exception occurred while executing '/mycommand arg0 arg1 <arg2>*': '" + e.getMessage() + "'");
	}
}
```
