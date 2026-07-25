---
title: Recipe for repeatable tasks.
description: This page defines a recipe (pre-written code snippet) for a repeatable task, that can be copied by a developer instead of manually writing it.
navigation:
  title: Repeatable tasks
---

# Repeatable tasks

Below is an example for a repeatable task that will run on your server’s scheduler (main server thread).  
Use repeatable tasks for a more structured way of creating server-synchronous tasks.

The example shown below will repeat every 1 second.

```java [MyRepeatableTask.java]
@VitalRepeatableTask.Info(interval = 1_000)
public class MyRepeatableTask extends VitalRepeatableTask.Spigot {
	public MyRepeatableTask(JavaPlugin plugin) {
		super(plugin);
	}

	@Override
	public void onTick() {
		for (final var player : Bukkit.getOnlinePlayers()) {
			player.sendMessage("Repeatable task tick.");
		}
	}
}
```
