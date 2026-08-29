---
title: Recipe for minigame instance states
description: This page defines a recipe (pre-written code snippet) for an instance game state, that can be copied by a developer instead of manually writing it.
navigation:
  title: Instance states
---

# Minigame instance states

Below is an example for a mini-game instance state for `vital-minigames`.  
In a game, only one state can currently be active at a given time.

Only the currently active state will receive events.

```java [MyInstanceState.java]
public class MyInstanceState extends VitalMinigameInstanceState<MyInstance> {
	public MyInstanceState(MyInstance instance) {
		super(instance);
	}

	@Override
	public void onEnable() {
		for (final var player : getInstance().getWorld().getPlayers()) {
			player.sendMessage("This instance state was enabled.");
		}
	}

	@Override
	public void onDisable() {
		for (final var player : getInstance().getWorld().getPlayers()) {
			player.sendMessage("This instance state was disabled.");
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		e.getPlayer().sendMessage("You joined this instance state.");
	}
}
```

Because multiple instances can have multiple states, you can’t define an instance state as a Spring bean.  
Just store your states directly in your instances and switch between them using `setState(VitalMinigameInstanceState)`

```java [MyInstance.java]
public class MyInstance extends VitalMinigameInstance {
	private MyInstanceState myInstanceState = new MyInstanceState(this);

	// Getters and setters.

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		setState(myInstanceState);
	}
}
```
