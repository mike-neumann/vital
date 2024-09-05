# 🚀 Vital-Framework 🚀

## 🌟Overview

Vital is the new framework to streamline your plugin development.  
Current implementations of the mc server api require you to manually implement systems which could easily be replaced by
already existing ones.  
Spring solved this issue years ago, why invent the wheel anew?  
Vital is designed to work in tandem with the programmer, allowing you to create simple or even extensive and complex
server application.

## Techstack

- JDK 21
- Spigot 1.20.1
- Gradle
- Spring

## 🧩Module Overview

Vital is thoughtfully organized into several submodules which each serve a different purpose, so you can only implement
the things you need for your project.

- 🏢**vital-core**: Provides Vital's core functionality (required by some modules)
- 👥**vital-users**: Implement your own player management system, useful when you need to store player information, why
  not store them directly on a player instance?
- 📜**vital-configs**: OOP-Driven configuration system designed to simplify and improve the way you interact with
  configuration files, now classes are your config!
- ⌨️**vital-commands**: Custom command creation system to easily create organized commands, even with automatic tab
  completion and permission management!
- 💫**vital-holograms**: Easily create your own holograms, store them in configs (vital-configs) or whatever
- 🍄**vital-items**: Allows you to create your own class based items which serve right and left-click functions to the
  user wielding it
- ⏲️**vital-tasks**: Custom scheduler service, provides you with the ability to create repeated tasks and even
  countdowns
- 📋**vital-inventories**: Create interactive inventory menus with this module, global or player based ones
- 📊**vital-scoreboards**: OK I don't even need to explain why you need this... Easily create scoreboard within a single
  line of code!
- 🎮**vital-minigames**: Minigame management solution, provides minigame state management, countdown minigame states and
  much more!
- 🛠️**vital-utils**: Provides utilities useful for many implementations

Powered by Spring, Vital delivers component based, testable code, with the ability to include many extensions via Spring
dependencies.

## You matter

You can contribute to the Vital project to make it even better for everyone!  
Please PR (Pull Request) if you have any ideas or want to change something.
