---
title: About global scoreboards
description: This page explains how you can create scoreboards that are visible for all players and will show the same content for all players.
navigation:
  title: Global scoreboards
---

# Global scoreboards

Global scoreboards, like the name implies, will show global data and text that is **THE SAME FOR ALL PLAYERS ON THE SERVER**. You can't show translated text / data with this scoreboard.  
If you do need to show data / text that may be different for each player, please use [player scoreboards](/docs/modules/vital-scoreboards/player-scoreboards).

Unlike other components, scoreboards are not classes since they don't require any special data or event management.  
Simply create your scoreboard wherever you need it and store it globally.  

> It is recommended to expose your scoreboards as a Spring bean.  
> But if you don't want to, you don't have to.

```java
@Bean
public VitalGlobalScoreboard vitalGlobalScoreboard() {
  return new VitalGlobalScoreboard(
    () -> "Title",
    () -> "Line 1",
    () -> "Line 2",
    () -> "Line 3"
  );
}
```

Note that the title and lines are both lambda functions.  
Every time you change the title or the lines, the title and lines functions will be invoked to produce a freshly updated scoreboard.

---

To change the title, use `setTitle(Function<String>)`.  
```java
vitalGlobalScoreboard.setTitle(() -> "New title");
```

To change the lines, use `setLines(List<Function<String>>)`.  
```java
vitalGlobalScoreboard.setLines(List.of(
  () -> "New line 1",
  () -> "New line 2",
  () -> "New line 3"
));
```

To add a player to the scoreboard, use `addPlayer(Player)`.  
```java
vitalGlobalScoreboard.addPlayer(myPlayer);
```

To remove a player from the scoreboard, use `removePlayer(Player)`.  
```java
vitalGlobalScoreboard.removePlayer(myPlayer);
```
