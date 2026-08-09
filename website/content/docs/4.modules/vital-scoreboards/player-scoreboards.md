---
title: About player scoreboards
description: This page explains how you can create scoreboards that only visible by specific players and will show player-specific content for each player.
navigation:
  title: Player scoreboards
---

# Player scoreboards

You can create player scoreboards that are specifically designed to show data that may be unique across multiple players.  
With player scoreboards, you can create scoreboards that show translated text and show data specifically tied to a single player.  

If you do not need to show unique data / texts for each player, you should use [global scoreboards](/docs/modules/vital-scoreboards/global-scoreboards).

Unlike other components, scoreboards are not classes since they don't require any special data or event management.  
Simply create your scoreboard wherever you need it and store it globally.

> It is recommended to expose your scoreboards as a Spring bean.  
> But if you don't want to, you don't have to.

```java
@Bean
public VitalPlayerScoreboard vitalPlayerScoreboard() {
  return new VitalPlayerScoreoard(
    (player) -> "Title",
    (player) -> "Line 1",
    (player) -> "Line 2",
    (player) -> "Line 3"
  );
}
```

Note that the title and lines are both lambda functions that take in a player.  
Every time you change the title or the lines, the title and lines functions will be invoked to produce a freshly updated scoreboard.

---

To change the title, use `setTitle(BiFunction<Player, String>)`.  
```java
vitalPlayerScoreboard.setTitle(player -> "New title for " + player.getName());
```

To change the lines, use `setLines(List<BiFunction<Player, String>>)`.  
```java
vitalPlayerScoreboard.setLines(List.of(
  player -> "New line 1 for " + player.getName(),
  player -> "New line 2 for " + player.getName(),
  player -> "New line 3 for " + player.getName()
));
```

To add a player to the scoreboard, use `addPlayer(Player)`.  
```java
vitalPlayerScoreboard.addPlayer(myPlayer);
```

To remove a player from the scoreboard, use `removePlayer(Player)`.  
```java
vitalPlayerScoreboard.removePlayer(myPlayer);
```
