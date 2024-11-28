# Guide

## Installation

1. Clone `vital`
2. Configure JDK 21 for project and gradle
3. Reload gradle and wait until all background tasks have been completed
4. `publishToMavenLocal` using gradle via run configuration (top right) or terminal `./gradlew publishToMavenLocal`

## Usage

## IMPORTANT

*TO UNDERSTAND HOW VITAL WORKS, IT IS RECOMMENDED TO READ DOCUMENTATION EXPLAINING HOW AOP AND DEPENDENCY INJECTION IN
SPRING WORKS*

---

## Plugin project initialization

1. Create new project with gradle
2. Configure plugin scanning in local m2 (settings.gradle.kts):

```kotlin
pluginManagement {
    repositories {
        // this allows for gradle to scan our LOCAL m2
        mavenLocal()
        // this is just the default gradle portal for any NON-LOCAL plugins
        gradlePluginPortal()
    }
}
```

3. Implement the following plugins:

```kotlin
plugins {
    // this allows for some auto configuration regarding dependencies needed for vital to work
    id("me.vitalframework.vital-gradle-plugin") version "1.0"
    // this plugin is needed to shade any "implementation" dependency into the output jar when building, so that vital classes can be found at runtime
    id("com.github.johnrengelman.shadow") version "8.1.1"
}
```

4. Create main plugin class under your desired package `my.domain.company.projectname`
   e.g. `my.domain.company.projectname.ProjectPlugin`
5. Initialize vital on plugin startup:  
   Spigot:

```java
package my.domain.company.projectname;

@VitalPluginInfo(
        name = "test-plugin",
        environment = VitalPluginEnvironment.SPIGOT_PAPER
)
public class ProjectPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        Vital.run(getClass(), getName());
    }
}
```

Bungeecord:

```java
package my.domain.company.projectname;

@VitalPluginInfo(
        name = "test-plugin",
        environment = VitalPluginEnvironment.BUNGEECORD
)
public class ProjectPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        Vital.run(getClass(), getDescription().getName());
    }
}
```

When configured correctly using the `vital-gradle-plugin` you should be able to build your project and directly deploy
it on a server.

Vital will generate the `plugin.yml` file automatically for you if you included the dependencies correctly.

The `vital-core-processor` dependency is needed for this to work, usually it is automatically applied when you use
the `vital-gradle-plugin`

From here on, you can utilize all of Spring's functionality and features.

To fetch any Spring component outside a component (like in your main plugin class), you can
use `Vital#getContext()#getBean(...)` to retrieve components registered by vital and Spring.

So if you want to fetch a Component after you enable Vital, your code could look like this:

```java

@Override
public void onEnable() {
    Vital.run(getClass(), getName());

    // The "MyComponent" class must be annotated with a valid "@Component" annotation so Spring picks it up
    MyComponent myComponent = Vital.getContext().getBean(MyComponent.class);

    myComponent.doStuff();
}
```

6. Vital is now ready to perform!
7. Now you can move onto the section needed for your plugin!

## Listeners

When using Vital, you can use Vital's predefinded `VitalListener` classes to implement a self registering listener.

Your listener should look like this:

```java
package my.domain.company.projectname.listener;

// needed to automatically register this listener
@Component
public class MySpigotListener extends VitalListener.Spigot {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        // as you can see, this is exactly the same as when we use normal spigot, just the wrapper changes
    }
}
```

And now for Bungeecord:

```java
package my.domain.company.projectname.listener;

// needed to automatically register this listener
@Component
public class MyBungeecordListener extends VitalListener.Bungeecord {
    @EventHandler
    public void onPlayerJoin(PostLoginEvent e) {
        // as you can see, this is exactly the same as when we use normal bungeecord, just the wrapper changes
    }
}
```

## Commands

When using Vital, you can use Vital's predefined `VitalCommand` classes to implement a self registering command.

Your command should look like this:

```java
package my.domain.company.projectname.command;

@VitalCommandInfo(
        name = "testcommand",
        args = {
                @VitalCommandArg("arg"), // this is just a normal fixed arg
                @VitalCommandArg("arg %PLAYER%") // this is a dynamic arg, the player can run "/testcommand arg ANYTHINGHERE"
        }
)
public class MySpigotCommand extends VitalCommand.Spigot {
    @Override
    public VitalCommandReturnState onBaseCommand(CommandSender sender) {
        // here you can implement any code that should run when the command sender executes command "/testcommand"

        // return value can be any state you need for this command execution, this applies for all methods that return such values.
        return VitalCommandReturnState.SUCCESS;
    }

    // there are many methods here that start with the "on..." method syntax, these can be overridden to implement custom logic for common command events
    @Override
    public void onCommandInvalidArgs(@NonNull CommandSender sender, @NonNull String args) {
        // here you can implement logic that should run if the command is run with arguments that are not supplied in the @VitalCommandInfo annotation
    }

    // sometimes it is required for command senders to supply data to an executing command, for example when we want to teleport to a player via "/tp PLAYERNAME"
    // the player name represent a dynamic argument here which can actually be anything the user provides the command argument with
    // this method catches all non-fixed arguments in the provided String array (String[])
    // since we defined in our @VitalCommandInfo, an argument called "arg %PLAYER%" we can expect one value passed in the String array (String[])
    @VitalCommandArgHandler({"arg %PLAYER%"})
    public VitalCommandReturnState onArgPlayer(CommandSender sender, String[] values) {
        // here the values are ALWAYS = ["someValueTheSenderHasPassedHereCanBeAnything"]

        return VitalCommandReturnState.SUCCESS;
    }

    // summarized, the values String array (String[]) is always the size of how many %VAR% arguments we provide within @VitalCommandInfo

    // we can also have arg handlers that handle multiple arguments, e.g.
    @VitalCommandArgHandler({
            "arg",
            "arg %PLAYER%"
    })
    public VitalCommandReturnState onArgOrArgPlayer(CommandSender sender, String[] values) {
        // this method would be called when the sender either executes "/testcommand arg" or "/testcommand arg SOMEVALUEHERE"
        // since we catch 2 different command scenarios we cannot be sure if the values String array (String[]) will be filled here, we'd first have to check if it is so we won't run into any problems

        return VitalCommandReturnState.SUCCESS;
    }
}
```

The implementation stays the same with Bungeecord, but instead of extending `VitalCommand.Spigot` you would extend `
VitalCommand.Bungeecord.

## Items

When using Vital, you can use Vital's predefined `VitalItem` class to implement a self registering class based item
which can have right and left-click actions.

Your item class should look like this:

```java
package my.domain.company.projectname.item;

@VitalItemInfo(name = "<yellow>My Item")
public class MyItem extends VitalItem {
    @Override
    public void onLeftClick(PlayerInteractEvent e) {
        // here you can implement any logic that should run when the player left clicks with the item in hand
    }

    @Override
    public void onRightClick(PlayerInteractEvent e) {
        // here you can implement any logic that should run when the player left clicks with the item in hand
    }
}
```

Since Bungeecord is a proxy, there is no Bungeecord implementation for this class.

## Players

This module is very important for project that require different information for players across the server, that could
include "Perks, Levels, Statistics, etc."  
With this module you are able to easily implement your own custom player management solution for your plugin.

Follow this setup process:

1. You have to create your player class that houses the data each player can carry:

```java
package my.domain.company.projectname.model;

public class MySpigotPlayer extends VitalPlayer.Spigot {
    // here we have a variable this is store on our custom player object
    @Getter
    @Setter
    private int coins;

    public MySpigotPlayer(Player player) {
        super(player);
    }
}
```

2. You have to create a custom player repository, that manages your custom player instances:

```java
package my.domain.company.projectname.repository;

@Component
public class MySpigotPlayerRepository extends VitalRepository<MySpigotPlayer> {

}
```

3. Then you have to create a custom player listener, which is responsible for automatically managing your custom player
   instances when players join or leave the server:

```java
package my.domain.company.projectname.listener;

@Component
// here in the generic type declaration (<>) we must define the types for our custom player class and our custom player repository
public class MySpigotPlayerListener extends VitalPlayerListener.Spigot<MySpigotPlayer, MySpigotPlayerRepository> {
    // and here we sadly still have to implement this method since type erasure erases the types needed for autoconfiguration
    @Override
    public Class<MySpigotPlayer> vitalPlayerType() {
        return MySpigotPlayer.class;
    }
}
```

Once that is done, you can utilize your very own custom player management system.

To fetch players from it, you can use your "MySpigotPlayerRepository" component, which can be injected into any other
component.

```java
// since we are annotating this class as @Component, it is automatically instantiated and injected via Spring
@Component
public class MyComponent {
    private final MySpigotPlayerRepository playerRepository;

    public MyComponent(MySpigotPlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public void doStuff() {
        playerRepository.getComponentByName("somePlayerName");
        // here we put the actual uuid of the player, this is just an example
        playerRepository.getComponentByUniqueId(UUID.randomUUID());
    }
}
```

The same goes for Bungeecord, but there we have to extend the corresponding Bungeecord classes instead of Spigot.

## Tasks

When using Vital, you can use Vital's predefined `VitalRepeatableTask` and `VitalCountdownTask` classes to create self
registering tasks for both Spigot and Bungeecord.

Your repeatable task should look like this:

```java
package my.domain.company.projectname.task;

// min interval is 5, which corresponds to exactly 1 ingame tick.
// interval is measured in millis
@VitalRepeatableTaskInfo(interval = 50)
public class MyRepeatableTask extends VitalRepeatableTask.Spigot {
    // there are a couple classes we can override for certain functionality

    @Override
    public void onStart() {
        // here we define logic for when the task is started using "start()"
    }

    @Override
    public void onTick() {
        // here we define logic for every task tick, which in this case would be every 50 millis, or 1 ingame tick
    }

    @Override
    public void onStop() {
        // here we define logic for when the task is stopped using "stop()"
    }
}
```

Your countdown task should look like this:

```java
package my.domain.company.projectname.task;

@VitalCountdownTaskInfo(interval = 50, countdown = 10)
public class MySpigotCountdownTask extends VitalCountdownTask.Spigot {
    // here we inherit every other method in VitalRepeatableTask
    // we also have methods regarding the current lifecycle of the countdown

    @Override
    public void onExpire() {
        // this method is called when the countdown has finally expired (reached 0)
    }

    @Override
    public void onReset() {
        // this method is called when "reset()" is called
    }

    // other methods that can be looked in up source docs
}
```

The same implementation is available for Bungeecord through Vital's corresponding `VitalRepeatableTask.Bungeecord`
and `VitalCountdownTask.Bungeecord` classes.

## Inventories

When using Vital, you can use Vital's predefined `VitalInventory` and `VitalPagedInventory` classes for creating and
managing interactive inventories.

Your normal non paged inventory class should look like this:

```java
package my.domain.company.projectname.inventory;

// IMPORTANT, if you mark this class as a Component, you define this inventory as a global inventory, which is available for dependency injection
// if you want an inventory that carries player specific data like, friend request manager, etc. you must remove this annotation and pass in any dependency needed through the constructor
@Component
public class MyInventory extends VitalInventory {
    public MyInventory(Inventory previousInventory) {
        // previousInventory can be null if no is available
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
    public void onUpdate() {
        setItem(0, ITEM);
        setItem(1, ITEM);
    }

    // on player inventory click, used to set items which are either player specific, with player data, or items that are clickable
    // NOTE: EVEN IF YOURE BUILDING A GLOBAL INVENTORY, AND NEED ITEMS THAT ARE CLICKABLE, YOU CAN USE THIS METHOD TO SET THOSE ITEMS!!!
    @Override
    public void onUpdate(Player player) {
        // not clickable, static item
        setItem(0, ITEM);

        // item that is clickable by the specified player
        setItem(1, ITEM, player, e -> {
            // this function is called when the player clicks this item, you can use any code above this statement in here to perform actions!!!
        });
    }
}
```

When creating a global inventory that instance is available through dependency injection via Spring beans:

```java

@Component
public class MyComponent {
    private final MyInventory myInventory;

    public MyComponent(MyInventory myInventory) {
        this.myInventory = myInventory;
    }

    public void doStuff(Player player) {
        player.openInventory(myInventory.getInventory());
    }
}
```

When NOT USING @Component, you have to manually create an instance of that inventory, then open it for the player:

```java
public void doStuff(Player player) {
    MyInventory myInventory = new MyInventory(null);

    player.openInventory(myInventory.getInventory());
}
```

There is also a way to create a paged inventory, which can hold many pages containing certain information (Very useful
for store pages, item shops, etc)

Your paged inventory should look like this:

```java
package my.domain.company.projectname.inventory;

// the same goes for paged inventories, if they are marked with @Component, they are considered global inventories, only one instance of them exist
// to create player specific paged inventories, remove the @Component annotation from it and instantiate an instance of that class yourself when trying to open it for a player
@Component
@VitalPagedInventoryInfo()
public class MyPagedInventory extends VitalPagedInventory {
    // here we can override specific methods for the lifecycle of this paged inventory
    // take a look at the class source code documentation or just the source class for more info about methods to override, the names should be self-explanatory 

    // the functions for methods that already exist in normal vital inventories, like mentioned above, they work the same with paged inventories!
}
```

## Scoreboards

When using Vital, you can use Vital's predefined classes to easily create scoreboards that update information
dynamically

vital-scoreboards is seperated into PerPlayer scoreboards and GlobalScoreboard, the names should be self explanatory.  
Per player scoreboards carry information for each player alone.  
Global scoreboards are scoreboards with many players, that carry global information, this could be used for global
statistics in a minigame

```java
public VitalPerPlayerScoreboard perPlayerScoreboard = new VitalPerPlayerScoreboard(
        "TITLE",
        // since these lines are of functional nature, everytime you update the scoreboard, the functions are called to evaluate a return value
        // in short, this will always fetch the latest information from whatever call you provide here, strings are immutable, function return values like this are not.
        player -> "LINE 1",
        player -> "LINE 2",
        player -> "LINE 3",
        player -> "LINE 4"
);

public VitalGlobalScoreboard globalScoreboard = new VitalGlobalScoreboard(
        "TITLE",
        "LINE 1",
        "LINE 2",
        "LINE 3",
        "LINE 4"
);

public void doStuff(Player player) {
    // then you can add players to these scoreboards...
    perPlayerScoreboard.addPlayer(player);
    globalScoreboard.addPlayer(player);
}
```

## Configs

Using Vital, you can easily create complex configuration files, here we dont work with files directly... we work with
object instead, which is waaay more elegant than calling config keys all of the time....

Configs are always class based, so you have to extend the predefined Vital class "VitalConfig".

Your config should look like this:

```java
package my.domain.company.projectname.config;

// here we define the file name and the processor we want to use for our file, we can use any file processor implementation for serializing and deserializing our config if you want to use any other file type of your liking.
@VitalConfigInfo(name = "", processor = YMLFileProcessor.class)
public class MyConfig extends VitalConfig {
    // now we can define our config "keys" using the @Property annotation, which defines the managing types of you field, yes there can be multiple, especially when you are working with maps!
    @Property({
            String.class
    })
    // the field should be public
    public String myProperty;

    // you can also define complex types, but be aware of that the type should be instantiable with either an empty constructor or with a constructor that defines the properties needed to load that object from config.
    @Property({
            MyCustomClass.class
    })
    public MyCustomClass myCustomClass;
}
```

This is how the complex object type should look like when you need to write it to config:

```java
public class MyCustomClass {
    @Property({
            String.class
    })
    public String someProperty;

    public MyCustomClass() {
        // the empty constructor is valid for serialization
    }

    // but this constructor is also valid, since it contains all fields annotated with @Property
    // if we would have multiple @Property fields, we would have to have a corresponding constructor which initializes all @Property annotated fields
    public MyCustomClass(String someProperty) {
        this.someProperty = someProperty;
    }
}
```

To save the config you simply call `VitalConfig#save()` where `VitalConfig` is the instance of your config.

## Minigames

When using Vital, you can easily create minigames using Vital's predefined classes.

All minigames should follow the standard of being split up into multiple game states...  
Every game state serves a different purpose or "part" / "phase" of the minigame.  
For example, there could be a game state that is active when we are waiting for more players to join the server before
we can start the actual minigame itself.  
And then theres the actual ingame state that serves ingame logic, for example there players can die, respawn at their
base, etc.  
All of these functions are always kept in their corresponding game state to encapsulate parts of the game as much as
possible.  
When following this standard, you can always switch from one game state into another without having to set anything up
manually.

Heres how that could work in an example:

```java
package my.domain.company.projectname.state;

// States must always be a component, otherwise we would break the standard!
@Component
public class WaitingState extends VitalMinigameState {
    // when switching to a game state, the onEnable() method is called on that game state
    @Override
    public void onEnable() {
        // some initialization code
    }

    // when we switch between game states, we also disable the last game state that we were on before switching to the new one
    @Override
    public void onDisable() {
        // here we can implement any logic that may revert the effects the "onEnable()" method made
    }

    // all game states are also listeners, so you can always place any event here...
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        // do some stuff
    }
}
```

Game state can also be a countdown, for example when the game is about to start, there may be a 10s timer.

```java
package my.domain.company.projectname.state;

// we do not need to add the @Component annotation here, since that is already supplied by @VitalCountdownTaskInfo, doesn't hurt to apply it anyway...
// when we implement countdown game states, we also HAVE TO apply this annotation to the class, since the underlying implementation is a VitalCountdownTask instance!
@VitalCountdownTaskInfo(interval = 1_000, countdown = 10)
public class StartingState extends VitalCountdownMinigameState {
    // here we can just override any method that can be overridden in the normal VitalCountdownTask
    // this class also has the "onEnable()" and "onDisable()" methods just like the game state without a countdown
}
```