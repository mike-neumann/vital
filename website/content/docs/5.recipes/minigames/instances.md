---
title: Recipe for minigame instances
description: This page defines a recipe (pre-written code snippet) for a game instance, that can be copied by a developer instead of manually writing it.
navigation:
  title: Instances
---

# Minigame instances

Below is an example for a single `vital-minigames` mini game instance.  
A game instance defines the "type" of your game and stores all the information for your game, like player stats, players alive, etc.

Every instance will be isolated into its own world.  
Instances can all receive events but only events that actually came from the isolated instance world.

New instance = new world.

```java [MyInstance.java]
public class MyInstance extends VitalMinigameInstance {
	@Override
	public void onRegister() {
		for (final var player : getInstance().getWorld().getPlayers()) {
			player.sendMessage("This instance was registered.");
		}
	}

	@Override
	public void onUnregister() {
		for (final var player : getInstance().getWorld().getPlayers()) {
			player.sendMessage("This instance was unregistered.");
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		e.getPlayer().sendMessage("You joined the game instance.");
	}
}
```

Because multiple instances can exist, you can’t define an instance as a Spring bean.  
Use the `VitalMinigameInstanceService` bean to actually register your instance in Vital.

```java
vitalMinigameInstanceService.registerInstance(new MyInstance());
```
