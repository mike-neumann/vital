---
title: Recipe for a player scoreboard
description: This page defines a recipe (pre-written code snippet) for a player scoreboard, that can be copied by a developer instead of manually writing it.
navigation:
  title: Player scoreboard
---

# Player scoreboard

Below is an example of a player scoreboard whose content can be unique for each player on the server.  
Use this scoreboard if you need to, for example, display player-specific stats, etc.

```java
final var myPlayerScoreboard = new VitalPlayerScoreboard(
  (player) -> "Title for player " + player.getName(),
	(player) -> "Line 1 for player " + player.getName(),
	(player) -> "Line 2 for player " + player.getName(),
	(player) -> "Line 3 for player " + player.getName()
);
```
