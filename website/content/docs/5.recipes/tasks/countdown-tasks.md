---
title: Recipe for countdown tasks.
description: This page defines a recipe (pre-written code snippet) for a countdown task, that can be copied by a developer instead of manually writing it.
navigation:
  title: Countdown tasks
---

# Countdown tasks

Below is an example for a countdown task that will run on your server’s scheduler (main server thread).  
Use countdown tasks for a more structured way of creating server-synchronous countdowns.

The example shown below will define a countdown of 10 seconds, every 1 second the countdown will decrease.

```java [MyCountdownTask.java]
@VitalCountdownTask.Info(interval = 1_000, countdown = 10)
public class MyCountdownTask extends VitalCountdownTask.Spigot {
	public MyCountdownTask(JavaPlugin plugin) {
		super(plugin);
	}

	@Override
	public void onTick() {
		for (final var player : Bukkit.getOnlinePlayers()) {
			player.sendMessage("Countdown tick.");
		}
	}

	@Override
	public void onExpire() {
		for (final var player : Bukkit.getOnlinePlayers()) {
			player.sendMessage("Countdown has expired");
		}
	}
}
```
