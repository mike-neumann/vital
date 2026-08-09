---
title: Recipe for a global scoreboard
description: This page defines a recipe (pre-written code snippet) for a global scoreboard, that can be copied by a developer instead of manually writing it.
navigation:
  title: Global scoreboard
---

# Global scoreboard

Below is an example of a global scoreboard whose content will always be the same for all players on the server.  
This scoreboard should be used when you want to display global data / text for all players.  
If you want to show unique text / data for each player, use player scoreboards.

```java
final var myGlobalScoreboard = new VitalGlobalScoreboard(
	() -> "Title",
	() -> "Line 1",
	() -> "Line 2",
	() -> "Line 3"
);
```
