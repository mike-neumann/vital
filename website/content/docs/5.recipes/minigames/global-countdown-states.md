---
title: Recipe for global minigame countdown states.
description: Recipe for global minigame countdown states.
navigation:
  title: Global minigame countdown states
---

# Global mini game countdown states

Below is an example for a global mini game countdown state for the `vital-minigames` module.  
In a game, only 1 state can currently be active at a given time.

Only the currently active state will receive events and the countdown will decrease.

```java [MyCountdownMinigameState.java]
@GlobalMinigameState
public class MyCountdownMinigameState extends VitalGlobalMinigameCountdownState {
	public MyCountdownMinigameState(JavaPlugin plugin) {
		super(plugin);
	}

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

	@Override
	public void onStart() {
		for (final var player : Bukkit.getOnlinePlayers()) {
			player.sendMessage("This state's countdown was started.");
		}
	}

	@Override
	public void onTick() {
		for (final var player : Bukkit.getOnlinePlayers()) {
			player.sendMessage("This state's countdown ticked.");
		}
	}

	@Override
	public void onExpire() {
		for (final var player : Bukkit.getOnlinePlayers()) {
			player.sendMessage("This state’s countdown has expired.");
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		e.getPlayer().sendMessage("You have joined the game during this active state.");
	}
}
```
