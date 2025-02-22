# Guide

## How to install

1. Clone `vital`.
2. Configure JDK 21 for project and gradle.
3. Reload gradle and wait until all background tasks have been completed.
4. `publishToMavenLocal` using gradle via run configuration (top right) or terminal `./gradlew publishToMavenLocal`.

## How to use

### Plugin project initialization

1. Create new project with gradle.
2. Configure plugin scanning in local m2 (`settings.gradle.kts`):
   ```kotlin
   pluginManagement {
       repositories {
           // this allows for gradle to scan our local m2.
           mavenLocal()
           // this is just the default gradle portal for any NON-LOCAL plugins.
           gradlePluginPortal()
       }
   }
   ```
3. Implement the `vital-gradle-plugin`:
   ```kotlin
   plugins {
       // this allows for some autoconfiguration regarding dependencies needed for vital to work.
       id("me.vitalframework.vital-gradle-plugin") version "1.0"
   }
   ```
4. Create main plugin class under your desired package e.g. `my.domain.company.projectname.ProjectPlugin`.
5. Initialize vital on plugin startup:
   ```kotlin
   @Vital.Info(
        name = "test-plugin", // REQUIRED
        description = "", // OPTIONAL
        apiVersion = "1.21", // OPTIONAL
        version = "1.0", // OPTIONAL
        authors = "xRa1ny", // OPTIONAL
        environment = Vital.Info.PluginEnvironment.SPIGOT, // REQUIRED
        springConfigLocations = ["classpath:application.properties"] // OPTIONAL
   )
   class ProjectPlugin : SpigotPlugin {
        override fun onEnable() {
            // This will initialize Vital and run Spring.
            Vital.run(this)
        }
   }
   ```
   When configured correctly, you should be able to build your project and directly deploy it on a server.  
   Vital will generate the `plugin.yml` automatically for you.

   From here on, you can utilize all of Spring's functionality and features.

   To fetch any Spring component outside a component, you can use `Vital#context()#getBean(...)`.
   So if you want to fetch a Component after you enable Vital, your code could look like this:

   ```kotlin
   override fun onEnable() {
        Vital.run(this)
   
        val myComponent = Vital.context.getBean(MyComponent::class.java)
   
        myComponent.doStuff()
   }
   ```
6. Vital is now ready to perform!
7. Now you can move onto the section needed for your plugin!

### Data storage (Entities, Repositories and Services)

Vital uses spring similar syntax for its components and functionalities.  
In Vital, the `VitalRepository` class is used to represent a container that stores non-persistent data (Like a
`JpaRepository` in spring, but volatile).  
Picture this, we want to store in-game requests for something.  
We can use a repository for this:

```kotlin
// this would be our model / entity
class Request(override var id: UUID) : VitalEntity<UUID>

// this is the container that stores all instances of our request class
@Repository
class RequestRepository : VitalRepository<Request, UUID>() 

// finally we have a service that contains business logic for our request entities
@Service
class RequestService(val requestRepository: RequestRepository) {
   fun createRequest()
   fun getRequest(id: UUID)
   fun deleteRequest(id: UUID)
}
```

### Listeners

Vital provides its own `VitalListener` class that automatically registers the listener, so you don't have to.  
Your listener should look like this:

```kotlin
@Component
class MySpigotListener(plugin: SpigotPlugin) : VitalListener.Spigot(plugin) {
    @SpigotEventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        // as you can see, this is exactly the same as when we use normal spigot, just the wrapper changes
    }
}
```

### Commands

Vital provides its own `VitalCommand` class, which is able to create function based commands.  
Commands are also automatically registered, so you don't have to register them manually.  
Your command class should look like this:

```kotlin
@VitalCommand.Info(
    name = "testcommand",
    description = "description",
    aliases = ["test"],
    usage = "Usage error message",
    permission = "my.domain.company.project.command.testcommand",
    playerOnly = false
)
class MySpigotCommand(plugin: SpigotPlugin) : VitalCommand.Spigot(plugin) {
    override fun onBaseCommand(sender: SpigotCommandSender): ReturnState {
        // here you can implement any code that should run when the command sender executes the command "/testcommand".
        // the return value can be any state you need for this command execution, this applies for all methods that return such values.
        return ReturnState.SUCCESS
    }

    // there are many methods here that start with the "on..." method syntax, these can be overridden to implement custom logic for common command events.
    override fun onCommandInvalidArgs(sender: SpigotCommandSender, args: String) {
        // here you can implement logic that should run if the command is run with arguments that are not supplied in the @VitalCommand.Info annotation.
        // in this case, args would be the arguments the command sender has supplied.
    }

    // sometimes it is required for command senders to supply data to an executing command, for example when we want to teleport to a player via "/tp PLAYERNAME"
    // the player name represents a dynamic argument here which can actually be anything the user provides the command argument with.
    // this method catches all non-fixed arguments in the provided string array
    // all arguments available for every arg handler is defined in "VitalCommand.ArgHandlerContext".
    // not all arguments must be supplied, any unknown ones will be ignored, or may fail when mapping.
    @ArgHandler(Arg("arg %PLAYER%"))
    fun onArgPlayer(sender: SpigotCommandSender, executedArg: String, arg: Arg, values: Array<String>): ReturnState {
        // this method would be called when the sender executes "/testcommand someValueTheSenderHasPassedHereCanBeAnything"
        // here the values are ALWAYS = ["someValueTheSenderHasPassedHereCanBeAnything"]
        return VitalCommandReturnState.SUCCESS
    }

    @ArgHandler(Arg("arg"))
    fun onArg(sender: SpigotCommandSender, values: Array<String>): ReturnState {
        // this method would be called when the sender executes "/testcommand arg"
        // since this argument handler does not expect any values, the values array would be empty
        return VitalCommandReturnState.SUCCESS
    }

    // sometimes we need to handle errors in argument exceptions.
    // this can be done with arg exception handlers, which map certain exception handling functions to arguments.
    // this method would be called, if an exception of type "Exception" was thrown while executing the argument "arg" function as defined above.
    // all arguments available for every arg exception handler is defined in "VitalCommand.ArgExceptionHandlerContext"
    // not all arguments must be supplied, any unknown ones will be ignored, or may fail when mapping.
    @ArgExceptionHandler("arg", type = Exception::class)
    fun onArgException(sender: SpigotCommandSender, executedArg: String, arg: Arg, exception: Exception) {
    }
}
```

### Items

Vital provides its own `VitalItem` class to create interactive items.
Your item class could look like this:

```kotlin
@VitalItem.Info(
    name = "<yellow>My Item",
    lore = ["<yellow>line1", "<blue>line2", "<red>line3"],
    amount = 1,
    type = Material.STONE,
    itemFlags = [ItemFlag.HIDE_ENCHANTS],
    cooldown = 1_000, // measured in milliseconds
    enchanted = false,
    unbreakable = true
)
class MyItem : VitalItem {
    override fun onLeftClick(e: PlayerInteractEvent) {
        // here you can implement any logic that should run when the player left clicks with the item in hand
    }

    override fun onRightClick(e: PlayerInteractEvent) {
        // here you can implement any logic that should run when the player left clicks with the item in hand
    }
}
```

### Players

This module is very important for projects that require different information for players across the server, that could
include "Perks, Levels, Statistics, etc.".  
With this module you are able to easily implement your own custom player management solution for your plugin.  
Follow this setup process:

1. You have to create your player class that houses the data each player can carry:
   ```kotlin
   class MySpigotPlayer(player: SpigotPlayer) : VitalPlayer.Spigot(player) {
       // here we have a variable this is store on our custom player object
       var coins: Int
   }
   ```
2. You have to specify which class should be used for every player instance in `application.properties`:
   ```properties
   vital.players.type=my.domain.company.projectname.player.MySpigotPlayer
   ```

Once that is done, you can utilize your very own custom player management system via Vital's `VitalPlayerService` class.

### Tasks

Vital provides its own task classes for interacting with the server's scheduler.  
`VitalRepeatableTask` and `VitalCountdownTask`.  
Your repeatable task should look like this:

```kotlin
// min interval is 50, which corresponds to exactly 1 in-game tick.
// interval is measured in milliseconds
@VitalRepeatableTask.Info(interval = 50)
class MyRepeatableTask(plugin: SpigotPlugin) : VitalRepeatableTask.Spigot(plugin) {
    override fun onStart() {
        // here we define logic for when the task is started using "start()"
    }

    override fun onTick() {
        // here we define logic for every task tick, which in this case would be every 50 millis, or 1 ingame tick
    }

    override fun onStop() {
        // here we define logic for when the task is stopped using "stop()"
    }
}
```

Your countdown task should look like this:

```kotlin
@VitalCountdownTask.Info(interval = 50, countdown = 10)
class MySpigotCountdownTask(plugin: SpigotPlugin) : VitalCountdownTask.Spigot(plugin) {
    // here we inherit every other method in VitalRepeatableTask
    // we also have methods regarding the current lifecycle of the countdown
    override fun onExpire() {
        // this method is called when the countdown has finally expired (reached 0)
    }

    override fun onReset() {
        // this method is called when "reset()" is called
    }
    // other methods that can be looked in up source docs
}
```

### Inventories

Vital provides its own `VitalInventory` and `VitalPagedInventory` classes to create interative inventories.  
Your normal non paged inventory class should look like this:

```kotlin
@VitalInventory.Info(name = "<yellow>Inventory", size = 54, background = Material.STONE)
class MyInventory(previousInventory: VitalInventory?) : VitalInventory(previousInventory) {
    override fun onOpen(player: SpigotPlayer) {
        // any inventory open logic for the opening player
    }

    override fun onClose(player: SpigotPlayer) {
        // any inventory close logic for the closing player
    }

    // on global inventory update, used to set items which are static, and do not change or are not clickable
    override fun onUpdate() {
        setItem(0, ITEM)
        setItem(1, ITEM)
    }

    // on player inventory click, used to set items which are either player specific, with player data, or items that are clickable
    override fun onUpdate(player: SpigotPlayer) {
        // not clickable, static item
        setItem(0, ITEM)
        // item that is clickable by the specified player
        setItem(1, ITEM, player) {
            // this function is called when the player clicks this item, you can use any code above this statement in here to perform actions!!! 
        }
    }
}
```

Any inventory can be opened as follows: `VitalInventory#open(Player)`

There is also a way to create a paged inventory, which can hold many pages containing certain information (Very useful
for store pages, item shops, etc.)

Your paged inventory should look like this:

```kotlin
// we also need to define @VitalInventory.Info to specify the inventory's general meta-information.
@VitalInventory.Info(name = "<yellow>Inventory", size = 54, background = Material.STONE)
// fromSlot defines the slot where the "paged" items start
// toSlot defines the slot where the "paged" items end
// both values are inclusive, and important for paging correctly.
@VitalPagedInventory.Info(fromSlot = 0, toSlot = 9)
class MyPagedInventory(previousInventory: VitalInventory?) : VitalPagedInventory(previousInventory) {
    // here we can override specific methods for the lifecycle of this paged inventory
    // take a look at the class source code documentation or just the source class for more info about methods to override, the names should be self-explanatory 
    // the functions for methods that already exist in normal vital inventories, like mentioned above, work the same with paged inventories!
}
```

### Scoreboards

Vital provides its own `VitalPerPlayerScoreboard` and `VitalGlobalScoreboard` classes to create scoreboards.

The names should be self-explanatory.  
Per player scoreboards carry information for each player alone.  
Global scoreboards are scoreboards with many players, that carry global information.

```kotlin
val perPlayerScoreboard = VitalPerPlayerScoreboard(
    "TITLE",
    // since these lines are of functional nature, everytime you update the scoreboard, the functions are called to evaluate a return value
    // in short, this will always fetch the latest information from whatever call you provide here, strings are immutable, function return values like this are not.
    { "LINE 1" },
    { "LINE 2" },
    { "LINE 3" },
    { "LINE 4" }
)
val globalScoreboard = VitalGlobalScoreboard(
    "TITLE",
    "LINE 1",
    "LINE 2",
    "LINE 3",
    "LINE 4"
)

fun doStuff(player: SpigotPlayer) {
    // then you can add players to these scoreboards...
    perPlayerScoreboard.addPlayer(player)
    globalScoreboard.addPlayer(player)
}
```

### Configs

Vital provides a way to easily create complex configuration files, here we don't work with files directly... we work
with objects instead, which is waaay more elegant than calling config keys all the time....

Configs are always class based, so you have to extend the predefined Vital class `VitalConfig`.  
Your config could look like this:

```kotlin
// here we define the file name and the processor we want to use for our file, we can use any file processor implementation for serializing and deserializing our config if you want to use any other file type of your liking.
@VitalConfig.Info(
    name = "plugin.yaml",
    // here we define that we want to use a YAML file processor, that works with YAML formatted files (.yaml, .yml)
    processor = VitalYAMLFileProcessor::class.java
)
class MyConfig : VitalConfig {
    // now we can define our config "keys" using the @Property annotation, which defines the managing types of you field, yes there can be multiple, especially when you are working with maps!
    @Property(String::class.java)
    lateinit var myProperty: String

    // you can also define complex types, but be aware of that the type should be instantiable with either an empty constructor or with a constructor that defines the properties needed to load that object from config.
    @Property(MyCustomClass::class.java)
    lateinit var myCustomClass: MyCustomClass
}
```

This is how the complex object type should look like when you need to write it to config:

```kotlin
class MyCustomClass() {
    @Property(String::class.java)
    lateinit var someProperty: String
}
```

To save the config you simply call `VitalConfig#save()` where `VitalConfig` is the instance of your config.

### Minigames

Vital provides a way to easily create minigames.

All minigames should follow the standard of being split up into multiple game states...  
Every game state serves a different purpose or "part" / "phase" of the minigame.  
For example, there could be a game state that is active when we are waiting for more players to join the server before
we can start the actual minigame itself.  
And then there's the actual in-game state that serves in-game logic, for example, there players can die, respawn at
their base, etc.  
All of these functions are always kept in their corresponding game state classes to encapsulate parts of the game as
much as possible.  
When following this standard, you can always switch from one game state into another without having to set anything up
manually.

Here's how that could work in an example:

```kotlin
// States must always be a component, otherwise we would break the standard!
@Component
class WaitingState : VitalMinigameState {
    // when switching to a game state, the onEnable() method is called on that game state
    override fun onEnable() {
        // some initialization code
    }

    // when we switch between game states, we also disable the last game state that we were on before switching to the new one
    override fun onDisable() {
        // here we can implement any logic that may revert the effects the "onEnable()" method made
    }

    // all game states are also listeners, so you can always place any event here...
    @SpigotEventHandler
    override fun onPlayerJoin(e: PlayerJoinEvent) {
        // do some stuff
    }
}
```

Game states can also be a countdown, for example when the game is about to start, there may be a 10s timer.

```kotlin
// we do not need to add the @Component annotation here, since that is already supplied by @VitalCountdownTask.Info
// when we implement countdown game states, we also HAVE TO apply this annotation to the class, since the underlying implementation is a VitalCountdownTask instance!
@VitalCountdownTask.Info(interval = 1_000, countdown = 10)
class StartingState : VitalCountdownMinigameState {
    // here we can just override any method that can be overridden in the normal VitalCountdownTask
    // this class also has the "onEnable()" and "onDisable()" methods just like the game state without a countdown
}
```