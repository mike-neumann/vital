---
title: Recipe for custom players
description: This page defines a recipe (pre-written code snippet) for `vital-players`, that can be copied by a developer instead of manually writing it.
navigation:
  title: Players
---

# Custom players

Below is an example for a custom player using `vital-players`.  
First you need to add this to your application configuration (`application.yaml`, `application.properties`, etc.).

```yaml [application.yaml]
vital:
	players:
		player-class-name: fully.qualified.class.name.MyPlayer
```


Then you can use the example below:

```java [MyPlayer.java]
public class MyPlayer extends VitalPlayer.Spigot {
	private String myString;
	private boolean myBoolean;

	public MyPlayer(Player player) {
		super(player);
	}

	// Getters and setters
}
```

Vital automatically creates an instance of your custom player when a player joins your server and automatically deletes it, when your player leaves.

Now use the `VitalPlayerRepository` bean to get your player.

```java
final var myPlayer = vitalPlayerRepository.getById(MyPlayer.class, myPlayerUniqueId);
myPlayer.getMyString();
myPlayer.getMyBoolean();
// …
```
