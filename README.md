# Vital [![Buy me a coffee](bmc-button.svg)](https://buymeacoffee.com/mike.neumann)

A modular Minecraft plugin framework to create true multi-platform plugins for Spigot, Paper and BungeeCord without the hassle.  
It simplifies plugin development by automatically handling setup, commands, localization, minigames, and more.  
Vital offers easy-to-use APIs and true cross-platform support so you can finally focus on actually programming your plugin instead of building the foundations for it over and over and over again...

## About

Vital was born from the idea to finally create a centralized framework that can be used for building any plugin of any size, for any server platform.  
It currently supports Spigot, Paper and BungeeCord and can be used to create enterprise-grade plugins for Minecraft servers.  

At its core, Vital is powered by Spring Boot and silently launches a **lightweight** Spring Boot application when it starts.  
This allows you to create the most advanced plugins in Minecraft server plugin history.  

Vital is built modular, meaning that you can expand its functionality even more by adding modules to your project.  
Add official modules or even custom-made ones, the skies the limit!

## Highlights

- **No `plugin.yml` needed** – Vital auto-handles plugin setup.
- **Super advanced minigames system** – Finally create true state-based minigames with automatic event routing. Also supports hosting multiple isolated games on the same server.
- **Advanced commands system** – Easy command-creation with automatic tab-completion right out of the box.
- **Dynamic and easy scoreboards api** – Simple creation of updatable scoreboards with dynamic data.
- **Localization** – Built-in support for multilingual plugins.
- **Interactable items** – Create interactable items with ease.
- **Inventory menus** – Create interactive and dynamic inventory menus.
- **bStats** – bStats support comes right out-of-the-box.
- **Modular** – Only use the necessary features for a lightweight plugin.
- **Customizability** – Create and share your own modules to expand Vital even more!

## Comparisons

Not convinced yet?  
Check out some comparisons between Vital and Spigot [here](COMPARISONS.md).

## Get started (~5 minutes)

To quickly get started with a Vital project, you can use the [Vital-Initializer](initializer/README.md).  
It will help you generate a fully working plugin in about 5 minutes.  

## Documentation

For further information about how to get started with Vital and how to create your first plugin, please check out the [docs](http://vitalframework.dev).
