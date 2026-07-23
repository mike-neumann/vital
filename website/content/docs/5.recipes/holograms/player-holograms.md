---
title: Recipe for player holograms.
description: Recipe for player holograms.
navigation:
  title: Player holograms
---

# Player holograms

Below is an example on how to create a player hologram.  
Player holograms should be used when you want to display data / text that can be unique for each player.  
Use player holograms to display player specific stats, or translated text.

Use the `VItalHologramService` bean to create and delete your holograms.

```java
vitalHologramService.createPlayerHologram(
	myPlayer,
	myLocation,
	(player) -> new VitalHologram.Line("Line 1 for player " + player.getName()),
	(player) -> new VitalHologram.Line("Line 2 for player " + player.getName()),
	(player) -> new VitalHologram.Line("Line 3 for player " + player.getName())
);
```

To update your player hologram, simply use the `update()` method.
