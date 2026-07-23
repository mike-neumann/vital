---
title: Recipe for a global scoreboard.
description: Recipe for a global scoreboard.
navigation:
  title: Global scoreboard
---

# Player scoreboard

Below is an example for a player scoreboard whose content can be unique for each player on the server.  
Use this scoreboard, if you need to for example display player specific stats, etc.

```java
final var myPlayerScoreboard = new VitalPlayerScoreboard(
  (player) -> "Title for player " + player.getName(),
	(player) -> "Line 1 for player " + player.getName(),
	(player) -> "Line 2 for player " + player.getName(),
	(player) -> "Line 3 for player " + player.getName()
);
```
