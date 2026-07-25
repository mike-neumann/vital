---
title: Recipe for global minigame states.
description: This page defines a recipe (pre-written code snippet) for a global game state, that can be copied by a developer instead of manually writing it.
navigation:
  title: Global minigame states
---

# Global mini game states

Below is an example for a global game state for the `vital-minigames` module.  
In a game, only 1 state can currently be active at a given time.

Only the currently active state will receive events.

```java [MyGlobalMinigameState.java]
@GlobalMinigameState
public class MyGlobalMinigameState extends VitalGlobalMinigameState {
	@Override
	public void onEnable() {
		for (final var player : Bukkit.getOnlinePlayers()) {
			player.sendMessage("This state was enabled.");
		}
	}

	@Override
	public void onDisable() {
		for (final var player : Bukkit.getOnlinePlayers()) {
			player.sendMessage("This state was disabled.");
		}
	}

	// This event method will only be active when this state is currently active.
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		e.getPlayer().sendMessage("You have joined the game during this active state.");
	}
}
```

To switch between states, use the global `VitalGlobalMinigameService` bean.  
Use `VitalGlobalMinigameService.setState(Class<? extends VitalGlobalMinigameState>)` to switch to a new state.


The old state will be deactivated and will no longer receive any events.  
The new state will be activated and can now receive events.

If the old state was a countdown state, the countdown will be stopped and reset.
