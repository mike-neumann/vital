# vital-minigames

This module can be used to add Vital's advanced minigame system to your plugin.  
It provides the means to make advanced state-driven games possible.  

Each game may have n-amount of states that define a "phase" in your game.  
E.g., Waiting, Starting, Running, Ending, etc.  

Every state will have its own event listeners, which will only be active when the state is active.  
This makes your code much cleaner and easier to maintain.  

This module also supports instance-based games, so you can finally host multiple truly isolated games on the same server, 
without having to worry about logic leaking into any of your other games.  

Each instance-game will be scoped to its own world to fully isolate it from any of your other games.  
Just like normal games, instance-games will also have n-amount of states which define the phases of your game.  
Each event handler will only fire, when the event that occurred actually came from the world of your instance.  

This makes game development MUCH easier and cleaner.  
It works like magic.
