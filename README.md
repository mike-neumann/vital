# Vital - The ultimate Minecraft server plugin framework

Vital helps you to easily create Minecraft server plugins for Spigot, Paper and BungeeCord by providing you with APIs that make your life easier.  
Powered by Spring Boot at its core, Vital is designed to work for small projects as well as large ones, with the possibility to include only the modules / functionalitites you need for your project.  
This makes your plugin smaller and easier to maintain while also having a clean and structured codebase; it's a win-win for everyone!

## Highlights

- **No `plugin.yml` anymore!**  
Vital takes care of all the annoying plugin setup for you.  
Just write your code and let Vital do the rest.


- **Advanced command system**  
Vital offers the `vital-commands` and `vital-commands-processor` module to easily create commands for your plugin.  
Define function-based command handling with advanced exception handling and **automatic tab completion** right out of the box!


- **Advanced config system**  
With `vital-configs` you don't need to handle plugin configs yourself anymore.  
Create typed and easy-to-use configs using classes instead of juggling with raw strings!


- **Easy-to-use scoreboard system**  
Using `vital-scoreboards` you can easily create scoreboards for your players, without needing many lines of code!  
Create **dynamic** and **updatable** scoreboards with a single line of code!


- **Localization support**  
Vital offers built-in support for localization, so multilingual plugins can be easily created.  
Works in inventories and even **items** that are **already inside a player's inventory, without having to manually update anything!**  
Gotta tell you it works like magic


- **Interactive items**  
You can easily create interactive items with `vital-items`!  
Native support for **cooldowns**, **localization**, **left-click** and **right-click** events!


- **Multiplatform support**  
Vital is designed to work with Spigot, Paper and BungeeCord right out of the box.  
All modules are cross-platforms (Except those that don't make sense, like inventories for BungeeCord, etc.).


- **Utilities, even for non-Vital projects!**  
Interested in Vital but can't fully commit to it yet?  
You can still use Vital's utilities by using `vital-utils`!  
There you can find all kinds of utilities, like sending formatted (MiniMessage) titles, messages, actionbar, etc.


- **Module system**  
Vital's module system allows you to include only the modules you need for your project.  
This makes your plugin smaller and easier to maintain.  
Just want to use Vital as is? No problem, just use `vital-core`.  
Want an interactive inventory system? Go ahead and use `vital-inventories`.  
You can even integrate custom modules made by others if you want to!


- **Plug and play**  
Vital is designed to work right out of the box, no hidden configuration steps needed.  
Just drop it in your plugin and start developing.


- **Native support for bStats**  
Vital natively supports bStats, so you can easily track your plugin's performance!  
Just register your plugin on https://bstats.org/ and configure your plugin id in Vital!


## Sneak Peak

The following code snippet shows a full Vital plugin.  
This is seriously all you need to get started.

```java
@Vital.Info(
        name = "my-vital-plugin",
        description = "My first Vital plugin",
        apiVersion = "1.21",
        version = "1.0.0",
        author = {"Me"},
        environment = Vital.Info.PluginEnvironment.SPIGOT
)
public class MyPlugin {
    // This method will be called when Vital is up and running.
    // The name of the method doesn't matter.
    // You can also omit the parameter if you don't need it here.
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(ApplicationReadyEvent e) {
        // Do something here...
    }
    
    // This method will be called when Vital is shutting down.
    // The name of the method doesn't matter.
    // You can also omit the parameter if you don't need it here.
    @EventListener(ContextClosedEvent.class)
    public void onContextClosed(ContextClosedEvent e) {
        // Do something here...
    }
}
```

This plugin, of course, doesn't do anything yet.
Want to have a listener?  
Add this anywhere in your plugin (A different class / file, it doesn't matter).  

```java
// This annotation is the magic ingredient for Vital to automatically register your listener.
@Listener
public class MyPluginListener extends VitalListener.Spigot {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        // Do something here...
    }
}
```

Or a simple command?

```java
// This annotation is the magic ingredient for Vital to automatically register your command.
@VitalCommand.Info(
        name = "myFirstCommand",
        description = "My first Vital command"
)
public class MyPluginCommand extends VitalCommand.Spigot {
    @ArgHandler
    public ReturnState onNoArgs(Player player) {
        player.sendMessage("Hello from vital-commands");
        return ReturnState.SUCCESS;
    }
    
    // <someValueName> will be the first element in the "values" array.
    // add more <...> in your command and the array will grow accordingly.
    @ArgHandler(arg = @Arg("test <someValueName>"))
    public ReturnState onTestArg(Player player, String[] values) {
        player.sendMessage("You entered: " + values[0]);
        return ReturnState.SUCCESS;
    }
}
```
