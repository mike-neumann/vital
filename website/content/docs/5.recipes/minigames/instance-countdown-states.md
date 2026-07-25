---
title: Recipe for minigame instance countdown states.
description: This page defines a recipe (pre-written code snippet) for an instance countdown game state, that can be copied by a developer instead of manually writing it.
navigation:
  title: Instance countdown states
---

# Minigame instance countdown states

Below is an example for a mini game instance countdown state for `vital-minigames`.  
In a game, only 1 state can currently be active at a given time.

Only the currently active state will receive events and decrease the countdown.  
The example shown below will define a countdown of 10 seconds that will decrease every 1 second.

```java [MyInstanceCountdownState.java]
@VitalCountdown.Info(interval = 1_000, countdown = 10)
public class MyInstanceCountdownState extends VitalMinigameInstanceCountdownState<MyInstance> {
	public MyInstanceState(MyInstance instance) {
		super(instance);
	}

	@Override
	public void onEnable() {
		for (final var player : getInstance().getWorld().getPlayers()) {
			player.sendMessage("This instance countdown state was enabled.");
		}
	}

	@Override
	public void onDisable() {
		for (final var player : getInstance().getWorld().getPlayers()) {
			player.sendMessage("This instance countdown state was disabled.");
		}
	}

	@Override
	public void onStart() {
		for (final var player : getInstance().getWorld().getPlayers()) {
			player.sendMessage("This instance countdown state countdown was started.");
		}
	}

	@Override
	public void onTick() {
		for (final var player : getInstance().getWorld().getPlayers()) {
			player.sendMessage("This instance countdown state was ticked.");
		}
	}

	@Override
	public void onExpire() {
		for (final var player : getInstance().getWorld().getPlayers()) {
			player.sendMessage("This instance countdown state has expired.");
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		e.getPlayer().sendMessage("You joined this instance countdown state.");
	}
}
```

Because multiple instances can have multiple states, you can’t define an instance state as a Spring bean.  
Just store your states directly in your instances and switch between them using `setState(VitalMinigameInstanceState)`

```java [MyInstance.java]
public class MyInstance extends VitalMinigameInstance {
	private MyInstanceCountdownState myInstanceCountdownState = new MyInstanceCountdownState(this);

	// Getters and setters.

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		setState(myInstanceCountdownState);
	}
}
```
