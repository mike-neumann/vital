---
title: Recipe for scheduled tasks
description: This page defines a recipe (pre-written code snippet) for a scheduled task, that can be copied by a developer instead of manually writing it.
navigation:
  title: Scheduled tasks
---

# Scheduled tasks

Below is an example for a scheduled task that will run on your server’s scheduler (main server thread).  
Use scheduled tasks for a more slimmer version of a repeatable task and a more structured way of creating server-synchronous tasks.  

The example shown below will initially wait 1 second when it starts, and then repeat every 5 seconds.

```java [MyComponent.java]
@Component
class MyComponent {
  @VitalScheduled(initialDelay = 1, fixedDelay = 5, timeUnit = TimeUnit.SECONDS)
  public void myScheduledTask() {
    for (final var player : Bukkit.getOnlinePlayers()) {
      player.sendMessage("myScheduledTask ticked.");
    }
  }
}
```
