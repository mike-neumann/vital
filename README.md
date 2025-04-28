# 🚀 Vital-Framework 🚀

## 🌟Overview

Vital is the new framework to streamline your plugin development.  
Current implementations of the mc server api require you to manually implement systems which could easily be replaced by
already existing ones.  
Spring solved this issue years ago...  
Vital is designed to work in tandem with the programmer, allowing you to create simple or even extensive and complex
server applications.

## TechStack

- JDK 21
- Spigot 1.21.4
- Gradle
- Spring Boot 3.x

## 🧩Module Overview

Vital is thoughtfully organized into several submodules which each serve a different purpose, so you can only implement
the things you really need for your project.

- **vital-cloudnet4-bridge**: Provides an intuitive way to interact with the CloudNET-Bridge module (v4).
- **vital-cloudnet4-driver**: Provides an intuitive way to interact with the CloudNET-Driver module (v4).
- **vital-commands**: Provides an extensive way to create custom commands, even with automatic tab-completion and custom
  exception handling.
- **vital-configs**: Provides an OOP-Driven configuration system. Read and write to config files using classes instead
  of raw and loose data.
- **vital-core**: The core module required by all Vital modules.
- **vital-core-holograms**: Provides an easy and developer-friendly way to create holograms.
- **vital-inventories**: Provides an intuitive way to create reactive interactive inventory menus, global or
  player-based.
- **vital-items**: Provides a way to create interactive items directly as a class, to encapsulate functionality.
- **vital-minigames**: Provides an easy way to create minigames using a custom state-system to make minigame creation
  fun again, without creating spaghetti code.
- **vital-players**: Provides a custom player management solution, to store volatile data directly on a player class of
  your choice.
- **vital-scoreboards**: Provides an easy way to create reactive player-based or global scoreboard that may hold
  important information.
- **vital-statistics**: Hooks into your servers heartbeat to ensure server stability by having an eye on certain
  statistics you can view later.
- **vital-tasks**: Provides an easy way to implement server-based scheduled tasks using the servers runtime scheduler (
  RepeatableTask, Countdowns).
- **vital-utils**: Provides many utilities the server runtimes are missing to streamline development and make it even
  more efficient.

Powered by Spring Boot, Vital delivers component based, testable code, with the ability to include many extensions via Spring
dependencies.

## You matter

You can contribute to the Vital project to make it even better for everyone!  
PR (Pull Request) if you have any ideas or want to change something.

__Make minecraft development great again!__

## Getting started

To get started with Vital, please follow the instructions on the wiki.
