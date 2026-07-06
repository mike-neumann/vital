# vital-inventories

This module can be used to add Vital's advanced inventory menu system to your plugin.  
It provides the means of defining global inventories, which can hold data not tied to a specific player, 
inventories that may hold player-specific data, as well as paged inventories.  

Inventories are classes, that define the general structure and content of the inventory.  
As well as event handlers, to react to user input, e.g. open inventory, click on item, close inventory, change page, etc.  

Item click events are automatically routed to your item when you set it in your inventory, so you don't have to add boilerplate checks to your click handler.
