---
title: Recipe for a global scoreboard.
description: Recipe for a global scoreboard.
navigation:
  title: Global scoreboard
---

# Global scoreboard

Below is an example for a global scoreboard whose content will always be the same for all players on the server.  
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
