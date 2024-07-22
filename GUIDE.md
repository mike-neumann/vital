# Guide

## Installation

1. Clone `vital`
2. Configure JDK21 for project (Eclipse Temurin) and Language Level (SDK default)
3. Set Gradle Wrapper JDK Version to Project SDK
4. Reload Gradle and Wait until all background tasks have been completed (may take a while)
5. `publishToMavenLocal` using Gradle

## Usage

## IMPORTANT

*To understand how Vital works, it is recommended to read documentation explaining AOP and how dependency injection
works within Spring / Spring-Boot*

---

## Plugin Project Initialization

1. Create new project with Gradle or Maven support
2. Implement `Vital`s dependencies
3. Reload Gradle and Wait until all background tasks have been completed (may take a while)
4. When using both Gradle or Maven, make sure that `Vital`s dependencies are shaded into a FAT-Jar in the end
   If you do not know how that works, here is an example of how it is done in Gradle: `build.gradle.kts`

```
plugins {
    id("com.github.johnrengelman.shadow") version ("8.1.1")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
```

If you are using Vital's built in "vital-gradle-plugin", you can easily implement that plugin, and it will manage some
needed dependencies for you, it will also tell you when specific plugins are not found that are required for Vital to
work!

```
plugins {
    id("vital-gradle-plugin") version "1.0"
    id("com.github.johnrengelman.shadow") version ("8.1.1")
}

// tasks do not need to be configured for shading, since Vital's plugin does that for us
```

To allow the plugin to be found, you must configure gradle to scan for plugins in your local m2 repo:  
This must be configured in `settings.gradle.kts`

```
pluginManagement {
    repositories {
        mavenLocal() // to scan in local m2 repo
        gradlePluginPortal() // to scan in remote plugin portal (default)
    }
}
```

5. Create main plugin class under package `my.domain.company.project` e.g. `me.xra1ny.plugin` class `TestPlugin` or any
   other class name for your plugin
6. The main class should have the following signature `MYCLASS extends JavaPlugin`
7. The main class should have the following body:

```
@Override
public void onEnable() {
    Vital.run(getClass(), getName()); // here we run Vital with the current class of our plugin and the name of our plugin    
    // "getClass()" and "getName()" are accessible by the superclasses of our class
}

@Override
public void onDisable() { // this is just the default onDisable() method you override in spigot anyway
  // any shutdown logic
}
```

9. Now Vital is enabled and ready to perform!
10. Now you can move on to the sections needed for your plugin development!

## Vital Listeners

When using Vital, you can use Vital's predefined `VitalListener.Spigot` or `VitalListener.Bungeecord` class to implement
a self registering listener
Your listener class should have the following structure:

```
@Component // this annotation comes from Spring, this will make sure the listener is automatically registered when enabling Vital!
public class MyListener extends VitalListener.Spigot {
    // this is just like we do in regular spigot
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        // any logic on player join
    }
}
```

It stays the same with `Bungeecord` but there you will implement `VitalListener.Bungeecord` instead

## Vital Commands

When using Vital, you can use Vital's predefined `VitalCommand.Spigot` or `VitalCommand.Bungeecord` class to implement a
self registering command
Your command class should have the following structure:

```
@Component // this annotation comes from Spring, this will make sure the command is automatically registered when enabling Vital!
@VitalCommandInfo( // this annotation defines any meta data needed by the command to function properly like: name, arguments, permission, onlyPlayer, etc.
    value = "name",
    args = {
        @VitalCommandArg("arg"), // this is just a normal arg
        @VitalCommandArg("arg %PLAYER%") // this a player value arg
    }
)
public class MyCommand extends VitalCommand.Spigot {
    @Override
    public VitalCommandReturnState executeBaseCommand(CommandSender sender) {
        // any command logic when we enter "/name"
        return VitalCommandReturnState.SUCCESS;
    }

    // the signature of this method can just be simple, since we do not pass any player made values into it
    @VitalCommandArgHandler("arg")
    public VitalCommandReturnState onArg(CommandSender sender) {
        // any logic when we enter "/name arg"
    }

    // this method signature MUST be sender AND values: Array<String>, since we WANT TO PASS A PLAYER MADE VALUE INTO THE ARGUMENT
    // every time you want to pass any custom player made values into a command arg, you need to specify a method argument value: Array<String>, this array will contain any values, that are NOT the ones defined in the args = [] section of our command info
    @VitalCommandArgHandler("arg %PLAYER%")
    public VitalCommandReturnState onArgPlayer(CommandSender sender, String[] values) {
        // any logic when we enter "/name arg Herobrine" or "/name arg Notch" or "/name arg xRa1ny"       
        // any argument surrounded with % is marked as a player made value and MUST be handled with a method param: values: Array<String>
        return VitalCommandReturnState.SUCCESS;
    }
}
```

It stays the same with `Bungeecord` but there you will implement `VitalCommand.Bungeecord` instead  
*Any tab completion will be configured automatically!!!*

## Vital Items

When using Vital, you can use Vital's predefined `VitalItem` class to implement a self registering custom item stack
that has both left-click and right-click functionality
Your item class should have be following structure:

```
@Component // this makes sure that this item is automatically registered in Vital, the item may be given to a player via Vital's `VitalItemManager` e.g. `VitalItemManager.setItem()` ...
@VitalItemInfo(name = "Navigator <gray>(Rightclick)", type = Material.NETHER_STAR, cooldown = 1000) // here we define any metadata for out item
public class NavigatorItem extends VitalItem {
    @Override
    public void onRightClick(PlayerInteractEvent e) {
        // any logic when the player holding this item rights clicks
    }

    @Override
    public void onLeftClick(PlayerInteractEvent e) {
        // any logic when the player holding this item rights clicks
    }

    @Override
    public void onCooldownTick(Player player) {
        // any logic when the player holding this item has a cooldown and it counts down
    }

    // more methods available for overriding to add functionality
}
```

## Vital Players

This module is very important for developers who want to store player session data  
Using Vital you can define a player class that is instantiated when a player joins the server, holding important data a
developer may store  
To enable this functionality you will need to setup a player class as follows:

```
public class MyPlayer extends VitalPlayer.Spigot {
    public MyPlayer(Player player) {
        super(player);
    }

    public String someVariable = "dwadwa"

    // here we can store any data related to this player instance
}
```

The same goes for `Bungeecord` but with `VitalPlayer.Bungeecord` instead

Now we will need a player manager that manages all instances of this class, as follows:

```
@Component // this makes sure that the `MyPlayerManager` is automatically installed and registered within vital (IMPORTANT!!!)
public class MyPlayerManager extends VitalComponentManager<MyPlayer> {
}
```

Now that we have a player class that holds player data, and a manager that holds every available MyPlayer instance, we
need a system that automatically registers this custom instance every time a player joins the server...  
Create a class called `MyPlayerListener` as follows:

```
@Component // this is IMPORTANT since if this instance is not registered, the custom player instanced WILL NOT BE REGISTERED, and thus will never be available...
public class MyPlayerListener extends VitalPlayerListener.Spigot<LobbyPlayer, MyPlayerManager> {
    // this is automatically injected since `MyPlayerManager` is marked as `@Component`, no need to implement anything here
    public MyPlayerListener(MyPlayerManager myPlayerManager) {
        super(myPlayerManager)
    }

    @Override
    public Class<LobbyPlayer> vitalPlayerType() {
        return LobbyPlayer.class;
    }
}
```

The same goes for `Bungeecord` but there we use `VitalPlayerListener.Bungeecord` Instead

## Vital Tasks

This module is important if you want to implement any repeating task or countdown in your plugin  
Your class should be structured as follows:

```
@Component // the task should be registered automatically
@VitalRepeatableTaskInfo(interval = 1000) // marks the metadata of the task, defining its tick interval in millis
public class MyRepeatingTask extends VitalRepeatableTask.Spigot {
    // if you want to start the task right after vital is enabled, you can override the onRegistered method from VitalComponent, and then call the start() function to start the task
    @Override
    public void onRegistered() {
        start()
    }

    @Override
    public void onStart() {
        // any logic when this task starts
    }

    @Override
    public void onStop() {
        // any logic when this task stops
    }

    @Override
    public void onTick() {
        // any logic on task tick
    }
}
```

The same goes for `Bungeecord` but with `VitalRepeatableTask.Bungeecord` instead

And for countdown tasks:

```
@Component // should also be registered automatically
@VitalCountdownTaskInfo(3, interval = 1000) // metadata, value is the countdown in seconds, the interval is measured in millis
public class MyCountdownTask extends VitalCountdownTask.Bungeecord {
    @Override
    public void onStart() {}
    
    @Override
    public void onStop() {}
    ...

    // this structure is the same as in a normal repeatable task, but with methods to controll countdown flow and other actions happening within the countdown itself...
}
```

The same goes for `Bungeecord` but with `VitalCountdownTask.Bungeecord` instead

## Vital Inventories

When using Vital, you can easily build interactive and paged inventories.  
Your class should be structured as follows:

```
@Component // IMPORTANT!!! IF YOU MARK THIS INVENTORY AS @Component IT IS FORCED AS A _GLOBAL_ INVENTORY. TO HANDLE SINGULAR PLAYER DATA REMOVE THIS ANNOTATION AND HANDLE OPEN INVENTORY MANUALLY!!!
@VitalInventoryInfo("TITLE", size = 54)
public class MyGlobalInventory extends VitalInventory {
    public MyGlobalInventory(@Nullable Inventory previosInventory) {
      super(previousInventory);
    }

    @Override
    public void onOpen(InventoryOpenEvent e) {
        // any inventory open logic for the opening player
    }

    @Override
    public void onClose(InventoryCloseEvent e) {
        // any inventory close logic for the closing player
    }
    
    // on global inventory update, used to set items which are static, and do not change or are not clickable
    // use this method when you make global inventory which are not clickable
    @Override
    public void fun onUpdate() {
        setItem(0, ITEM);
        setItem(1, ITEM);
    }

    // on player inventory click, used to set items which are either player specific, with player data, or items that are clickable
    // NOTE: EVEN IF YOURE BUILDING A GLOBAL INVENTORY, AND NEED ITEMS THAT ARE CLICKABLE, YOU CAN USE THIS METHOD TO SET THOSE ITEMS!!!
    @Override
    public void fun onUpdate(player: Player) {
        // not clickable, static item
        setItem(0, ITEM);

        // item that is clickable by the specified player
        setItem(1, ITEM, player, e -> {
            // this function is called when the player clicks this item, you can use any code above this statement in here to perform actions!!!
        });
    }
}
```

When creating a _GLOBAL_ inventory, you can use Vital's `VitalInventoryManager` to open the Inventory for a player as
follows:

```
VitalInventoryManager.openVitalInventory(player, MyGlobalInventory.class)
```

When NOT creating a _GLOBAL_ inventory, you MUST manually create a new instance of your custom inventory as follows:

```
// creating an instance of our global inventory
MyGlobalInventory myGlobalInventory = new MyGlobalInventory(ANY ARGS);

// opening that inventory manually...
player.openInventory(myGlobalInventory.getInventory());
```

## Vital Scoreboards

When using Vital, you can use Vital's predefined classes to easily create custom and dynamic scoreboards  
Your implementation should be structured as follows:

```
// this is how you define a global scoreboard
VitalGlobalScoreboard globalScoreboard = new VitalGlobalScoreboard(
    "TITLE",
    "LINE1",
    "LiNE2",
    "LINE3"
);

// this is how you define a per player based scoreboard, these types of scoreboards are where you show player specific data
VitalPerPlayerScoreboard perPlayerScoreboard = new VitalPerPlayerScoreboard(
    "TITLE",
    player -> "LINE1", // you can always call `it.*` to perform actions on the player. The player is a variable called "it"
    player -> "LINE2",
    player -> "LINE3"
);
```

Then you can add players to a scoreboard via perPlayerScoreboard.addPlayer(PLAYERINSTANCE)`

## Vital Configs

The vital-configs module predefines some spigot / bukkit types available to save in a config.  
When defining configs you MUST save these types instead of the ones bukkit provides, since bukkit types are NORMALLY not
compatible with config serializers since they don't follow any standards.

1. Location -> ConfigLocation
2. Player -> ConfigPlayer
3. ItemStack -> ConfigItemStack
   etc.

## Vital Utils

When using Vital, you can utilize the vital-utils submodule for various utilities that make life more enjoyable

## Vital Minigames

With the Vital Minigames submodule, you can manage different states within a Vital made Plugin!  
This means that every minigame MUST have different game states
e.g. `Waiting`, `Voting`, `Starting`, `Ingame`, `Playing`, `Ending`, etc.  
Using this convention, minigames are more organized and structured.

A game state is structured as follows:

```
@Component // it is advised to make minigame states a COMPONENT
public class WaitingState extends VitalMinigameState {
    // we can now implement any methods from VitalMinigameState to handle a minigame state status in our current minigame plugin.
    // NOTE: minigame states are ALWAYS listeners, so you can directly implement your @EventHandler here!!!
}

@Component // again...
@VitalCountdownTaskInfo(3, interval = 1000) // is required for  minigame states that have countdowns!
public class StartingSate extends VitalCountdownMinigameState {
    // minigame states may sometimes require a countdown of some sort, vital has got you covered with a class that covers that functionality, IT WORKS THE SAME WAY AS A 'VitalCountdownTask' and must be configured the same way!
}
```

Now that we have some states, we may set them using Vital's built-in feature to set states across a minigame:  
`VitalMinigameManager.setVitalMinigameState(WaitingState.class)`  
`VitalMinigameManager.setVitalMinigameState(StartingState.class)`  
This way we can set the current state of our minigame!

---

## Vital Core Processor

This submodule will automatically generate your `plugin.yml` with every data required for the plugin to work.  
NO MORE plugin.yml TROUBLE!!!   
To Set this up, implement `vital-core-processor` as an annotation processor using your favourite build tool, like gradle
or maven  
Then add this annotation to your plugins main class:
`@VitalPluginInfo(...)`  
This annotation will supply vital with enough meta information to automatically build the `plugin.yml` file...

---

## Vital Commands Processor

This submodule is responsible to work with `vital-core-processor` to automatically handle the `plugin.yml` registration
of `@VitalCommandInfo` annotated classes.  
Implement this submodule as an annotation processor using your favourite build tool like, gradle or maven to
automatically register commands within your automatically generated `plugin.yml` file!
