# Comparisons

Here you will find small comparisons between Spigot and Vital regarding certain implementations.

**These comparisons are NOT meant to teach you how Vital works.**

## Main class

<table>
<tr>
<th>Spigot</th>
<th>Vital</th>
</tr>

<tr>
<td>

```java
public class MyPlugin extends JavaPlugin {
    @Override
    public void onEnable() {

    }
    
    @Override
    public void onDisable() {
        
    }
}
```

Then for Spigot and Paper you also need a `plugin.yml` file:

```yaml
name: MyPlugin
version: 1.0.0
description: MyPlugin description
api-version: 1.21
author: [MyName]
main: me.myplugin.MyPlugin
```
</td>

<td>

```java
@Vital.Info(
        name = "MyPlugin",
        version = "1.0.0",
        description = "MyPlugin description",
        apiVersion = "1.21",
        author = {"MyName"},
        // For Spigot
        environment = Vital.Info.PluginEnvironment.SPIGOT,
        // Or for Paper
        environment = Vital.Info.PluginEnvironment.PAPER
)
public class MyPlugin {
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(ApplicationReadyEvent e) {
        // Do something here...
    }

    @EventListener(ContextClosedEvent.class)
    public void onContextClosed(ContextClosedEvent e) {
        // Do something here...
    }
}
```

When using Vital, you don't need to create a `plugin.yml` file.  
All plugin-related configurations are done via annotations.  
You also don't have to register any listeners or commands anymore!
</td>
</tr>
</table>

## Commands

<table>
<tr>
<th>Spigot</th>
<th>Vital</th>
</tr>

<tr>
<td>

```java
public class MyCommand extends CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // If our command should be player only, 
        // we need to cancel execution early here.
        if (!(sender instanceof Player)) {
            return true;
        }

        // In Spigot, to handle each argument, 
        // you need to manually check the "args" array for length and content.
        if (args.length == 0) {
            // Execute something when just calling "/mycommand"
        } else {
            if (args.length == 1) {
                // We have exactly one argument, but we don't know what it is.
                // We need to check the content of the argument.
                if (args[0].equalsIgnoreCase("arg1")) {
                    // now we need to check 
                    // if the sender has permission to execute this command.
                    if (!sender.hasPermission("permission.node.required.to.run.this.arg")) {
                        return true;
                    }
                    
                    // We have executed "/mycommand arg1"
                } else {
                    // We have executed something else.
                }
            } else {
                // We have more than one argument, 
                // and we still don't know what they are.
            }
        }
        
        // As you can see, we have a pyramid of doom already, 
        // and we are not even doing anything yet...
        // And if we include values in arguments, 
        // the logic gets even more messed up...
    }
}
```

Then you need to update your `plugin.yml` file:
```yaml
name: MyPlugin
version: 1.0.0
description: MyPlugin description
api-version: 1.21
author: [MyName]
main: me.myplugin.MyPlugin

commands:
  mycommand:
```
</td>

<td>

```java
// This "sets up" the command.
@VitalCommand.Info(
        name = "MyCommand",
        description = "MyCommand description",
        aliases = {"mycommand"},
        usage = "Some usage information about the command",
        permission = "the permission node required to execute this command, e.g. my.command.execute",
        playerOnly = true
)
public class MyCommand extends VitalCommand.Spigot {
    // @ArgHandler tells Vital that this method should be called
    // when the command is executed with no arguments.
    @ArgHandler
    public ReturnState onNoArg(Player player) {
        // We can safely pass a player reference to this method because up top, 
        // we have defined that the command is player-only.

        // Here we define what the result of this command execution should be.
        // In almost all cases, this can be ReturnState.SUCCESS.
        // Just take a look at the "ReturnState" enum for more information.
        return ReturnState.SUCCESS;
    }

    // Same things go for this method here.
    @ArgHandler(arg = @Arg(name = "arg1", permission = "permission.node.required.to.run.this.arg", playerOnly = true))
    public ReturnState onArg1(Player player) {
        // We can safely pass a player reference to this method because up top, 
        // we have defined that the arg is player-only.
        return ReturnState.SUCCESS;
    }
    
    @ArgHandler(arg = @Arg(name = "arg1 <value1>"))
    public ReturnState onArg1Value1(Player player, String[] values) {
        // Just like above, but now we also have a value.
        // The value is passed to the "values" array, 
        // and because we defined exactly one inside our arg handler annotation, 
        // this will ALWAYS be one argument here.
        final var value1 = values[0];
        
        // Now we can do whatever we want with the value.
        
        return ReturnState.SUCCESS;
    }
}
```
As you can see, Vital offers a more concise way of handling commands.  
Stuff is waay more readable, structured and less error-prone.  

There is also native exception handling in Vital, 
but that goes a bit too deep for a short comparison.  

The `plugin.yml` is managed by Vital, so you don't have to worry about it anymore.
</td>
</tr>
</table>

## Listeners

<table>
<tr>
<th>Spigot</th>
<th>Vital</th>
</tr>

<tr>
<td>

```java
public class MyListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        // Do something here...
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        // Do something here...
    }
}
```

And then you'd have to manually register the listener.  

```java
@Override
public void onEnable() {
    getServer().getPluginManager().registerEvents(new MyListener(), this);
    // and repeat for every other listener in your project...
}
```
</td>

<td>

```java
@Listener
public class MyListener extends VitalListener.Spigot {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        // Do something here...
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        // Do something here...
    }
}
```

Listeners are pretty much the same in Vital,
as they are already pretty lightweight in Spigot / Paper.  

In Vital, you don't have to register listeners anymore.  
</td>
</tr>
</table>

## Scoreboards

<table>
<tr>
<th>Spigot</th>
<th>Vital</th>
</tr>

<tr>
<td>

```java
public void setGlobalScoreboard(Player player) {
    final var scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    final var objective = scoreboard.registerNewObjective(
            "myObjective",
            Criteria.DUMMY,
            Component.text("Display Name")
    );
    objective.setDisplaySlot(DisplaySlot.SIDEBAR);

    objective.getScore("Line 3").setScore(2);
    objective.getScore("Line 2").setScore(1);
    objective.getScore("Line 1").setScore(0);

    // To update the scoreboard, you need to remove all entries first...
    for (final var entry : scoreboard.getEntries()) {
        scoreboard.resetScores(entry);
    }

    // And then re-add the content...
    objective.getScore("Line 3").setScore(2);
    objective.getScore("Line 2").setScore(1);
    objective.getScore("Line 1").setScore(0);

    // Not very developer-friendly...
}

public void setPerPlayerScoreboard(Player player) {
    final var scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    final var objective = scoreboard.registerNewObjective(
            "myObjective", 
            Criteria.DUMMY,
            Component.text("Display Name")
    );
    objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    
    objective.getScore("Line 3").setScore(2);
    objective.getScore("Line 2").setScore(1);
    objective.getScore("Line 1").setScore(0);
    
    // To update the scoreboard, you need to remove all entries first...
    for (final var entry : scoreboard.getEntries()) {
        scoreboard.resetScores(entry);
    }
    
    // And then re-add the content...
    objective.getScore("Line 3").setScore(2);
    objective.getScore("Line 2").setScore(1);
    objective.getScore("Line 1").setScore(0);
    
    // Not very developer-friendly...
}
```
</td>

<td>

Global scoreboards (Should be used when you want to display the same scoreboard on all players, so no player-specific data).  

```java
public void setGlobalScoreboard(Player player) {
    final var scoreboard = new VitalGlobalScoreboard(
            "title",
            () -> "Line 1",
            () -> "Line 2",
            () -> "Line 3"
    );
    
    scoreboard.addPlayer(player);

    // If some background data has changed on the player object, 
    // you can easily update it.
    scoreboard.update(player);
    
    // Or update it for all players that are added to this scoreboard 
    // if you want to.
    scoreboard.update();
}
```

Per-player scoreboards (Should be used when you want to display a scoreboard for each player individually).  

```java
public void setScoreboard(Player player) {
    final var scoreboard = new VitalPerPlayerScoreboard(
            "title",
            player -> "Line 1",
            player -> "Line 2",
            player -> "Line 3"
    );
    
    scoreboard.addPlayer(player);

    // If some background data has changed on the player object, 
    // you can easily update it.
    scoreboard.update(player);
}
```

A perfect example of how Vital can simplify your code.  
As demonstrated above, you can easily create scoreboards with Vital, without having to worry about the details of Bukkit's Scoreboard API.  
</td>
</tr>
</table>

## More to come...